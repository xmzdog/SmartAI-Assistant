package com.onepiece.xmz.app.agent.manus;

import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.app.agent.service.TaskManagementService;
import com.onepiece.xmz.app.agent.service.SystemMonitorService;
import com.onepiece.xmz.app.domain.dto.AgentStatusDTO;
import com.onepiece.xmz.app.domain.dto.TaskHistoryDTO;
import com.onepiece.xmz.app.domain.dto.TaskStatisticsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ManusAgent控制器
 * 提供HTTP API接口访问ManusAgent功能
 * 
 * @author SmartAI-Assistant
 */
@RestController
@RequestMapping("/api/v1/manus")
@Tag(name = "ManusAgent", description = "通用多功能智能体接口")
public class ManusAgentController {
    
    private static final Logger log = LoggerFactory.getLogger(ManusAgentController.class);
    
    @Autowired
    private ManusAgentService manusAgentService;
    
    @Autowired
    private TaskManagementService taskManagementService;
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    /**
     * 任务执行请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskRequest {
        private String task;
        private String description;
        private boolean async = false;
        private String taskId;
        private String strategy;
        
        public String getTask() {
            return task;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isAsync() {
            return async;
        }
        
        public String getTaskId() {
            return taskId;
        }
        
        public String getStrategy() {
            return strategy;
        }
    }
    
    /**
     * 深度推理请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasoningRequest {
        private String problem;
        private String strategy = "COMPREHENSIVE";
        
        public String getProblem() {
            return problem;
        }
        
        public String getStrategy() {
            return strategy;
        }
    }
    
    /**
     * 配置请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigRequest {
        private String property;
        private String value;
        
        public String getProperty() {
            return property;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * 执行任务
     */
    @PostMapping("/execute")
    @Operation(summary = "执行ManusAgent任务", description = "使用ManusAgent执行复杂任务")
    public Response<String> executeTask(@RequestBody TaskRequest request) {
        log.info("收到任务执行请求: {}", request.getDescription());
        
        try {
            if (Boolean.TRUE.equals(request.isAsync())) {
                // 异步执行
                String taskId = UUID.randomUUID().toString();
                
                String result = manusAgentService.executeTaskAsync(request.getTask(), taskId);
                return Response.success(result, "任务已开始异步执行");
            } else {
                // 同步执行
                String result = manusAgentService.executeTask(request.getTask());
                return Response.success(result, "任务执行完成");
            }
        } catch (Exception e) {
            log.error("任务执行失败: {}", e.getMessage(), e);
            return Response.fail("任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 快速执行任务（GET方式）
     */
    @GetMapping("/execute")
    @Operation(summary = "快速执行任务", description = "通过GET请求快速执行简单任务")
    public Response<String> quickExecute(
            @Parameter(description = "任务描述") @RequestParam String task) {
        
        log.info("快速执行任务: {}", task);
        
        try {
            String result = manusAgentService.executeTask(task);
            return Response.success(result, "任务执行完成");
        } catch (Exception e) {
            log.error("快速任务执行失败: {}", e.getMessage(), e);
            return Response.fail("任务执行失败: " + e.getMessage());
        }
    }
    


    /**
     * 获取任务状态
     */
    @GetMapping("/task/{taskId}/status")
    @Operation(summary = "获取任务状态", description = "查询异步任务的执行状态")
    public Response<Map<String, Object>> getTaskStatus(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        
        try {
            Map<String, Object> status = manusAgentService.getTaskStatusDetail(taskId);
            return Response.success(status, "获取状态成功");
        } catch (Exception e) {
            log.error("获取任务状态失败: {}", e.getMessage(), e);
            return Response.fail("获取状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务结果
     */
    @GetMapping("/task/{taskId}/result")
    @Operation(summary = "获取任务结果", description = "获取异步任务的执行结果")
    public Response<String> getTaskResult(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        
        try {
            String result = manusAgentService.getTaskResult(taskId);
            return Response.success(result, "获取结果成功");
        } catch (Exception e) {
            log.error("获取任务结果失败: {}", e.getMessage(), e);
            return Response.fail("获取结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消任务
     */
    @DeleteMapping("/task/{taskId}")
    @Operation(summary = "取消任务", description = "取消正在执行的异步任务")
    public Response<String> cancelTask(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        
        try {
            String result = manusAgentService.cancelTask(taskId);
            return Response.success(result, "任务取消成功");
        } catch (Exception e) {
            log.error("取消任务失败: {}", e.getMessage(), e);
            return Response.fail("取消任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 深度推理
     */
    @PostMapping("/reasoning")
    @Operation(summary = "深度推理", description = "使用CoT进行深度推理分析")
    public Response<String> performReasoning(@RequestBody ReasoningRequest request) {
        log.info("执行深度推理: {}", request.getProblem());
        
        try {
            String result = manusAgentService.performDeepReasoning(
                    request.getProblem(), request.getStrategy());
            return Response.success(result, "深度推理完成");
        } catch (Exception e) {
            log.error("深度推理失败: {}", e.getMessage(), e);
            return Response.fail("深度推理失败: " + e.getMessage());
        }
    }
    
    /**
     * 快速推理（GET方式）
     */
    @GetMapping("/reasoning")
    @Operation(summary = "快速推理", description = "通过GET请求进行快速推理")
    public Response<String> quickReasoning(
            @Parameter(description = "问题") @RequestParam String problem,
            @Parameter(description = "策略") @RequestParam(defaultValue = "COMPREHENSIVE") String strategy) {
        
        try {
            String result = manusAgentService.performDeepReasoning(problem, strategy);
            return Response.success(result, "推理完成");
        } catch (Exception e) {
            log.error("快速推理失败: {}", e.getMessage(), e);
            return Response.fail("推理失败: " + e.getMessage());
        }
    }
    

    
    /**
     * 配置Agent
     */
    @PostMapping("/config")
    @Operation(summary = "配置Agent", description = "修改ManusAgent的配置参数")
    public Response<String> configureAgent(@RequestBody ConfigRequest request) {
        log.info("配置Agent: {} = {}", request.getProperty(), request.getValue());
        
        try {
            String result = manusAgentService.configureAgent(
                    request.getProperty(), request.getValue());
            return Response.success(result, "配置成功");
        } catch (Exception e) {
            log.error("配置Agent失败: {}", e.getMessage(), e);
            return Response.fail("配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取运行统计
     */
    @GetMapping("/running-stats")
    @Operation(summary = "获取运行统计", description = "获取ManusAgent的运行统计信息")
    public Response<String> getRunningStatistics() {
        try {
            String stats = manusAgentService.getRunningStatistics();
            return Response.success(stats, "获取统计成功");
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            return Response.fail("获取统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 重启Agent
     */
    @PostMapping("/restart")
    @Operation(summary = "重启Agent", description = "重启ManusAgent，清理所有状态")
    public Response<String> restartAgent() {
        log.info("重启ManusAgent");
        
        try {
            String result = manusAgentService.restartAgent();
            return Response.success(result, "重启成功");
        } catch (Exception e) {
            log.error("重启Agent失败: {}", e.getMessage(), e);
            return Response.fail("重启失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期结果
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理过期结果", description = "清理过期的任务结果缓存")
    public Response<String> cleanupResults() {
        try {
            String result = manusAgentService.cleanupExpiredResults();
            return Response.success(result, "清理成功");
        } catch (Exception e) {
            log.error("清理过期结果失败: {}", e.getMessage(), e);
            return Response.fail("清理失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查ManusAgent服务是否正常")
    public Response<String> healthCheck() {
        return Response.success("ManusAgent服务正常运行", "健康检查通过");
    }
    
    /**
     * 获取支持的推理策略
     */
    @GetMapping("/reasoning/strategies")
    @Operation(summary = "获取推理策略", description = "获取支持的CoT推理策略列表")
    public Response<String> getReasoningStrategies() {
        StringBuilder strategies = new StringBuilder();
        strategies.append("支持的CoT推理策略:\n");
        
        for (CoTReasoning.ReasoningStrategy strategy : CoTReasoning.ReasoningStrategy.values()) {
            strategies.append("- ").append(strategy.name()).append(": ")
                    .append(strategy.getDisplayName()).append("\n");
        }
        
        return Response.success(strategies.toString(), "获取策略列表成功");
    }
    
    /**
     * 获取支持的工具选择策略
     */
    @GetMapping("/tools/strategies")
    @Operation(summary = "获取工具策略", description = "获取支持的工具选择策略列表")
    public Response<String> getToolStrategies() {
        StringBuilder strategies = new StringBuilder();
        strategies.append("支持的工具选择策略:\n");
        
        for (ToolCallAgent.ToolSelectionStrategy strategy : ToolCallAgent.ToolSelectionStrategy.values()) {
            strategies.append("- ").append(strategy.name()).append(": ")
                    .append(strategy.getDisplayName()).append("\n");
        }
        
        return Response.success(strategies.toString(), "获取策略列表成功");
    }
    
    /**
     * 获取Agent状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取Agent状态", description = "获取ManusAgent运行状态")
    public Response<AgentStatusDTO> getStatus() {
        try {
            // 记录Agent活动
            systemMonitorService.recordAgentActivity();
            
            // 获取实时状态数据
            Response<AgentStatusDTO> statusResponse = systemMonitorService.getCurrentAgentStatus();
            return statusResponse;
        } catch (Exception e) {
            log.error("获取Agent状态失败: {}", e.getMessage(), e);
            return Response.fail("获取状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务历史
     */
    @GetMapping("/tasks")
    @Operation(summary = "获取任务历史", description = "获取任务执行历史列表")
    public Response<Page<TaskHistoryDTO>> getTaskHistory(
            @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "状态筛选") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "任务类型") @RequestParam(name = "taskType", required = false) String taskType) {
        
        try {
            // 使用数据库任务管理服务
            return taskManagementService.getTaskHistory(page, size, status, taskType);
        } catch (Exception e) {
            log.error("获取任务历史失败: {}", e.getMessage(), e);
            return Response.fail("获取任务历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取任务执行统计数据")
    public Response<TaskStatisticsDTO> getStatistics() {
        try {
            // 使用数据库任务管理服务
            return taskManagementService.getTaskStatistics();
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            return Response.fail("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "获取任务详情", description = "获取指定任务的详细信息")
    public Response<Map<String, Object>> getTaskDetail(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        
        try {
            Map<String, Object> taskDetail = manusAgentService.getTaskDetail(taskId);
            return Response.success(taskDetail, "获取任务详情成功");
        } catch (Exception e) {
            log.error("获取任务详情失败: {}", e.getMessage(), e);
            return Response.fail("获取任务详情失败: " + e.getMessage());
        }
    }
}