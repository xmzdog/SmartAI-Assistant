package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务执行日志实体（新增表）
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "task_execution_log")
public class TaskExecutionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "task_record_id", nullable = false)
    private Long taskRecordId;
    
    @Column(name = "step_name", length = 100)
    private String stepName;
    
    @Column(name = "step_description", length = 500)
    private String stepDescription;
    
    @Column(name = "step_type", length = 50)
    private String stepType; // THINKING, ACTION, OBSERVATION, TOOL_CALL等
    
    @Column(name = "step_status", length = 20)
    private String stepStatus; // SUCCESS, FAILED, RUNNING
    
    @Column(name = "step_order")
    private Integer stepOrder;
    
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;
    
    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "execution_time")
    private Long executionTime; // 步骤执行时间（毫秒）
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "task_record_id", insertable = false, updatable = false)
    // private TaskExecutionRecord taskRecord;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
