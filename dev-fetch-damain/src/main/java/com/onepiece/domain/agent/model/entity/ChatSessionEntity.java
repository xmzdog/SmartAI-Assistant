package com.onepiece.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 聊天会话实体
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionEntity {

    /**
     * 会话ID
     */
    private String id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 智能体ID
     */
    private Long agentId;

    /**
     * 知识库ID
     */
    private Long ragId;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后消息时间
     */
    private Date lastMessageTime;

    /**
     * 预览内容（最后一条消息的部分内容）
     */
    private String preview;

    /**
     * 更新时间
     */
    private Date updateTime;

}
