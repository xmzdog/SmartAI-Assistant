package com.onepiece.xmz.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务统计数据DTO
 * 
 * @author SmartAI-Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticsDTO {
    
    /**
     * 总任务数
     */
    private Long totalTasks;
    
    /**
     * 已完成任务数
     */
    private Long completedTasks;
    
    /**
     * 失败任务数
     */
    private Long failedTasks;
    
    /**
     * 运行中任务数
     */
    private Long runningTasks;
    
    /**
     * 等待中任务数
     */
    private Long pendingTasks;
    
    /**
     * 今日任务数
     */
    private Long todayTasks;
    
    /**
     * 今日完成任务数
     */
    private Long todayCompletedTasks;
    
    /**
     * 今日失败任务数
     */
    private Long todayFailedTasks;
    
    /**
     * 平均执行时间（毫秒）
     */
    private Double averageExecutionTime;
    
    /**
     * 成功率（百分比）
     */
    private Double successRate;
    
    /**
     * 系统负载
     */
    private Double systemLoad;
}


