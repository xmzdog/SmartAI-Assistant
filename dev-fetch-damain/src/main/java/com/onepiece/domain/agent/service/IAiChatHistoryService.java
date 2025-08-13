package com.onepiece.domain.agent.service;

import com.onepiece.domain.agent.model.entity.ChatHistoryEntity;
import com.onepiece.domain.agent.model.entity.ChatSessionEntity;

import java.util.List;

/**
 * AI聊天历史服务接口
 * 
 * @author xmz
 * @date 2025-08-13
 */
public interface IAiChatHistoryService {

    /**
     * 保存聊天记录
     */
    void saveChatRecord(ChatHistoryEntity chatHistory);

    /**
     * 获取聊天历史
     */
    List<ChatSessionEntity> getChatHistory(Long agentId, Long ragId);

    /**
     * 删除聊天会话
     */
    void deleteChatSession(String sessionId);

    /**
     * 获取聊天详情
     */
    ChatHistoryEntity getChatDetails(String chatId);

}
