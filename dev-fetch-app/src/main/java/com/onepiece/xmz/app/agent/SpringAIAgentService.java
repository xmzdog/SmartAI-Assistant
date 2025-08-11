package com.onepiece.xmz.app.agent;

import com.onepiece.xmz.api.IAgentService;
import com.onepiece.xmz.api.request.TaskRequest;
import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.api.response.TaskResult;
import com.onepiece.xmz.app.agent.tools.RAGTool;
import com.onepiece.xmz.app.agent.tools.TencentMeetingTool;
import com.onepiece.xmz.app.agent.tools.PDFTool;
import com.onepiece.xmz.app.agent.tools.WebSearchTool;
import com.onepiece.xmz.app.enums.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于SpringAI ChatClient的智能Agent服务
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Service
@Primary
public class SpringAIAgentService implements IAgentService {
    
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    
    // 任务存储
    private final Map<String, TaskResult> taskStorage = new ConcurrentHashMap<>();
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    
    // Tools
    private final RAGTool ragTool;
    private final TencentMeetingTool tencentMeetingTool;
    private final PDFTool pdfTool;
    private final WebSearchTool webSearchTool;
    
    public SpringAIAgentService(
            @Autowired ChatClient.Builder chatClientBuilder,
            @Autowired @Qualifier("pgVectorStore") VectorStore vectorStore,
            @Autowired RAGTool ragTool,
            @Autowired TencentMeetingTool tencentMeetingTool,
            @Autowired PDFTool pdfTool,
            @Autowired WebSearchTool webSearchTool) {
        
        this.vectorStore = vectorStore;
        this.ragTool = ragTool;
        this.tencentMeetingTool = tencentMeetingTool;
        this.pdfTool = pdfTool;
        this.webSearchTool = webSearchTool;
        
        // 构建具有默认能力的ChatClient
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    你是一个智能的任务规划和执行助手。你的职责是：
                    
                    1. 分析用户提出的复杂任务
                    2. 将任务分解为具体的可执行步骤
                    3. 选择合适的工具来执行每个步骤
                    4. 根据执行结果决定下一步行动
                    5. 生成完整的执行报告
                    
                    你拥有以下工具能力：
                    - RAG知识库检索和问答（ragSearch, ragQA）
                    - 腾讯会议API调用（getMeetings, getMeetingDetail, downloadMeetingRecord, transcribeMeeting）
                    - PDF报告生成（generatePdfReport, generateExecutionPlanPdf, generateMeetingSummaryPdf）
                    - 网络搜索获取最新信息（webSearch）
                    
                    请始终遵循以下原则：
                    - 任务分解要具体明确
                    - 选择最合适的工具
                    - 处理错误时要有重试机制
                    - 提供详细的执行反馈
                    """)
                .defaultTools(
                        ragTool,              // RAG工具
                        tencentMeetingTool,   // 腾讯会议工具
                        pdfTool,              // PDF生成工具
                        webSearchTool         // 网络搜索工具
                )
//                .defaultAdvisors(
//                        QuestionAnswerAdvisor.builder(vectorStore)
//                                .searchRequest(SearchRequest.defaults())
//                                .build()
//                )
                .build();
    }
    
    @Override
    public Response<TaskResult> executeTask(TaskRequest request) {
        try {
            String taskId = generateTaskId();
            log.info("开始执行任务: {} - {}", taskId, request.getTaskDescription());
            
            // 创建初始任务结果
            TaskResult result = TaskResult.builder()
                    .taskId(taskId)
                    .taskDescription(request.getTaskDescription())
                    .status(TaskStatus.RUNNING.getCode())
                    .startTime(LocalDateTime.now())
                    .progress(0)
                    .build();
            
            taskStorage.put(taskId, result);
            
            if (Boolean.TRUE.equals(request.getAsync())) {
                // 异步执行
                CompletableFuture.runAsync(() -> executeTaskInternal(taskId, request));
                return Response.success(result, "任务已提交，正在异步执行");
            } else {
                // 同步执行
                TaskResult finalResult = executeTaskInternal(taskId, request);
                return Response.success(finalResult, "任务执行完成");
            }
        } catch (Exception e) {
            log.error("任务提交失败: {}", e.getMessage(), e);
            return Response.fail("任务提交失败: " + e.getMessage());
        }
    }
    
    private TaskResult executeTaskInternal(String taskId, TaskRequest request) {
        TaskResult result = taskStorage.get(taskId);
        
        try {
            // 构建执行提示
            String prompt = buildExecutionPrompt(request);
            
            // 更新进度
            updateTaskProgress(taskId, 10, "开始任务规划...");
            
            // 使用ChatClient执行任务
            String response = chatClient.prompt()
                    .user(prompt)
                    .advisors(advisor -> advisor
                            .param("task_id", taskId)
                            .param("task_type", request.getTaskType())
                            .param("parameters", request.getParameters())
                    )
                    .call()
                    .content();
            
            // 更新进度
            updateTaskProgress(taskId, 90, "任务执行完成，生成报告...");
            
            // 完成任务
            result.setStatus(TaskStatus.COMPLETED.getCode());
            result.setEndTime(LocalDateTime.now());
            result.setProgress(100);
            result.setResult(Map.of("response", response));
            
            log.info("任务执行完成: {}", taskId);
            
        } catch (Exception e) {
            log.error("任务执行失败: {} - {}", taskId, e.getMessage(), e);
            
            result.setStatus(TaskStatus.FAILED.getCode());
            result.setEndTime(LocalDateTime.now());
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Response<TaskResult> getTaskStatus(String taskId) {
        TaskResult result = taskStorage.get(taskId);
        if (result != null) {
            return Response.success(result);
        } else {
            return Response.fail("任务不存在: " + taskId);
        }
    }
    
    @Override
    public Response<String> stopTask(String taskId) {
        TaskResult result = taskStorage.get(taskId);
        if (result == null) {
            return Response.fail("任务不存在: " + taskId);
        }
        
        if (TaskStatus.COMPLETED.getCode().equals(result.getStatus()) || 
            TaskStatus.FAILED.getCode().equals(result.getStatus()) ||
            TaskStatus.CANCELLED.getCode().equals(result.getStatus())) {
            return Response.fail("任务已结束，无法停止");
        }
        
        // 更新任务状态为已取消
        result.setStatus(TaskStatus.CANCELLED.getCode());
        result.setEndTime(LocalDateTime.now());
        result.setErrorMessage("任务被用户取消");
        
        log.info("任务已停止: {}", taskId);
        return Response.success("任务已成功停止");
    }
    
    @Override
    public Response<java.util.List<String>> getSupportedTools() {
        java.util.List<String> tools = java.util.Arrays.asList(
            "RAG知识库检索工具",
            "腾讯会议API工具", 
            "PDF生成工具",
            "网络搜索工具"
        );
        return Response.success(tools);
    }
    
    public String quickKnowledgeQA(String question, String knowledge, int contextSize, String userId) {
        log.info("快速知识问答: {} - 知识库: {}", question, knowledge);
        
        try {
            return chatClient.prompt()
                    .user(question)
                    .advisors(advisor -> advisor
                            .param("knowledge_base", knowledge)
                            .param("context_size", contextSize)
                    )
                    .call()
                    .content();
                    
        } catch (Exception e) {
            log.error("知识问答失败: {}", e.getMessage(), e);
            return "抱歉，处理您的问题时出现错误：" + e.getMessage();
        }
    }
    
    public String quickGenerateReport(String title, String content, String outputPath, String userId) {
        log.info("快速生成报告: {} - {}", title, outputPath);
        
        try {
            String prompt = String.format("""
                请基于以下内容生成一份格式化的报告：
                
                标题：%s
                内容：%s
                输出路径：%s
                
                请调用PDF生成工具来创建报告文档。
                """, title, content, outputPath);
            
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
                    
        } catch (Exception e) {
            log.error("报告生成失败: {}", e.getMessage(), e);
            return "抱歉，生成报告时出现错误：" + e.getMessage();
        }
    }
    
    public Flux<String> streamResponse(String taskDescription, Map<String, Object> parameters) {
        log.info("流式处理任务: {}", taskDescription);
        
        String prompt = String.format("""
            任务描述：%s
            参数：%s
            
            请分步骤执行此任务，并实时反馈执行进度。
            """, taskDescription, parameters);
        
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
    
    private String buildExecutionPrompt(TaskRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请执行以下任务：\n\n");
        prompt.append("任务描述：").append(request.getTaskDescription()).append("\n");
        prompt.append("任务类型：").append(request.getTaskType()).append("\n");
        
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            prompt.append("任务参数：\n");
            request.getParameters().forEach((key, value) -> 
                    prompt.append("- ").append(key).append(": ").append(value).append("\n"));
        }
        
        prompt.append("\n请按照以下步骤执行：\n");
        prompt.append("1. 分析任务需求\n");
        prompt.append("2. 制定执行计划\n");
        prompt.append("3. 调用相应工具执行任务\n");
        prompt.append("4. 处理执行结果\n");
        prompt.append("5. 生成完整报告\n");
        
        return prompt.toString();
    }
    
    private void updateTaskProgress(String taskId, int progress, String message) {
        TaskResult result = taskStorage.get(taskId);
        if (result != null) {
            result.setProgress(progress);
            result.setCurrentStep(message);
            log.info("任务进度更新 [{}]: {}% - {}", taskId, progress, message);
        }
    }
    
    private String generateTaskId() {
        return "springai_task_" + System.currentTimeMillis() + "_" + taskCounter.incrementAndGet();
    }
}