package com.onepiece.xmz.app.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务执行记录实体（新增表）
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "task_execution_record")
public class TaskExecutionRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "task_id", nullable = false, length = 64, unique = true)
    private String taskId;
    
    @Column(name = "agent_id")
    private Long agentId;
    
    @Column(name = "task_name", length = 100)
    private String taskName;
    
    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;
    
    @Column(name = "task_type", length = 50)
    private String taskType; // MANUS, AGENT, RAG等
    
    @Column(name = "status", length = 20)
    private String status; // PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    
    @Column(name = "progress")
    private Integer progress = 0;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "execution_time")
    private Long executionTime; // 执行时间（毫秒）
    
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "input_params", columnDefinition = "TEXT")
    private String inputParams; // JSON格式的输入参数
    
    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData; // JSON格式的输出数据
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
//    // 关联关系
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "agent_id", insertable = false, updatable = false)
//    private AiAgent agent;
//
//    @OneToMany(mappedBy = "taskRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<TaskExecutionLog> executionLogs;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}


