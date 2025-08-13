package com.onepiece.infrastructure.dao;

import com.onepiece.infrastructure.dao.po.AiChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI聊天会话数据访问接口
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Mapper
public interface IAiChatSessionDao {

    /**
     * 插入聊天会话
     */
    void insert(AiChatSession session);

    /**
     * 更新聊天会话
     */
    void update(AiChatSession session);

    /**
     * 根据ID查询聊天会话
     */
    AiChatSession queryById(@Param("id") String id);

    /**
     * 根据智能体ID查询聊天会话列表
     */
    List<AiChatSession> queryByAgentId(@Param("agentId") Long agentId, @Param("ragId") Long ragId);

    /**
     * 删除聊天会话
     */
    void deleteById(@Param("id") String id);

    /**
     * 更新会话消息统计
     */
    void updateMessageStats(@Param("sessionId") String sessionId, 
                           @Param("messageCount") Integer messageCount,
                           @Param("lastMessageTime") java.util.Date lastMessageTime);

}
