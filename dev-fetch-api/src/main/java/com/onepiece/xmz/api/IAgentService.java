package com.onepiece.xmz.api;

import com.onepiece.xmz.api.request.TaskRequest;
import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.api.response.TaskResult;

/**
 * 智能Agent服务接口
 * 提供任务分解、自主规划、工具调用等核心能力
 * 
 * @author SmartAI-Assistant
 */
public interface IAgentService {
    
    /**
     * 执行复杂任务
     * @param taskRequest 任务请求，包含目标描述和参数
     * @return 任务执行结果
     */
    Response<TaskResult> executeTask(TaskRequest taskRequest);
    
    /**
     * 查询任务执行状态
     * @param taskId 任务ID
     * @return 任务状态和进度
     */
    Response<TaskResult> getTaskStatus(String taskId);
    
    /**
     * 停止正在执行的任务
     * @param taskId 任务ID
     * @return 操作结果
     */
    Response<String> stopTask(String taskId);
    
    /**
     * 获取支持的工具列表
     * @return 工具列表
     */
    Response<java.util.List<String>> getSupportedTools();
}