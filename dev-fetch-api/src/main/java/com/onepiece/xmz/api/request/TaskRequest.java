package com.onepiece.xmz.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 任务请求对象
 * 
 * @author SmartAI-Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest implements Serializable {
    
    /**
     * 任务描述，自然语言描述要完成的目标
     */
    private String taskDescription;
    
    /**
     * 任务类型，如：meeting_analysis, document_processing, report_generation
     */
    private String taskType;
    
    /**
     * 任务参数，键值对形式的额外参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 优先级：1-低，2-中，3-高
     */
    private Integer priority;
    
    /**
     * 超时时间（分钟）
     */
    private Integer timeoutMinutes;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 是否异步执行
     */
    private Boolean async;
}