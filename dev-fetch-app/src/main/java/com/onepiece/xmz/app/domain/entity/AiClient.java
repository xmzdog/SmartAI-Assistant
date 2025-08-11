package com.onepiece.xmz.app.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客户端配置实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client")
public class AiClient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "client_name", nullable = false, length = 50)
    private String clientName;
    
    @Column(name = "description", length = 1024)
    private String description;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
//    // 关联关系
//    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<AiAgentClient> agentClients;
//
//    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<AiClientAdvisorConfig> advisorConfigs;
//
//    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<AiClientModelConfig> modelConfigs;
//
//    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<AiClientSystemPromptConfig> promptConfigs;
//
//    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<AiClientToolConfig> toolConfigs;
    
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


