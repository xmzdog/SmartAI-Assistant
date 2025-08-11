package com.onepiece.xmz.onepiece.trigger.http;


import com.onepiece.xmz.api.IAgentService;
import com.onepiece.xmz.api.request.TaskRequest;
import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.api.response.TaskResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 智能Agent控制器
 * 提供任务执行、状态查询、工具管理等API
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/agent/")
@Tag(name = "智能Agent", description = "智能Agent任务执行和管理API")
public class AgentController implements IAgentService {
    
    @Resource
    private IAgentService agentService;
    
    /**
     * 执行复杂任务
     * 
     * POST /api/v1/agent/execute
     * Content-Type: application/json
     * 
     * 请求体示例:
     * {
     *   "taskDescription": "获取最近一周的会议纪要，切片入库，并生成会议总结PDF报告",
     *   "taskType": "meeting_analysis",
     *   "parameters": {
     *     "startDate": "2024-01-01",
     *     "endDate": "2024-01-07",
     *     "knowledgeBase": "meeting_minutes"
     *   },
     *   "priority": 2,
     *   "timeoutMinutes": 60,
     *   "userId": "user123",
     *   "async": true
     * }
     */
    @PostMapping("execute")
    @Operation(summary = "执行复杂任务", description = "Agent将自动分解任务并选择合适的工具执行")
    @Override
    public Response<TaskResult> executeTask(@RequestBody TaskRequest taskRequest) {
        log.info("接收任务执行请求 - 描述: {}, 类型: {}", 
                taskRequest.getTaskDescription(), taskRequest.getTaskType());
        
        // 参数验证
        if (taskRequest.getTaskDescription() == null || taskRequest.getTaskDescription().trim().isEmpty()) {
            return Response.<TaskResult>builder()
                    .code("1001")
                    .info("任务描述不能为空")
                    .data(null)
                    .build();
        }
        
        // 设置默认值
        if (taskRequest.getAsync() == null) {
            taskRequest.setAsync(true); // 默认异步执行
        }
        if (taskRequest.getPriority() == null) {
            taskRequest.setPriority(2); // 默认中等优先级
        }
        if (taskRequest.getTimeoutMinutes() == null) {
            taskRequest.setTimeoutMinutes(30); // 默认30分钟超时
        }
        
        return agentService.executeTask(taskRequest);
    }
    
    /**
     * 查询任务执行状态
     * 
     * GET /api/v1/agent/status/{taskId}
     */
    @GetMapping("status/{taskId}")
    @Operation(summary = "查询任务状态", description = "获取指定任务的执行状态和进度")
    @Override
    public Response<TaskResult> getTaskStatus(
            @Parameter(description = "任务ID") @PathVariable("taskId") String taskId) {
        log.debug("查询任务状态: {}", taskId);
        return agentService.getTaskStatus(taskId);
    }
    
    /**
     * 停止正在执行的任务
     * 
     * DELETE /api/v1/agent/stop/{taskId}
     */
    @DeleteMapping("stop/{taskId}")
    @Operation(summary = "停止任务", description = "停止指定的正在执行的任务")
    @Override
    public Response<String> stopTask(
            @Parameter(description = "任务ID") @PathVariable("taskId") String taskId) {
        log.info("停止任务: {}", taskId);
        return agentService.stopTask(taskId);
    }
    
    /**
     * 获取支持的工具列表
     * 
     * GET /api/v1/agent/tools
     */
    @GetMapping("tools")
    @Operation(summary = "获取支持的工具", description = "获取Agent支持的所有工具列表")
    @Override
    public Response<List<String>> getSupportedTools() {
        log.debug("获取支持的工具列表");
        return agentService.getSupportedTools();
    }
    
