package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统监控数据实体（新增表）
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "system_monitor_data")
public class SystemMonitorData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "monitor_time", nullable = false)
    private LocalDateTime monitorTime;
    
    @Column(name = "cpu_usage")
    private Double cpuUsage;
    
    @Column(name = "memory_usage")
    private Double memoryUsage;
    
    @Column(name = "disk_usage")
    private Double diskUsage;
    
    @Column(name = "active_tasks")
    private Integer activeTasks;
    
    @Column(name = "completed_tasks_today")
    private Integer completedTasksToday;
    
    @Column(name = "failed_tasks_today")
    private Integer failedTasksToday;
    
    @Column(name = "average_response_time")
    private Double averageResponseTime;
    
    @Column(name = "system_load")
    private Double systemLoad;
    
    @Column(name = "agent_status", length = 20)
    private String agentStatus; // IDLE, BUSY, ERROR
    
    @Column(name = "uptime_seconds")
    private Long uptimeSeconds;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (monitorTime == null) {
            monitorTime = LocalDateTime.now();
        }
    }
}


