package com.onepiece.xmz.app.agent.controller;

import com.onepiece.xmz.api.request.TaskRequest;
import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.api.response.TaskResult;
import com.onepiece.xmz.app.agent.SpringAIAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * SpringAI Tools调用测试控制器
 * 用于测试新的Tools机制
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@RestController
@RequestMapping("/api/tools")
public class ToolsTestController {
    
    @Autowired
    private SpringAIAgentService agentService;
    
    /**
     * 测试RAG工具调用
     */
    @PostMapping("/test-rag")
    public Response<String> testRAGTools(@RequestParam String query) {
        try {
            log.info("测试RAG Tools调用: {}", query);
            
            TaskRequest request = TaskRequest.builder()
                    .taskDescription("请使用RAG工具搜索关于'" + query + "'的信息，并提供详细回答")
                    .taskType("RAG_SEARCH")
                    .async(false)
                    .build();

            Response<TaskResult> taskResultResponse = agentService.executeTask(request);

            
            return Response.success(taskResultResponse.getData().getResult().toString());
            
        } catch (Exception e) {
            log.error("RAG Tools测试失败", e);
            return Response.fail("RAG Tools测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试腾讯会议工具调用
     */
    @PostMapping("/test-meeting")
    public Response<String> testMeetingTools(@RequestParam String userId) {
        try {
            log.info("测试腾讯会议Tools调用: {}", userId);
            
            TaskRequest request = TaskRequest.builder()
                    .taskDescription("请获取用户ID为'" + userId + "'的会议列表，并获取第一个会议的详细信息")
                    .taskType("MEETING_QUERY")
                    .async(false)
                    .build();

            Response<TaskResult> taskResultResponse = agentService.executeTask(request);


            return Response.success(taskResultResponse.getData().getResult().toString());
            
        } catch (Exception e) {
            log.error("腾讯会议Tools测试失败", e);
            return Response.fail("腾讯会议Tools测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试PDF生成工具调用
     */
    @PostMapping("/test-pdf")
    public Response<String> testPDFTools(@RequestParam String title, @RequestParam String content) {
        try {
            log.info("测试PDF Tools调用: {}", title);
            
            TaskRequest request = TaskRequest.builder()
                    .taskDescription("请生成一个标题为'" + title + "'的PDF报告，内容为：" + content)
                    .taskType("PDF_GENERATION")
                    .async(false)
                    .build();
            
            Response<TaskResult> taskResultResponse = agentService.executeTask(request);


            return Response.success(taskResultResponse.getData().getResult().toString());
            
        } catch (Exception e) {
            log.error("PDF Tools测试失败", e);
            return Response.fail("PDF Tools测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试网络搜索工具调用
     */
    @PostMapping("/test-websearch")
    public Response<String> testWebSearchTools(@RequestParam String query) {
        try {
            log.info("测试网络搜索Tools调用: {}", query);
            
            TaskRequest request = TaskRequest.builder()
                    .taskDescription("请在互联网上搜索关于'" + query + "'的最新信息，并总结搜索结果")
                    .taskType("WEB_SEARCH")
                    .async(false)
                    .build();


            Response<TaskResult> taskResultResponse = agentService.executeTask(request);


            return Response.success(taskResultResponse.getData().getResult().toString());


        } catch (Exception e) {
            log.error("网络搜索Tools测试失败", e);
            return Response.fail("网络搜索Tools测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试复合任务调用（多个工具组合）
     */
    @PostMapping("/test-complex")
    public Response<String> testComplexTask(@RequestParam String topic) {
        try {
            log.info("测试复合任务Tools调用: {}", topic);
            
            TaskRequest request = TaskRequest.builder()
                    .taskDescription(String.format("""
                        请执行以下复合任务，关于主题'%s'：
                        1. 首先使用RAG工具搜索相关知识库信息
                        2. 然后使用网络搜索获取最新信息
                        3. 最后生成一个综合报告的PDF文件
                        
                        请详细执行每个步骤并提供执行结果。
                        """, topic))
                    .taskType("COMPLEX_TASK")
                    .async(false)
                    .build();


            Response<TaskResult> taskResultResponse = agentService.executeTask(request);


            return Response.success(taskResultResponse.getData().getResult().toString());


        } catch (Exception e) {
            log.error("复合任务Tools测试失败", e);
            return Response.fail("复合任务Tools测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试流式响应
     */
    @GetMapping("/test-stream")
    public Flux<String> testStreamResponse(@RequestParam String query) {
        try {
            log.info("测试流式Tools调用: {}", query);
            
            String taskDescription = "请使用可用的工具逐步分析和回答问题：" + query;
            
            return agentService.streamResponse(taskDescription, Map.of("query", query));
            
        } catch (Exception e) {
            log.error("流式Tools测试失败", e);
            return Flux.just("流式Tools测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取工具调用统计信息
     */
    @GetMapping("/stats")
    public Response<Map<String, Object>> getToolsStats() {
        try {
            // 这里可以添加工具调用统计逻辑
            Map<String, Object> stats = Map.of(
                    "message", "Tools机制已成功迁移",
                    "available_tools", Map.of(
                            "ragTool", "RAG知识库检索和问答",
                            "tencentMeetingTool", "腾讯会议API调用",
                            "pdfTool", "PDF报告生成",
                            "webSearchTool", "网络搜索"
                    ),
                    "migration_status", "completed"
            );
            
            return Response.success(stats);
            
        } catch (Exception e) {
            log.error("获取工具统计失败", e);
            return Response.fail("获取工具统计失败: " + e.getMessage());
        }
    }
}