package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AI智能体任务调度配置实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_agent_task_schedule")
public class AiAgentTaskSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "agent_id", nullable = false)
    private Long agentId;
    
    @Column(name = "task_name", length = 64)
    private String taskName;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;
    
    @Column(name = "task_param", columnDefinition = "TEXT")
    private String taskParam;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "agent_id", insertable = false, updatable = false)
    // private AiAgent agent;
    
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
