package com.onepiece.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * AI聊天会话表
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiChatSession {

    /**
     * 会话ID（前端生成的唯一标识）
     */
    private String id;

    /**
     * 智能体ID
     */
    private Long agentId;

    /**
     * 知识库ID
     */
    private Long ragId;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 最后消息时间
     */
    private Date lastMessageTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
