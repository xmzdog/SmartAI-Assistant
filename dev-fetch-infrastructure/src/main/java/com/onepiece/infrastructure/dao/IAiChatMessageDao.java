package com.onepiece.infrastructure.dao;

import com.onepiece.infrastructure.dao.po.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI聊天消息数据访问接口
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Mapper
public interface IAiChatMessageDao {

    /**
     * 插入聊天消息
     */
    void insert(AiChatMessage message);

    /**
     * 批量插入聊天消息
     */
    void insertBatch(@Param("messages") List<AiChatMessage> messages);

    /**
     * 根据会话ID查询消息列表
     */
    List<AiChatMessage> queryBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID删除消息
     */
    void deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID统计消息数量
     */
    Integer countBySessionId(@Param("sessionId") String sessionId);

}