    /**
     * 快速执行会议分析任务
     * 
     * POST /api/v1/agent/quick/meeting-analysis
     */
    @PostMapping("quick/meeting-analysis")
    @Operation(summary = "快速会议分析", description = "快速执行会议获取、转录、切片入库和报告生成任务")
    public Response<TaskResult> quickMeetingAnalysis(
            @RequestBody Map<String, Object> params) {
        
        log.info("快速会议分析请求: {}", params);
        
        // 构建任务请求
        TaskRequest taskRequest = TaskRequest.builder()
                .taskDescription(buildMeetingAnalysisDescription(params))
                .taskType("meeting_analysis")
                .parameters(params)
                .priority(2)
                .timeoutMinutes(90)
                .async(true)
                .userId((String) params.getOrDefault("userId", "anonymous"))
                .build();
        
        return executeTask(taskRequest);
    }
    
    /**
     * 快速执行知识库问答
     * 
     * POST /api/v1/agent/quick/knowledge-qa
     */
    @PostMapping("quick/knowledge-qa")
    @Operation(summary = "快速知识库问答", description = "基于指定知识库回答问题")
    public Response<TaskResult> quickKnowledgeQA(@RequestBody Map<String, Object> params) {
        
        log.info("快速知识库问答请求: {}", params);
        
        String question = (String) params.get("question");
        String knowledge = (String) params.getOrDefault("knowledge", "");
        
        if (question == null || question.trim().isEmpty()) {
            return Response.<TaskResult>builder()
                    .code("1001")
                    .info("问题不能为空")
                    .data(null)
                    .build();
        }
        
        TaskRequest taskRequest = TaskRequest.builder()
                .taskDescription("基于知识库 '" + knowledge + "' 回答问题: " + question)
                .taskType("knowledge_qa")
                .parameters(params)
                .priority(1)
                .timeoutMinutes(10)
                .async(false) // 问答通常同步返回
                .userId((String) params.getOrDefault("userId", "anonymous"))
                .build();
        
        return executeTask(taskRequest);
    }
    
    /**
     * 快速生成报告
     * 
     * POST /api/v1/agent/quick/generate-report
     */
    @PostMapping("quick/generate-report")
    @Operation(summary = "快速生成报告", description = "根据指定内容生成PDF报告")
    public Response<TaskResult> quickGenerateReport(@RequestBody Map<String, Object> params) {
        
        log.info("快速生成报告请求: {}", params);
        
        String title = (String) params.getOrDefault("title", "智能助手报告");
        String content = (String) params.get("content");
        
        if (content == null || content.trim().isEmpty()) {
            return Response.<TaskResult>builder()
                    .code("1001")
                    .info("报告内容不能为空")
                    .data(null)
                    .build();
        }
        
        TaskRequest taskRequest = TaskRequest.builder()
                .taskDescription("生成PDF报告: " + title)
                .taskType("report_generation")
                .parameters(params)
                .priority(1)
                .timeoutMinutes(15)
                .async(true)
                .userId((String) params.getOrDefault("userId", "anonymous"))
                .build();
        
        return executeTask(taskRequest);
    }
    
    /**
     * 构建会议分析任务描述
     */
    private String buildMeetingAnalysisDescription(Map<String, Object> params) {
        StringBuilder desc = new StringBuilder("执行完整的会议分析流程: ");
        
        String startDate = (String) params.get("startDate");
        String endDate = (String) params.get("endDate");
        String knowledge = (String) params.getOrDefault("knowledge", "meeting_minutes");
        
        if (startDate != null && endDate != null) {
            desc.append("1. 获取").append(startDate).append("到").append(endDate).append("的会议列表; ");
        } else {
            desc.append("1. 获取最近的会议列表; ");
        }
        
        desc.append("2. 下载会议录制文件; ");
        desc.append("3. 转录会议内容为文本; ");
        desc.append("4. 切片文本并存入知识库'").append(knowledge).append("'; ");
        desc.append("5. 基于会议内容生成总结报告; ");
        desc.append("6. 生成执行计划PDF文档。");
        
        return desc.toString();
    }
}