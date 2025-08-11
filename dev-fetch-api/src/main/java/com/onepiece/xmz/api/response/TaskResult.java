package com.onepiece.xmz.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务执行结果
 * 
 * @author SmartAI-Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResult implements Serializable {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务状态：PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
     */
    private String status;
    
    /**
     * 执行进度 (0-100)
     */
    private Integer progress;
    
    /**
     * 当前执行步骤描述
     */
    private String currentStep;
    
    /**
     * 任务描述
     */
    private String taskDescription;
    
    /**
     * 执行结果
     */
    private Object result;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行步骤记录
     */
    private List<ExecutionStep> executionSteps;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 使用的工具列表
     */
    private List<String> usedTools;
    
    /**
     * 生成的文件路径（如PDF等）
     */
    private List<String> generatedFiles;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> metadata;
    
    /**
     * 执行步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionStep implements Serializable {
        private Integer stepIndex;
        private String stepName;
        private String stepDescription;
        private String status;
        private Object result;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long duration;
        private String toolUsed;
    }
}