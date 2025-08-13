package com.onepiece.domain.agent.service.chat;

import com.onepiece.domain.agent.adapter.repository.IChatHistoryRepository;
import com.onepiece.domain.agent.model.entity.ChatHistoryEntity;
import com.onepiece.domain.agent.model.entity.ChatMessageEntity;
import com.onepiece.domain.agent.model.entity.ChatSessionEntity;
import com.onepiece.domain.agent.service.IAiChatHistoryService;
import com.onepiece.domain.agent.service.ILargeContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AI聊天历史服务实现
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Slf4j
@Service
public class AiChatHistoryService implements IAiChatHistoryService {

    @Resource
    private IChatHistoryRepository chatHistoryRepository;

    @Resource
    private ILargeContentService largeContentService;

    @Override
    @Transactional
    public void saveChatRecord(ChatHistoryEntity chatHistory) {
        try {
            log.info("开始保存聊天记录，会话ID: {}", chatHistory.getId());

            // 检查会话是否存在
            ChatSessionEntity existingSession = chatHistoryRepository.findSessionById(chatHistory.getId());
            
            if (existingSession == null) {
                // 创建新会话
                ChatSessionEntity session = ChatSessionEntity.builder()
                        .id(chatHistory.getId())
                        .agentId(chatHistory.getAgentId())
                        .ragId(chatHistory.getRagId())
                        .title(chatHistory.getTitle())
                        .messageCount(chatHistory.getMessages() != null ? chatHistory.getMessages().size() : 0)
                        .lastMessageTime(chatHistory.getLastMessageTime() != null ? chatHistory.getLastMessageTime() : new Date())
                        .createTime(new Date())
                        .build();
                
                chatHistoryRepository.saveSession(session);
                log.info("创建新聊天会话: {}", chatHistory.getId());
            } else {
                // 更新现有会话
                existingSession.setTitle(chatHistory.getTitle());
                existingSession.setMessageCount(chatHistory.getMessages() != null ? chatHistory.getMessages().size() : existingSession.getMessageCount());
                existingSession.setLastMessageTime(chatHistory.getLastMessageTime() != null ? chatHistory.getLastMessageTime() : new Date());
                
                chatHistoryRepository.updateSession(existingSession);
                log.info("更新聊天会话: {}", chatHistory.getId());
            }

            // 保存消息
            if (chatHistory.getMessages() != null && !chatHistory.getMessages().isEmpty()) {
                List<ChatMessageEntity> messagesToSave = new ArrayList<>();
                
                for (ChatMessageEntity messageEntity : chatHistory.getMessages()) {
                    String content = messageEntity.getContent();
                    String contentToStore = content;
                    
                    // 检查是否为大内容
                    if (largeContentService.isLargeContent(content)) {
                        log.info("检测到大内容消息，消息ID: {}, 内容大小: {} 字节", 
                                messageEntity.getId(), content.getBytes().length);
                        
                        // 存储大内容到专门的存储中
                        largeContentService.storeLargeContent(messageEntity.getId(), content);
                        
                        // 在数据库中只存储摘要信息
                        contentToStore = String.format("[大内容消息 - %d 字节] %s...", 
                                content.getBytes().length, 
                                content.length() > 200 ? content.substring(0, 200) : content);
                    }
                    
                    ChatMessageEntity messageToSave = ChatMessageEntity.builder()
                            .id(messageEntity.getId())
                            .sessionId(chatHistory.getId())
                            .role(messageEntity.getRole())
                            .content(contentToStore)
                            .timestamp(messageEntity.getTimestamp() != null ? messageEntity.getTimestamp() : new Date())
                            .isStreaming(messageEntity.getIsStreaming() != null ? messageEntity.getIsStreaming() : false)
                            .build();
                    messagesToSave.add(messageToSave);
                }
                
                if (!messagesToSave.isEmpty()) {
                    chatHistoryRepository.saveMessages(messagesToSave);
                    log.info("保存 {} 条消息到会话: {}", messagesToSave.size(), chatHistory.getId());
                }
            }

            log.info("聊天记录保存完成，会话ID: {}", chatHistory.getId());

        } catch (Exception e) {
            log.error("保存聊天记录失败，会话ID: {}", chatHistory.getId(), e);
            throw new RuntimeException("保存聊天记录失败", e);
        }
    }

