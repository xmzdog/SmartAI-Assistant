package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客户端模型配置实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client_model")
public class AiClientModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "model_name", nullable = false, length = 50)
    private String modelName;
    
    @Column(name = "base_url", nullable = false, length = 255)
    private String baseUrl;
    
    @Column(name = "api_key", nullable = false, length = 255)
    private String apiKey;
    
    @Column(name = "completions_path", length = 100)
    private String completionsPath = "v1/chat/completions";
    
    @Column(name = "embeddings_path", length = 100)
    private String embeddingsPath = "v1/embeddings";
    
    @Column(name = "model_type", nullable = false, length = 50)
    private String modelType;
    
    @Column(name = "model_version", length = 50)
    private String modelVersion = "gpt-4.1";
    
    @Column(name = "timeout")
    private Integer timeout = 180;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<AiClientModelConfig> modelConfigs;
    
    // @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<AiClientModelToolConfig> toolConfigs;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
