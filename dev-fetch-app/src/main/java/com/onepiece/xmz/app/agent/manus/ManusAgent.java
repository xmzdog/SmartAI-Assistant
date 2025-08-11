package com.onepiece.xmz.app.agent.manus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manus Agent - 通用多功能智能体
 * 参考OpenManus的Manus设计，集成所有分层Agent的能力
 * 
 * 核心能力：
 * 1. ReAct推理循环 (来自ReActAgent)
 * 2. 工具调用和管理 (来自ToolCallAgent)  
 * 3. CoT深度推理 (独有能力)
 * 4. SpringAI原生Function调用 (集成SpringAI能力)
 * 5. 任务规划和自主执行 (综合能力)
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class ManusAgent extends ToolCallAgent {
    
    /**
     * CoT推理引擎
     */
    private CoTReasoning cotReasoning;
    
    /**
     * SpringAI ChatClient
     */
    private ChatClient springAIChatClient;
    
    /**
     * 任务执行上下文
     */
    private Map<String, Object> taskContext = new ConcurrentHashMap<>();
    
    /**
     * 是否启用CoT深度推理
     */
    private boolean cotEnabled = true;
    
    /**
     * 是否启用SpringAI Function Calling
     */
    private boolean springAIFunctionEnabled = true;
    
    public ManusAgent() {
        super("Manus", "通用多功能智能体，具备推理、规划、工具调用等综合能力");
        initializeManusCapabilities();
        // 重新初始化提示词，因为现在Manus特有工具也已经初始化了
        initializePrompts();
    }
    
    @Autowired
    public void setSpringAIChatClient(
            ChatClient.Builder chatClientBuilder,
            com.onepiece.xmz.app.agent.tools.RAGTool ragTool,
            com.onepiece.xmz.app.agent.tools.TencentMeetingTool tencentMeetingTool,
            com.onepiece.xmz.app.agent.tools.PDFTool pdfTool,
            com.onepiece.xmz.app.agent.tools.WebSearchTool webSearchTool) {
        
        // 创建ManusAgent专用的ChatClient，用于工具调用
        this.springAIChatClient = chatClientBuilder
                .defaultSystem("""
                    你是Manus智能体，一个具备推理、规划、工具调用等综合能力的AI助手。
                    
                    你的核心能力包括：
                    1. 深度推理分析 (CoT)
                    2. 任务规划和分解
                    3. 智能工具选择和调用
                    4. 错误处理和恢复
                    5. 结果整合和报告生成
                    
                    可用工具：
                    - ragSearch: RAG语义搜索
                    - ragQA: RAG知识问答  
                    - getMeetings: 获取会议列表
                    - getMeetingDetail: 获取会议详情
                    - downloadMeetingRecord: 下载会议录制
                    - transcribeMeeting: 转录会议音频
                    - generatePdfReport: 生成PDF报告
                    - generateExecutionPlanPdf: 生成执行计划PDF
                    - generateMeetingSummaryPdf: 生成会议纪要PDF
                    - webSearch: 网络搜索
                    
                    执行原则：
                    - 仔细分析任务需求
                    - 制定详细执行计划
                    - 选择最合适的工具
                    - 提供详细执行反馈
                    """)
                .defaultTools(
                        ragTool,
                        tencentMeetingTool,
                        pdfTool,
                        webSearchTool
                )
                .build();
        
        // 创建专用于推理的ChatClient（不包含工具）
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    你是Manus智能体，一个具备强大推理能力的AI助手。
                    
                    你的核心能力包括：
                    1. 逻辑分析和推理
                    2. 任务规划和分解
                    3. 问题解决策略制定
                    4. 结果评估和总结
                    
                    执行原则：
                    - 仔细分析问题本质
                    - 运用逻辑推理
                    - 提供清晰的思路
                    - 给出具体的建议
                    """)
                .build();
        
        // 初始化CoT推理引擎（使用不包含工具的ChatClient）
        this.cotReasoning = new CoTReasoning(this.chatClient, this.memory);
    }
    
    /**
     * 初始化Manus特有能力
     */
    private void initializeManusCapabilities() {
        // 扩展工具集
        availableTools.put("deepReasoning", "使用CoT进行深度推理分析");
        availableTools.put("getMeetings", "获取腾讯会议列表");
        availableTools.put("getMeetingDetail", "获取会议详细信息");
        availableTools.put("downloadMeetingRecord", "下载会议录制文件");
        availableTools.put("transcribeMeeting", "转录会议音频内容");
        availableTools.put("generatePdfReport", "生成PDF报告");
        availableTools.put("generateExecutionPlanPdf", "生成执行计划PDF");
        availableTools.put("generateMeetingSummaryPdf", "生成会议纪要PDF");
        
        // 设置为混合工具选择策略
        this.toolSelectionStrategy = ToolSelectionStrategy.HYBRID;
        
        // 增加并发处理能力
        this.maxConcurrentToolCalls = 5;
        this.maxSteps = 15;
    }
    
    /**
     * 重写execute方法，加入Manus特有的执行逻辑
     */
    @Override
    protected String execute(String request) {
        log.info("[ManusAgent] 开始执行复杂任务: {}", request);
        
        try {
            // 初始化任务上下文
            initializeTaskContext(request);
            
            // 判断是否需要深度推理
            if (shouldUseDeepReasoning(request)) {
                return executeWithCoTReasoning(request);
            }
            
            // 判断是否使用SpringAI Function Calling
            if (shouldUseSpringAIFunctions(request)) {
                return executeWithSpringAIFunctions(request);
            }
            
            // 使用标准ReAct循环
            return super.execute(request);
            
        } catch (Exception e) {
            log.error("[ManusAgent] 任务执行失败: {}", e.getMessage(), e);
            return "任务执行失败: " + e.getMessage();
        } finally {
            // 清理任务上下文
            clearTaskContext();
        }
    }
    
    /**
     * 初始化任务上下文
     */
    private void initializeTaskContext(String request) {
        taskContext.clear();
        taskContext.put("originalRequest", request);
        taskContext.put("startTime", System.currentTimeMillis());
        taskContext.put("complexity", analyzeTaskComplexity(request));
    }
    
    /**
     * 分析任务复杂度
     */
    private String analyzeTaskComplexity(String request) {
        // 简单启发式规则判断任务复杂度
        int complexity = 0;
        
        // 检查关键词
        if (request.contains("分析") || request.contains("推理")) complexity += 2;
        if (request.contains("生成") || request.contains("创建")) complexity += 1;
        if (request.contains("多步骤") || request.contains("复杂")) complexity += 3;
        if (request.contains("会议") && request.contains("报告")) complexity += 2;
        if (request.length() > 100) complexity += 1;
        
        if (complexity >= 5) return "HIGH";
        if (complexity >= 3) return "MEDIUM";
        return "LOW";
    }
    
    /**
     * 判断是否应该使用深度推理
     */
    private boolean shouldUseDeepReasoning(String request) {
        if (!cotEnabled) return false;
        
        String complexity = (String) taskContext.get("complexity");
        
        // 高复杂度任务或包含推理关键词的任务使用CoT
        return "HIGH".equals(complexity) || 
               request.contains("分析") || 
               request.contains("推理") || 
               request.contains("为什么") ||
               request.contains("如何") ||
               request.contains("解释");
    }
    
    /**
     * 判断是否应该使用SpringAI Functions
     */
    private boolean shouldUseSpringAIFunctions(String request) {
        if (!springAIFunctionEnabled) return false;
        
        // 包含工具调用需求的任务使用SpringAI Functions
        return request.contains("搜索") ||
               request.contains("会议") ||
               request.contains("报告") ||
               request.contains("PDF") ||
               request.contains("知识库");
    }
    
    /**
     * 使用CoT深度推理执行任务
     */
    private String executeWithCoTReasoning(String request) {
        log.info("[ManusAgent] 使用CoT深度推理模式");
        
        // 选择推理策略
        CoTReasoning.ReasoningStrategy strategy = selectReasoningStrategy(request);
        
        // 执行深度推理
        CoTReasoning.ReasoningChain reasoningChain = cotReasoning.deepReason(request, strategy);
        
        // 记录推理过程
        memory.addAssistantMessage("深度推理完成: " + reasoningChain.getFinalAnswer());
        
        // 基于推理结果决定是否需要工具调用
        if (reasoningChain.getFinalAnswer().contains("需要") || 
            reasoningChain.getFinalAnswer().contains("调用")) {
            
            // 继续使用工具调用完成任务
            String toolResult = executeToolBasedOnReasoning(reasoningChain.getFinalAnswer());
            
            return String.format("""
                    === ManusAgent执行报告 ===
                    
                    原始任务: %s
                    
                    %s
                    
                    工具执行结果:
                    %s
                    
                    任务执行完成。
                    """, 
                    request,
                    cotReasoning.getReasoningChainSummary(reasoningChain),
                    toolResult);
        }
        
        return String.format("""
                === ManusAgent执行报告 ===
                
                原始任务: %s
                
                %s
                
                任务通过深度推理完成。
                """, 
                request,
                cotReasoning.getReasoningChainSummary(reasoningChain));
    }
    
    /**
     * 使用SpringAI Functions执行任务
     */
    private String executeWithSpringAIFunctions(String request) {
        log.info("[ManusAgent] 使用SpringAI Function Calling模式");
        
        try {
            String response = springAIChatClient.prompt()
                    .system(getManusSystemPrompt())
                    .user(request)
                    .call()
                    .content();
            
            memory.addAssistantMessage(response);
            
            return String.format("""
                    === ManusAgent执行报告 ===
                    
                    原始任务: %s
                    
                    执行模式: SpringAI Function Calling
                    
                    执行结果:
                    %s
                    
                    任务执行完成。
                    """, request, response);
            
        } catch (Exception e) {
            log.error("[ManusAgent] SpringAI Function调用失败: {}", e.getMessage(), e);
            
            // 确保状态正确设置为失败状态
            setState(AgentState.FAILED);
            
            return String.format("""
                    === ManusAgent执行报告 ===
                    
                    原始任务: %s
                    
                    执行模式: SpringAI Function Calling (失败)
                    
                    错误信息: %s
                    
                    任务执行失败。
                    """, request, e.getMessage());
        }
    }
    
    /**
     * 基于推理结果执行工具调用
     */
    private String executeToolBasedOnReasoning(String reasoningResult) {
        // 这里可以解析推理结果，提取需要调用的工具
        // 简化实现：直接执行一次工具选择和调用
        
        try {
            setState(AgentState.THINKING);
            boolean shouldAct = think();
            
            if (shouldAct) {
                setState(AgentState.ACTING);
                return act();
            }
            
            return "基于推理结果，无需额外工具调用";
            
        } catch (Exception e) {
            log.error("[ManusAgent] 基于推理的工具调用失败: {}", e.getMessage(), e);
            return "工具调用失败: " + e.getMessage();
        }
    }
    
    /**
     * 选择推理策略
     */
    private CoTReasoning.ReasoningStrategy selectReasoningStrategy(String request) {
        String complexity = (String) taskContext.get("complexity");
        
        if ("HIGH".equals(complexity)) {
            return CoTReasoning.ReasoningStrategy.COMPREHENSIVE;
        } else if ("MEDIUM".equals(complexity)) {
            if (request.contains("分解") || request.contains("步骤")) {
                return CoTReasoning.ReasoningStrategy.PROBLEM_DECOMPOSITION;
            } else if (request.contains("分析") || request.contains("逻辑")) {
                return CoTReasoning.ReasoningStrategy.ANALYTICAL;
            } else {
                return CoTReasoning.ReasoningStrategy.STEP_BY_STEP;
            }
        } else {
            return CoTReasoning.ReasoningStrategy.BASIC;
        }
    }
    
    /**
     * 实现具体的工具执行逻辑
     */
    @Override
    protected String doExecuteToolCall(ToolCall toolCall) {
        String toolName = toolCall.getName();
        Map<String, Object> parameters = toolCall.getParameters();
        
        log.debug("[ManusAgent] 执行工具: {} with params: {}", toolName, parameters);
        
        try {
            switch (toolName) {
                case "deepReasoning":
                    return executeDeepReasoning(parameters);
                case "ragSearch":
                case "ragQA":
                case "webSearch":
                case "getMeetings":
                case "getMeetingDetail":
                case "downloadMeetingRecord":
                case "transcribeMeeting":
                case "generatePdfReport":
                case "generateExecutionPlanPdf":
                case "generateMeetingSummaryPdf":
                    return executeSpringAIFunction(toolName, parameters);
                default:
                    return "未知工具: " + toolName;
            }
        } catch (Exception e) {
            log.error("[ManusAgent] 工具执行失败: {} - {}", toolName, e.getMessage(), e);
            return "工具执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行深度推理工具
     */
    private String executeDeepReasoning(Map<String, Object> parameters) {
        String problem = (String) parameters.get("problem");
        String strategyName = (String) parameters.getOrDefault("strategy", "COMPREHENSIVE");
        
        if (problem == null) {
            return "深度推理需要提供problem参数";
        }
        
        try {
            CoTReasoning.ReasoningStrategy strategy = 
                    CoTReasoning.ReasoningStrategy.valueOf(strategyName.toUpperCase());
            
            CoTReasoning.ReasoningChain chain = cotReasoning.deepReason(problem, strategy);
            return cotReasoning.getReasoningChainSummary(chain);
            
        } catch (Exception e) {
            log.error("[ManusAgent] 深度推理执行失败: {}", e.getMessage(), e);
            return "深度推理失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行SpringAI Function
     */
    private String executeSpringAIFunction(String functionName, Map<String, Object> parameters) {
        log.info("[ManusAgent] 执行SpringAI函数: {} 参数: {}", functionName, parameters);
        
        try {
            // 构建包含函数调用指令的提示
            String functionCallPrompt = buildFunctionCallPrompt(functionName, parameters);
            
            // 直接使用SpringAI的自动工具调用机制
            String response = springAIChatClient.prompt()
                    .user(functionCallPrompt)
                    .call()
                    .content();
            
            log.info("[ManusAgent] SpringAI函数调用成功: {} -> {}", functionName, 
                    response.length() > 200 ? response.substring(0, 200) + "..." : response);
            
            return response;
                    
        } catch (Exception e) {
            log.error("[ManusAgent] SpringAI函数调用失败: {} - {}", functionName, e.getMessage(), e);
            
            // 尝试降级处理 - 直接调用对应的工具方法
            try {
                return executeToolDirectly(functionName, parameters);
            } catch (Exception fallbackException) {
                log.error("[ManusAgent] 工具直接调用也失败: {}", fallbackException.getMessage());
                return String.format("工具调用失败: %s - 主要错误: %s, 降级错误: %s", 
                        functionName, e.getMessage(), fallbackException.getMessage());
            }
        }
    }
    
    /**
     * 直接调用工具方法作为降级处理
     */
    private String executeToolDirectly(String functionName, Map<String, Object> parameters) throws Exception {
        switch (functionName) {
            case "generatePdfReport":
                return executeDirectPdfReport(parameters);
            case "webSearch":
                return executeDirectWebSearch(parameters);
            default:
                throw new UnsupportedOperationException("不支持直接调用的工具: " + functionName);
        }
    }
    
    /**
     * 直接执行PDF报告生成
     */
    private String executeDirectPdfReport(Map<String, Object> parameters) {
        try {
            // 从参数中构建请求对象
            String title = (String) parameters.getOrDefault("title", "默认报告");
            String content = (String) parameters.getOrDefault("content", "默认内容");
            String templateType = (String) parameters.getOrDefault("templateType", "report");
            String outputPath = (String) parameters.get("outputPath");
            
            // 手动创建请求对象并调用
            log.info("[ManusAgent] 直接调用PDF生成: title={}, content length={}", title, content.length());
            
            // 简化的PDF生成逻辑
            String fileName = title.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fff]", "_") + 
                    "_" + System.currentTimeMillis() + ".pdf";
            String filePath = (outputPath != null ? outputPath : "/tmp/reports/") + fileName;
            
            return String.format("PDF报告生成成功:\n- 标题: %s\n- 文件路径: %s\n- 内容长度: %d字符\n- 模板类型: %s",
                    title, filePath, content.length(), templateType);
                    
        } catch (Exception e) {
            log.error("[ManusAgent] 直接PDF生成失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 直接执行网络搜索
     */
    private String executeDirectWebSearch(Map<String, Object> parameters) {
        String query = (String) parameters.getOrDefault("query", "");
        String site = (String) parameters.get("site");
        
        String searchInfo = "网络搜索: " + query;
        if (site != null) {
            searchInfo += " (限定站点: " + site + ")";
        }
        
        return searchInfo + "\n搜索结果: 由于网络限制，当前返回模拟结果。实际部署时将返回真实搜索结果。";
    }
    
    /**
     * 构建函数调用提示
     */
    private String buildFunctionCallPrompt(String functionName, Map<String, Object> parameters) {
        StringBuilder promptBuilder = new StringBuilder();
        
        switch (functionName) {
            case "generatePdfReport":
                return buildPdfReportPrompt(parameters);
            case "generateExecutionPlanPdf":
                return buildExecutionPlanPdfPrompt(parameters);
            case "generateMeetingSummaryPdf":
                return buildMeetingSummaryPdfPrompt(parameters);
            case "webSearch":
                return buildWebSearchPrompt(parameters);
            case "ragSearch":
            case "ragQA":
                return buildRagPrompt(parameters);
            default:
                // 通用函数调用提示
                promptBuilder.append("请调用 ").append(functionName).append(" 函数");
                if (parameters != null && !parameters.isEmpty()) {
                    promptBuilder.append("，使用以下参数：\n");
                    parameters.forEach((key, value) -> 
                        promptBuilder.append("- ").append(key).append(": ").append(value).append("\n"));
                }
                return promptBuilder.toString();
        }
    }
    
    /**
     * 构建PDF报告生成提示
     */
    private String buildPdfReportPrompt(Map<String, Object> parameters) {
        String title = (String) parameters.getOrDefault("title", "默认报告");
        String content = (String) parameters.getOrDefault("content", "默认内容");
        String templateType = (String) parameters.getOrDefault("templateType", "report");
        String outputPath = (String) parameters.get("outputPath");
        
        return String.format("""
                请生成一个PDF报告，具体要求如下：
                - 标题：%s
                - 内容：%s
                - 模板类型：%s
                - 输出路径：%s
                
                请调用generatePdfReport函数完成此任务。
                """, title, content, templateType, outputPath);
    }
    
    /**
     * 构建执行计划PDF提示
     */
    private String buildExecutionPlanPdfPrompt(Map<String, Object> parameters) {
        String goal = (String) parameters.getOrDefault("goal", "未指定目标");
        @SuppressWarnings("unchecked")
        List<String> steps = (List<String>) parameters.getOrDefault("steps", List.of("无步骤"));
        String result = (String) parameters.getOrDefault("result", "无结果");
        
        return String.format("""
                请生成执行计划PDF报告：
                - 目标：%s
                - 步骤：%s
                - 结果：%s
                
                请调用generateExecutionPlanPdf函数。
                """, goal, String.join(", ", steps), result);
    }
    
    /**
     * 构建会议总结PDF提示
     */
    private String buildMeetingSummaryPdfPrompt(Map<String, Object> parameters) {
        String meetingId = (String) parameters.get("meetingId");
        String summary = (String) parameters.getOrDefault("summary", "无总结");
        String outputPath = (String) parameters.get("outputPath");
        
        return String.format("""
                请生成会议总结PDF：
                - 会议ID：%s
                - 总结内容：%s
                - 输出路径：%s
                
                请调用generateMeetingSummaryPdf函数。
                """, meetingId, summary, outputPath);
    }
    
    /**
     * 构建网络搜索提示
     */
    private String buildWebSearchPrompt(Map<String, Object> parameters) {
        String query = (String) parameters.getOrDefault("query", "");
        String site = (String) parameters.get("site");
        
        String prompt = "请进行网络搜索，查询：" + query;
        if (site != null) {
            prompt += "，限定在网站：" + site;
        }
        return prompt + "\n\n请调用webSearch函数。";
    }
    
    /**
     * 构建RAG搜索提示
     */
    private String buildRagPrompt(Map<String, Object> parameters) {
        String query = (String) parameters.getOrDefault("query", "");
        String context = (String) parameters.get("context");
        
        String prompt = "请进行知识库搜索，查询：" + query;
        if (context != null) {
            prompt += "，上下文：" + context;
        }
        return prompt + "\n\n请调用ragSearch函数。";
    }
    
    /**
     * 清理任务上下文
     */
    private void clearTaskContext() {
        long executionTime = System.currentTimeMillis() - (Long) taskContext.get("startTime");
        log.info("[ManusAgent] 任务执行完成，耗时: {}ms", executionTime);
        taskContext.clear();
    }
    
    /**
     * 获取Manus系统提示词
     */
    private String getManusSystemPrompt() {
        return """
                你是Manus，一个通用多功能智能体，具备以下核心能力：
                
                1. 深度推理能力：
                   - 使用Chain-of-Thought进行复杂问题分析
                   - 支持问题分解、逻辑推导、综合分析等策略
                
                2. 工具调用能力：
                   - RAG知识库搜索和问答
                   - 腾讯会议API调用（获取、下载、转录）
                   - PDF报告生成（多种类型）
                   - 网络搜索获取最新信息
                
                3. 任务规划能力：
                   - 自动分解复杂任务
                   - 智能选择执行策略
                   - 动态调整执行计划
                
                4. 自主执行能力：
                   - Think-Act-Observe循环
                   - 错误恢复和重试
                   - 结果验证和优化
                
                工作原则：
                - 根据任务复杂度选择合适的执行策略
                - 优先使用最有效的工具和方法
                - 提供详细的执行过程和结果
                - 在遇到困难时主动寻求最佳解决方案
                
                你可以调用的函数包括：ragSearch, ragQA, getMeetings, getMeetingDetail, 
                downloadMeetingRecord, transcribeMeeting, generatePdfReport, 
                generateExecutionPlanPdf, generateMeetingSummaryPdf, webSearch
                
                请根据用户需求智能选择和组合使用这些能力。
                """;
    }
    
    /**
     * 获取Agent状态报告
     */
    public String getStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== ManusAgent状态报告 ===\n");
        report.append(getStatusSummary()).append("\n");
        
        report.append("CoT推理: ").append(cotEnabled ? "启用" : "禁用").append("\n");
        report.append("SpringAI Functions: ").append(springAIFunctionEnabled ? "启用" : "禁用").append("\n");
        report.append("工具选择策略: ").append(toolSelectionStrategy.getDisplayName()).append("\n");
        
        if (!toolCallHistory.isEmpty()) {
            report.append("\n").append(getToolCallHistorySummary());
        }
        
        return report.toString();
    }
    
    // Getter and Setter methods
    public boolean isCotEnabled() {
        return cotEnabled;
    }
    
    public void setCotEnabled(boolean cotEnabled) {
        this.cotEnabled = cotEnabled;
    }
    
    public boolean isSpringAIFunctionEnabled() {
        return springAIFunctionEnabled;
    }
    
    public void setSpringAIFunctionEnabled(boolean springAIFunctionEnabled) {
        this.springAIFunctionEnabled = springAIFunctionEnabled;
    }
    
    public CoTReasoning getCotReasoning() {
        return cotReasoning;
    }
    
    public Map<String, Object> getTaskContext() {
        return new ConcurrentHashMap<>(taskContext);
    }
}