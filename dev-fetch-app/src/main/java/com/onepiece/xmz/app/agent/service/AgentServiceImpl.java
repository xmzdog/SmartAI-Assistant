package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.api.IAgentService;
import com.onepiece.xmz.api.request.TaskRequest;
import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.api.response.TaskResult;
import com.onepiece.xmz.app.agent.executor.AgentExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Agent服务实现
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Service
public class AgentServiceImpl implements IAgentService {
    
    @Autowired
    private AgentExecutor agentExecutor;
    
    @Override
    public Response<TaskResult> executeTask(TaskRequest taskRequest) {
        try {
            log.info("接收到任务执行请求: {}", taskRequest.getTaskDescription());
            
            // 直接执行任务（简化版本，不使用TaskPlan）
            String taskId = "TASK_" + System.currentTimeMillis();
            
            if (Boolean.TRUE.equals(taskRequest.getAsync())) {
                // 异步执行
                CompletableFuture<TaskResult> future = agentExecutor.executeSimpleTask(taskRequest);
                
                // 立即返回任务ID，客户端可以通过轮询获取状态
                TaskResult initialResult = TaskResult.builder()
                        .taskId(taskId)
                        .taskDescription(taskRequest.getTaskDescription())
                        .status("RUNNING")
                        .progress(0)
                        .startTime(LocalDateTime.now())
                        .executionSteps(new ArrayList<>())
                        .build();
                
                return Response.<TaskResult>builder()
                        .code("0000")
                        .info("任务已开始异步执行")
                        .data(initialResult)
                        .build();
                        
            } else {
                // 同步执行
                CompletableFuture<TaskResult> future = agentExecutor.executeSimpleTask(taskRequest);
                TaskResult result = future.get(); // 等待执行完成
                
                return Response.<TaskResult>builder()
                        .code("0000")
                        .info("任务执行完成")
                        .data(result)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("任务执行失败: {}", e.getMessage(), e);
            
            TaskResult errorResult = TaskResult.builder()
                    .taskId("ERROR_" + System.currentTimeMillis())
                    .taskDescription(taskRequest.getTaskDescription())
                    .status("FAILED")
                    .progress(0)
                    .errorMessage(e.getMessage())
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .build();
            
            return Response.<TaskResult>builder()
                    .code("9999")
                    .info("任务执行失败: " + e.getMessage())
                    .data(errorResult)
                    .build();
        }
    }
    
    @Override
    public Response<TaskResult> getTaskStatus(String taskId) {
        try {
            log.debug("查询任务状态: {}", taskId);
            
            TaskResult taskResult = agentExecutor.getTaskStatus(taskId);
            
            if (taskResult != null) {
                return Response.<TaskResult>builder()
                        .code("0000")
                        .info("查询成功")
                        .data(taskResult)
                        .build();
            } else {
                return Response.<TaskResult>builder()
                        .code("1001")
                        .info("任务不存在")
                        .data(null)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("查询任务状态失败: {}", e.getMessage(), e);
            
            return Response.<TaskResult>builder()
                    .code("9999")
                    .info("查询失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
    
    @Override
    public Response<String> stopTask(String taskId) {
        try {
            log.info("停止任务: {}", taskId);
            
            boolean stopped = agentExecutor.stopTask(taskId);
            
            if (stopped) {
                return Response.<String>builder()
                        .code("0000")
                        .info("任务已停止")
                        .data("任务 " + taskId + " 已成功停止")
                        .build();
            } else {
                return Response.<String>builder()
                        .code("1002")
                        .info("任务停止失败，可能任务不存在或已完成")
                        .data(null)
                        .build();
            }
            
        } catch (Exception e) {
            log.error("停止任务失败: {}", e.getMessage(), e);
            
            return Response.<String>builder()
                    .code("9999")
                    .info("停止失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
    
    @Override
    public Response<List<String>> getSupportedTools() {
        try {
            log.debug("获取支持的工具列表");
            
            // 简化版本：返回硬编码的工具列表
            List<String> toolNames = new ArrayList<>();
            toolNames.add("ragSearch");
            toolNames.add("ragQA");
            toolNames.add("webSearch");
            toolNames.add("generatePdfReport");
            toolNames.add("deepReasoning");
            
            return Response.<List<String>>builder()
                    .code("0000")
                    .info("查询成功")
                    .data(toolNames)
                    .build();
                    
        } catch (Exception e) {
            log.error("获取工具列表失败: {}", e.getMessage(), e);
            
            return Response.<List<String>>builder()
                    .code("9999")
                    .info("查询失败: " + e.getMessage())
                    .data(new ArrayList<>())
                    .build();
        }
    }
}