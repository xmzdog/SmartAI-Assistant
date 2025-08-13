package com.onepiece.infrastructure.adapter.repository;

import com.onepiece.domain.agent.adapter.repository.IChatHistoryRepository;
import com.onepiece.domain.agent.model.entity.ChatHistoryEntity;
import com.onepiece.domain.agent.model.entity.ChatMessageEntity;
import com.onepiece.domain.agent.model.entity.ChatSessionEntity;
import com.onepiece.infrastructure.dao.IAiChatMessageDao;
import com.onepiece.infrastructure.dao.IAiChatSessionDao;
import com.onepiece.infrastructure.dao.po.AiChatMessage;
import com.onepiece.infrastructure.dao.po.AiChatSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 聊天历史仓储实现
 * Infrastructure层实现Domain层定义的接口
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Slf4j
@Repository
public class ChatHistoryRepositoryImpl implements IChatHistoryRepository {

    @Resource
    private IAiChatSessionDao chatSessionDao;

    @Resource
    private IAiChatMessageDao chatMessageDao;

    @Override
    public void saveSession(ChatSessionEntity session) {
        AiChatSession po = AiChatSession.builder()
                .id(session.getId())
                .agentId(session.getAgentId())
                .ragId(session.getRagId())
                .sessionTitle(session.getTitle())
                .messageCount(session.getMessageCount())
                .lastMessageTime(session.getLastMessageTime())
                .build();
        
        chatSessionDao.insert(po);
    }

    @Override
    public void saveMessages(List<ChatMessageEntity> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        List<AiChatMessage> pos = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(1);
        
        for (ChatMessageEntity message : messages) {
            AiChatMessage po = AiChatMessage.builder()
                    .sessionId(message.getSessionId())
                    .messageId(message.getId())
                    .role(message.getRole())
                    .content(message.getContent())
                    .messageOrder(order.getAndIncrement())
                    .isStreaming(message.getIsStreaming() != null ? message.getIsStreaming() : false)
                    .build();
            pos.add(po);
        }
        
        chatMessageDao.insertBatch(pos);
    }

    @Override
    public ChatSessionEntity findSessionById(String sessionId) {
        AiChatSession po = chatSessionDao.queryById(sessionId);
        if (po == null) {
            return null;
        }
        
        return ChatSessionEntity.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .ragId(po.getRagId())
                .title(po.getSessionTitle())
                .messageCount(po.getMessageCount())
                .lastMessageTime(po.getLastMessageTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    @Override
    public List<ChatMessageEntity> findMessagesBySessionId(String sessionId) {
        List<AiChatMessage> pos = chatMessageDao.queryBySessionId(sessionId);
        List<ChatMessageEntity> entities = new ArrayList<>();
        
        for (AiChatMessage po : pos) {
            ChatMessageEntity entity = ChatMessageEntity.builder()
                    .id(po.getMessageId())
                    .sessionId(po.getSessionId())
                    .role(po.getRole())
                    .content(po.getContent())
                    .timestamp(po.getCreateTime())
                    .isStreaming(po.getIsStreaming())
                    .build();
            entities.add(entity);
        }
        
        return entities;
    }

    @Override
    public List<ChatSessionEntity> findSessionsByAgentId(Long agentId, Long ragId) {
        List<AiChatSession> pos = chatSessionDao.queryByAgentId(agentId, ragId);
        List<ChatSessionEntity> entities = new ArrayList<>();
        
        for (AiChatSession po : pos) {
            ChatSessionEntity entity = ChatSessionEntity.builder()
                    .id(po.getId())
                    .agentId(po.getAgentId())
                    .ragId(po.getRagId())
                    .title(po.getSessionTitle())
                    .messageCount(po.getMessageCount())
                    .lastMessageTime(po.getLastMessageTime())
                    .createTime(po.getCreateTime())
                    .updateTime(po.getUpdateTime())
                    .build();
            entities.add(entity);
        }
        
        return entities;
    }

    @Override
    public void updateSession(ChatSessionEntity session) {
        AiChatSession po = AiChatSession.builder()
                .id(session.getId())
                .agentId(session.getAgentId())
                .ragId(session.getRagId())
                .sessionTitle(session.getTitle())
                .messageCount(session.getMessageCount())
                .lastMessageTime(session.getLastMessageTime())
                .build();
        
        chatSessionDao.update(po);
    }

    @Override
    public void deleteSession(String sessionId) {
        // 先删除消息
        chatMessageDao.deleteBySessionId(sessionId);
        // 再删除会话
        chatSessionDao.deleteById(sessionId);
    }

    @Override
    public boolean existsSession(String sessionId) {
        AiChatSession po = chatSessionDao.queryById(sessionId);
        return po != null;
    }

}
