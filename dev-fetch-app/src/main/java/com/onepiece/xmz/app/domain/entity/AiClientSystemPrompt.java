package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客户端系统提示词实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client_system_prompt")
public class AiClientSystemPrompt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "prompt_name", nullable = false, length = 50)
    private String promptName;
    
    @Column(name = "prompt_content", nullable = false, columnDefinition = "TEXT")
    private String promptContent;
    
    @Column(name = "description", length = 1024)
    private String description;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @OneToMany(mappedBy = "systemPrompt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<AiClientSystemPromptConfig> promptConfigs;
    
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
