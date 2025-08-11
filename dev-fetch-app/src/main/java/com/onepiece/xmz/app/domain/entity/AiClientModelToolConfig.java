package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 客户端模型-工具关联配置实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client_model_tool_config")
public class AiClientModelToolConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "model_id")
    private Long modelId;
    
    @Column(name = "tool_type", length = 20)
    private String toolType;
    
    @Column(name = "tool_id")
    private Long toolId;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "model_id", insertable = false, updatable = false)
    // private AiClientModel model;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "tool_id", insertable = false, updatable = false)
    // private AiClientToolMcp mcpTool;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
