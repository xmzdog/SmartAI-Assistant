package com.onepiece.xmz.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent状态数据DTO
 * 
 * @author SmartAI-Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStatusDTO {
    
    /**
     * Agent状态
     */
    private String status;
    
    /**
     * 活跃任务数量
     */
    private Integer activeTaskCount;
    
    /**
     * 系统运行时间
     */
    private String uptime;
    
    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivity;
    
    /**
     * 系统版本
     */
    private String version;
    
    /**
     * 内存使用率
     */
    private Double memoryUsage;
    
    /**
     * CPU使用率
     */
    private Double cpuUsage;
    
    /**
     * 磁盘使用率
     */
    private Double diskUsage;
    
    /**
     * 系统负载
     */
    private Double systemLoad;
    
    /**
     * 平均响应时间
     */
    private Double averageResponseTime;
}


