package com.onepiece.xmz.app.agent.manus;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工具调用信息
 * 参考OpenManus的ToolCall设计
 * 
 * @author SmartAI-Assistant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    
    /**
     * 工具调用ID
     */
    private String id;
    
    /**
     * 工具名称
     */
    private String name;
    
    /**
     * 工具参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 调用状态
     */
    private ToolCallStatus status = ToolCallStatus.PENDING;
    
    /**
     * 执行结果
     */
    private String result;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executedAt;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;
    
    /**
     * 工具调用状态枚举
     */
    public enum ToolCallStatus {
        PENDING("待执行"),
        EXECUTING("执行中"),
        SUCCESS("成功"),
        FAILED("失败"),
        TIMEOUT("超时");
        
        private final String displayName;
        
        ToolCallStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 构造函数
     */
    public ToolCall(String id, String name, Map<String, Object> parameters) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
        this.createdAt = LocalDateTime.now();
        this.status = ToolCallStatus.PENDING;
    }
    
    /**
     * 标记为执行中
     */
    public void markAsExecuting() {
        this.status = ToolCallStatus.EXECUTING;
        this.executedAt = LocalDateTime.now();
    }
    
    /**
     * 标记为成功
     */
    public void markAsSuccess(String result) {
        this.status = ToolCallStatus.SUCCESS;
        this.result = result;
        if (this.executedAt != null) {
            this.executionTimeMs = java.time.Duration.between(this.executedAt, LocalDateTime.now()).toMillis();
        }
    }
    
    /**
     * 标记为失败
     */
    public void markAsFailed(String error) {
        this.status = ToolCallStatus.FAILED;
        this.error = error;
        if (this.executedAt != null) {
            this.executionTimeMs = java.time.Duration.between(this.executedAt, LocalDateTime.now()).toMillis();
        }
    }
    
    /**
     * 标记为超时
     */
    public void markAsTimeout() {
        this.status = ToolCallStatus.TIMEOUT;
        this.error = "工具调用超时";
        if (this.executedAt != null) {
            this.executionTimeMs = java.time.Duration.between(this.executedAt, LocalDateTime.now()).toMillis();
        }
    }
    
    /**
     * 检查是否完成（成功或失败）
     */
    public boolean isCompleted() {
        return status == ToolCallStatus.SUCCESS || 
               status == ToolCallStatus.FAILED || 
               status == ToolCallStatus.TIMEOUT;
    }
    
    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return status == ToolCallStatus.SUCCESS;
    }
    
    /**
     * 获取执行摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("工具调用: %s", name));
        summary.append(String.format(" | 状态: %s", status.getDisplayName()));
        
        if (executionTimeMs != null) {
            summary.append(String.format(" | 耗时: %dms", executionTimeMs));
        }
        
        if (isSuccess() && result != null) {
            String shortResult = result.length() > 100 ? 
                    result.substring(0, 100) + "..." : result;
            summary.append(String.format(" | 结果: %s", shortResult));
        }
        
        if (status == ToolCallStatus.FAILED && error != null) {
            summary.append(String.format(" | 错误: %s", error));
        }
        
        return summary.toString();
    }
    
    /**
     * 转换为JSON字符串（用于日志）
     */
    @Override
    public String toString() {
        return String.format("ToolCall{id='%s', name='%s', status=%s, executionTimeMs=%d}", 
                id, name, status, executionTimeMs);
    }
}