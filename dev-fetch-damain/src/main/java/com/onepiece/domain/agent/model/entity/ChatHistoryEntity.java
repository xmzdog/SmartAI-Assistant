package com.onepiece.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 聊天历史实体
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryEntity {

    /**
     * 会话ID
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
    private String title;

    /**
     * 消息列表
     */
    private List<ChatMessageEntity> messages;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后消息时间
     */
    private Date lastMessageTime;

}
