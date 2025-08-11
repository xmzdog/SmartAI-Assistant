package com.onepiece.xmz.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务历史数据DTO
 * 
 * @author SmartAI-Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryDTO {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务描述
     */
    private String taskDescription;
    
    /**
     * 任务类型
     */
    private String taskType;
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 执行进度
     */
    private Integer progress;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
    
    /**
     * 执行结果
     */
    private String result;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 智能体名称
     */
    private String agentName;
    
    /**
     * 执行步骤列表
     */
    private List<TaskExecutionStepDTO> executionSteps;
    
    /**
     * 任务执行步骤DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskExecutionStepDTO {
        private String stepName;
        private String stepDescription;
        private String stepType;
        private String stepStatus;
        private Integer stepOrder;
        private Long executionTime;
        private LocalDateTime createTime;
    }
}


