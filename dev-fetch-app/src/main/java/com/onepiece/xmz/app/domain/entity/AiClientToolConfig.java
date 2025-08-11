package com.onepiece.xmz.app.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客户端-工具关联配置实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client_tool_config")
public class AiClientToolConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "tool_type", nullable = false, length = 20)
    private String toolType;
    
    @Column(name = "tool_id", nullable = false)
    private Long toolId;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "client_id", insertable = false, updatable = false)
    // private AiClient client;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "tool_id", insertable = false, updatable = false)
    // private AiClientToolMcp mcpTool;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