    @Override
    public List<ChatSessionEntity> getChatHistory(Long agentId, Long ragId) {
        try {
            log.info("查询智能体聊天会话列表，智能体ID: {}，ragId: {}", agentId, ragId);

            List<ChatSessionEntity> sessions = chatHistoryRepository.findSessionsByAgentId(agentId, ragId);
            
            log.info("查询到 {} 个会话，智能体ID: {}", sessions.size(), agentId);
            return sessions;

        } catch (Exception e) {
            log.error("查询聊天会话列表失败，智能体ID: {}", agentId, e);
            throw new RuntimeException("查询聊天会话列表失败", e);
        }
    }

    @Override
    public ChatHistoryEntity getChatDetails(String chatId) {
        try {
            log.info("查询聊天详情，会话ID: {}", chatId);

            ChatSessionEntity session = chatHistoryRepository.findSessionById(chatId);
            if (session == null) {
                log.warn("会话不存在，会话ID: {}", chatId);
                return null;
            }

            List<ChatMessageEntity> messages = chatHistoryRepository.findMessagesBySessionId(chatId);
            List<ChatMessageEntity> processedMessages = new ArrayList<>();

            for (ChatMessageEntity message : messages) {
                String content = message.getContent();
                
                // 检查是否为大内容摘要，如果是则加载完整内容
                if (content != null && content.startsWith("[大内容消息")) {
                    try {
                        String fullContent = largeContentService.retrieveLargeContent(message.getId());
                        if (fullContent != null) {
                            content = fullContent;
                            log.debug("加载大内容成功，消息ID: {}", message.getId());
                        } else {
                            log.warn("无法加载大内容，消息ID: {}, 使用摘要内容", message.getId());
                        }
                    } catch (Exception e) {
                        log.error("加载大内容失败，消息ID: {}, 使用摘要内容", message.getId(), e);
                    }
                }
                
                ChatMessageEntity processedMessage = ChatMessageEntity.builder()
                        .id(message.getId())
                        .sessionId(message.getSessionId())
                        .role(message.getRole())
                        .content(content)
                        .timestamp(message.getTimestamp())
                        .isStreaming(message.getIsStreaming())
                        .build();
                processedMessages.add(processedMessage);
            }

            ChatHistoryEntity chatHistory = ChatHistoryEntity.builder()
                    .id(session.getId())
                    .agentId(session.getAgentId())
                    .ragId(session.getRagId())
                    .title(session.getTitle())
                    .messages(processedMessages)
                    .createTime(session.getCreateTime())
                    .lastMessageTime(session.getLastMessageTime())
                    .build();

            log.info("查询聊天详情完成，会话ID: {}, 消息数: {}", chatId, processedMessages.size());
            return chatHistory;

        } catch (Exception e) {
            log.error("查询聊天详情失败，会话ID: {}", chatId, e);
            throw new RuntimeException("查询聊天详情失败", e);
        }
    }

    @Override
    @Transactional
    public void deleteChatSession(String sessionId) {
        try {
            log.info("删除聊天会话，会话ID: {}", sessionId);

            // 删除大内容存储
            List<ChatMessageEntity> messages = chatHistoryRepository.findMessagesBySessionId(sessionId);
            for (ChatMessageEntity message : messages) {
                if (message.getContent() != null && message.getContent().startsWith("[大内容消息")) {
                    try {
                        largeContentService.deleteLargeContent(message.getId());
                        log.debug("删除大内容，消息ID: {}", message.getId());
                    } catch (Exception e) {
                        log.warn("删除大内容失败，消息ID: {}", message.getId(), e);
                    }
                }
            }

            // 删除会话和消息
            chatHistoryRepository.deleteSession(sessionId);

            log.info("删除聊天会话完成，会话ID: {}", sessionId);

        } catch (Exception e) {
            log.error("删除聊天会话失败，会话ID: {}", sessionId, e);
            throw new RuntimeException("删除聊天会话失败", e);
        }
    }

}