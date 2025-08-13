package com.onepiece.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * AI聊天消息表
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiChatMessage {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息唯一标识（前端生成）
     */
    private String messageId;

    /**
     * 角色(user/assistant)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息顺序
     */
    private Integer messageOrder;

    /**
     * 是否为流式消息
     */
    private Boolean isStreaming;

    /**
     * 创建时间
     */
    private Date createTime;

}
