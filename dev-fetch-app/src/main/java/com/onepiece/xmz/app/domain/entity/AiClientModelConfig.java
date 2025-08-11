package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 客户端-模型关联配置实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client_model_config")
public class AiClientModelConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "model_id", nullable = false)
    private Long modelId;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "client_id", insertable = false, updatable = false)
    // private AiClient client;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "model_id", insertable = false, updatable = false)
    // private AiClientModel model;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
