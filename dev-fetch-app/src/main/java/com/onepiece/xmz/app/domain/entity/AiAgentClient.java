package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 智能体-客户端关联实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_agent_client")
public class AiAgentClient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "agent_id", nullable = false)
    private Long agentId;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "sequence", nullable = false)
    private Integer sequence;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "agent_id", insertable = false, updatable = false)
    // private AiAgent agent;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "client_id", insertable = false, updatable = false)
    // private AiClient client;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
