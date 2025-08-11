package com.onepiece.xmz.app.enums;

/**
 * 任务状态枚举
 * 
 * @author SmartAI-Assistant
 */
public enum TaskStatus {
    /**
     * 等待中
     */
    PENDING("PENDING", "等待中"),
    
    /**
     * 运行中
     */
    RUNNING("RUNNING", "运行中"),
    
    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),
    
    /**
     * 失败
     */
    FAILED("FAILED", "失败"),
    
    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String description;
    
    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的任务状态代码: " + code);
    }
}