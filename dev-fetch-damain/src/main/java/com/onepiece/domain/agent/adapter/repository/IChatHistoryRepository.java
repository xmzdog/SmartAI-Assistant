package com.onepiece.domain.agent.adapter.repository;

import com.onepiece.domain.agent.model.entity.ChatHistoryEntity;
import com.onepiece.domain.agent.model.entity.ChatMessageEntity;
import com.onepiece.domain.agent.model.entity.ChatSessionEntity;

import java.util.List;

/**
 * 聊天历史仓储接口
 * Domain层定义的接口，由Infrastructure层实现
 * 
 * @author xmz
 * @date 2025-08-13
 */
public interface IChatHistoryRepository {

    /**
     * 保存聊天会话
     * @param session 会话实体
     */
    void saveSession(ChatSessionEntity session);

    /**
     * 批量保存聊天消息
     * @param messages 消息列表
     */
    void saveMessages(List<ChatMessageEntity> messages);

    /**
     * 根据会话ID查询会话信息
     * @param sessionId 会话ID
     * @return 会话实体
     */
    ChatSessionEntity findSessionById(String sessionId);

    /**
     * 根据会话ID查询消息列表
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessageEntity> findMessagesBySessionId(String sessionId);

    /**
     * 根据智能体ID与可选的知识库ID查询会话列表
     * @param agentId 智能体ID
     * @param ragId 可选的知识库ID（null 则不过滤）
     * @return 会话列表
     */
    List<ChatSessionEntity> findSessionsByAgentId(Long agentId, Long ragId);

    /**
     * 更新会话信息
     * @param session 会话实体
     */
    void updateSession(ChatSessionEntity session);

    /**
     * 删除会话及其所有消息
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    /**
     * 检查会话是否存在
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean existsSession(String sessionId);

}
