package com.onepiece.xmz.app.agent.executor;

import com.onepiece.xmz.api.request.TaskRequest;
import com.onepiece.xmz.api.response.TaskResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent执行引擎
 * 负责执行任务
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Service
public class AgentExecutor {
    
    // 任务执行历史
    private final Map<String, TaskResult> taskResults = new ConcurrentHashMap<>();
    
    /**
     * 执行简单任务
     */
    public CompletableFuture<TaskResult> executeSimpleTask(TaskRequest taskRequest) {
        return CompletableFuture.supplyAsync(() -> {
            String taskId = "TASK_" + System.currentTimeMillis();
            try {
                log.info("开始执行任务: {}", taskRequest.getTaskDescription());
                
                // 创建任务结果
                TaskResult taskResult = TaskResult.builder()
                        .taskId(taskId)
                        .taskDescription(taskRequest.getTaskDescription())
                        .status("EXECUTING")
                        .progress(0)
                        .startTime(LocalDateTime.now())
                        .executionSteps(new ArrayList<>())
                        .build();
                
                taskResults.put(taskId, taskResult);
                
                // 简化执行：直接返回成功结果
                taskResult.setStatus("COMPLETED");
                taskResult.setProgress(100);
                taskResult.setEndTime(LocalDateTime.now());
                taskResult.setResult("任务执行完成 - " + taskRequest.getTaskDescription());
                
                log.info("任务执行完成: {}", taskId);
                return taskResult;
                
            } catch (Exception e) {
                log.error("任务执行失败: {} - {}", taskId, e.getMessage(), e);
                TaskResult errorResult = TaskResult.builder()
                        .taskId(taskId)
                        .taskDescription(taskRequest.getTaskDescription())
                        .status("FAILED")
                        .progress(0)
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now())
                        .errorMessage(e.getMessage())
                        .build();
                taskResults.put(taskId, errorResult);
                return errorResult;
            }
        });
    }
    
    /**
     * 获取任务状态
     */
    public TaskResult getTaskStatus(String taskId) {
        return taskResults.get(taskId);
    }
    
    /**
     * 停止任务
     */
    public boolean stopTask(String taskId) {
        TaskResult taskResult = taskResults.get(taskId);
        if (taskResult != null && "EXECUTING".equals(taskResult.getStatus())) {
            taskResult.setStatus("CANCELLED");
            taskResult.setEndTime(LocalDateTime.now());
            taskResult.setResult("任务被用户取消");
            log.info("任务已停止: {}", taskId);
            return true;
        }
        return false;
    }
}