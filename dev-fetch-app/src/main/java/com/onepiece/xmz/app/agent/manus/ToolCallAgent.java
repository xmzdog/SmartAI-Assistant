package com.onepiece.xmz.app.agent.manus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ToolCall Agent - 工具调用智能体
 * 参考OpenManus的ToolCallAgent设计，扩展ReActAgent以支持工具调用
 * 
 * 核心功能：
 * 1. 工具发现和选择 - 基于任务需求智能选择工具
 * 2. 工具调用执行 - 安全地执行工具调用
 * 3. 结果处理 - 处理工具执行结果并决定后续行动
 * 4. 错误恢复 - 处理工具调用失败的情况
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
public abstract class ToolCallAgent extends ReActAgent {
    
    /**
     * 可用工具映射
     */
    protected Map<String, String> availableTools = new ConcurrentHashMap<>();
    
    /**
     * 当前执行的工具调用列表
     */
    protected List<ToolCall> currentToolCalls = new ArrayList<>();
    
    /**
     * 工具调用历史
     */
    protected List<ToolCall> toolCallHistory = new ArrayList<>();
    
    /**
     * 工具选择策略
     */
    protected ToolSelectionStrategy toolSelectionStrategy = ToolSelectionStrategy.AUTO;
    
    /**
     * JSON对象映射器
     */
    protected ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 工具调用超时时间（秒）
     */
    protected int toolCallTimeoutSeconds = 30;
    
    /**
     * 最大并发工具调用数
     */
    protected int maxConcurrentToolCalls = 3;
    
    /**
     * 工具选择策略枚举
     */
    public enum ToolSelectionStrategy {
        AUTO("自动选择"),
        MANUAL("手动指定"),
        HYBRID("混合模式");
        
        private final String displayName;
        
        ToolSelectionStrategy(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public ToolCallAgent(String name, String description) {
        super(name, description);
        initializeDefaultTools();
        // 重新初始化提示词，因为现在工具已经初始化了
        initializePrompts();
    }
    
    public ToolCallAgent(String name, String description, ChatClient chatClient) {
        super(name, description, chatClient);
        initializeDefaultTools();
        // 重新初始化提示词，因为现在工具已经初始化了
        initializePrompts();
    }
    
    /**
     * 初始化默认工具
     */
    protected void initializeDefaultTools() {
        // 添加基础工具（由子类重写以添加特定工具）
        availableTools.put("ragSearch", "在知识库中进行语义搜索");
        availableTools.put("ragQA", "基于知识库回答问题");
        availableTools.put("webSearch", "网络搜索获取信息");
        availableTools.put("generatePdfReport", "生成PDF报告");
    }
    
    /**
     * Think阶段 - 分析任务并选择工具
     */
    @Override
    protected boolean think() {
        log.debug("[{}] 开始Think阶段：分析任务并选择工具", name);
        
        try {
            // 分析当前任务和上下文
            String analysisPrompt = buildAnalysisPrompt();
            String analysis = reasoning(analysisPrompt);
            
            // 记录分析结果
            memory.addAssistantMessage("任务分析: " + analysis);
            
            // 选择工具
            List<ToolCall> selectedTools = selectTools(analysis);
            
            if (selectedTools.isEmpty()) {
                log.info("[{}] 无需调用工具，任务可直接完成", name);
                return false;
            }
            
            // 设置当前工具调用
            this.currentToolCalls = selectedTools;
            
            log.info("[{}] 选择了{}个工具: {}", name, selectedTools.size(),
                    selectedTools.stream().map(ToolCall::getName).collect(Collectors.joining(", ")));
            
            return true;
            
        } catch (Exception e) {
            log.error("[{}] Think阶段出错: {}", name, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Act阶段 - 执行选定的工具调用
     */
    @Override
    protected String act() {
        log.debug("[{}] 开始Act阶段：执行工具调用", name);
        
        if (currentToolCalls.isEmpty()) {
            return "无工具需要执行";
        }
        
        StringBuilder executionResults = new StringBuilder();
        List<String> successfulResults = new ArrayList<>();
        List<String> failedResults = new ArrayList<>();
        
        for (ToolCall toolCall : currentToolCalls) {
            try {
                log.info("[{}] 执行工具调用: {}", name, toolCall.getName());
                
                String result = executeToolCall(toolCall);
                
                if (toolCall.isSuccess()) {
                    successfulResults.add(String.format("%s: %s", toolCall.getName(), result));
                } else {
                    failedResults.add(String.format("%s失败: %s", toolCall.getName(), toolCall.getError()));
                }
                
                // 添加到历史记录
                toolCallHistory.add(toolCall);
                
                // 记录到记忆
                memory.addToolMessage(result, toolCall.getId(), toolCall.getName());
                
            } catch (Exception e) {
                log.error("[{}] 工具调用执行异常: {}", name, e.getMessage(), e);
                toolCall.markAsFailed("执行异常: " + e.getMessage());
                failedResults.add(String.format("%s异常: %s", toolCall.getName(), e.getMessage()));
            }
        }
        
        // 构建执行结果摘要
        executionResults.append("工具调用执行完成:\n");
        
        if (!successfulResults.isEmpty()) {
            executionResults.append("成功执行:\n");
            successfulResults.forEach(result -> executionResults.append("- ").append(result).append("\n"));
        }
        
        if (!failedResults.isEmpty()) {
            executionResults.append("执行失败:\n");
            failedResults.forEach(result -> executionResults.append("- ").append(result).append("\n"));
        }
        
        // 清空当前工具调用
        currentToolCalls.clear();
        
        return executionResults.toString();
    }
    
    /**
     * 选择工具
     */
    protected List<ToolCall> selectTools(String taskAnalysis) {
        List<ToolCall> selectedTools = new ArrayList<>();
        
        switch (toolSelectionStrategy) {
            case AUTO:
                selectedTools = autoSelectTools(taskAnalysis);
                break;
            case MANUAL:
                selectedTools = manualSelectTools(taskAnalysis);
                break;
            case HYBRID:
                selectedTools = hybridSelectTools(taskAnalysis);
                break;
        }
        
        return selectedTools;
    }
    
    /**
     * 自动选择工具
     */
    protected List<ToolCall> autoSelectTools(String taskAnalysis) {
        log.debug("[{}] 自动选择工具", name);
        
        try {
            String toolSelectionPrompt = buildToolSelectionPrompt(taskAnalysis);
            String llmResponse = reasoning(toolSelectionPrompt);
            
            return parseToolSelectionResponse(llmResponse);
            
        } catch (Exception e) {
            log.error("[{}] 自动工具选择失败: {}", name, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 手动选择工具（由子类实现具体逻辑）
     */
    protected List<ToolCall> manualSelectTools(String taskAnalysis) {
        // 默认返回空列表，由子类重写
        return new ArrayList<>();
    }
    
    /**
     * 混合选择工具
     */
    protected List<ToolCall> hybridSelectTools(String taskAnalysis) {
        // 先尝试自动选择，如果失败则使用手动选择
        List<ToolCall> autoSelected = autoSelectTools(taskAnalysis);
        if (!autoSelected.isEmpty()) {
            return autoSelected;
        }
        
        return manualSelectTools(taskAnalysis);
    }
    
    /**
     * 执行单个工具调用
     */
    protected String executeToolCall(ToolCall toolCall) {
        log.debug("[{}] 执行工具调用: {}", name, toolCall.getName());
        
        toolCall.markAsExecuting();
        
        try {
            // 这里是抽象方法，由子类实现具体的工具执行逻辑
            String result = doExecuteToolCall(toolCall);
            
            toolCall.markAsSuccess(result);
            log.info("[{}] 工具调用成功: {} -> {}", name, toolCall.getName(), 
                    result.length() > 100 ? result.substring(0, 100) + "..." : result);
            
            return result;
            
        } catch (Exception e) {
            log.error("[{}] 工具调用失败: {} - {}", name, toolCall.getName(), e.getMessage(), e);
            toolCall.markAsFailed(e.getMessage());
            throw e;
        }
    }
    
    /**
     * 具体的工具执行逻辑（由子类实现）
     */
    protected abstract String doExecuteToolCall(ToolCall toolCall);
    
    /**
     * 构建任务分析提示词
     */
    protected String buildAnalysisPrompt() {
        return String.format("""
                请分析当前任务的需求和上下文：
                
                当前对话记忆：
                %s
                
                可用工具：
                %s
                
                请分析：
                1. 任务的核心目标是什么？
                2. 需要什么信息或能力来完成任务？
                3. 哪些工具可能对完成任务有帮助？
                
                请提供详细的分析。
                """, 
                getMemorySummary(),
                getAvailableToolsDescription());
    }
    
    /**
     * 构建工具选择提示词
     */
    protected String buildToolSelectionPrompt(String taskAnalysis) {
        return String.format("""
                基于以下任务分析，请选择需要调用的工具：
                
                任务分析：
                %s
                
                可用工具：
                %s
                
                请以JSON格式返回工具选择结果，格式如下：
                [
                  {
                    "name": "工具名称",
                    "parameters": {
                      "参数名": "参数值"
                    },
                    "reason": "选择理由"
                  }
                ]
                
                如果不需要调用任何工具，请返回空数组 []。
                """,
                taskAnalysis,
                getAvailableToolsDescription());
    }
    
    /**
     * 解析工具选择响应
     */
    protected List<ToolCall> parseToolSelectionResponse(String response) {
        List<ToolCall> toolCalls = new ArrayList<>();
        
        try {
            // 提取JSON部分
            String jsonPart = extractJsonFromResponse(response);
            if (jsonPart.isEmpty()) {
                log.warn("[{}] 无法从响应中提取JSON: {}", name, response);
                return toolCalls;
            }
            
            // 解析JSON
            List<Map<String, Object>> toolSpecs = objectMapper.readValue(
                    jsonPart, new TypeReference<List<Map<String, Object>>>() {});
            
            for (Map<String, Object> spec : toolSpecs) {
                String toolName = (String) spec.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> parameters = (Map<String, Object>) spec.get("parameters");
                
                if (toolName != null && availableTools.containsKey(toolName)) {
                    String toolId = UUID.randomUUID().toString();
                    ToolCall toolCall = new ToolCall(toolId, toolName, parameters);
                    toolCalls.add(toolCall);
                }
            }
            
        } catch (Exception e) {
            log.error("[{}] 解析工具选择响应失败: {}", name, e.getMessage(), e);
        }
        
        return toolCalls;
    }
    
    /**
     * 从响应中提取JSON部分
     */
    protected String extractJsonFromResponse(String response) {
        // 查找JSON数组开始和结束位置
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        return "";
    }
    
    /**
     * 获取记忆摘要
     */
    protected String getMemorySummary() {
        List<AgentMemory.AgentMessage> recentMessages = memory.getRecentMessages(5);
        
        return recentMessages.stream()
                .map(msg -> String.format("%s: %s", msg.getRole(), msg.getContent()))
                .collect(Collectors.joining("\n"));
    }
    
    /**
     * 获取可用工具描述
     */
    protected String getAvailableToolsDescription() {
        return availableTools.entrySet().stream()
                .map(entry -> String.format("- %s: %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
    }
    
    /**
     * 获取工具调用历史摘要
     */
    public String getToolCallHistorySummary() {
        if (toolCallHistory.isEmpty()) {
            return "无工具调用历史";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("工具调用历史（共%d次）:\n", toolCallHistory.size()));
        
        toolCallHistory.forEach(call -> summary.append("- ").append(call.getSummary()).append("\n"));
        
        return summary.toString();
    }
    
    /**
     * 添加工具
     */
    public void addTool(String name, String description) {
        availableTools.put(name, description);
        log.info("[{}] 添加工具: {} - {}", this.name, name, description);
    }
    
    /**
     * 移除工具
     */
    public void removeTool(String name) {
        availableTools.remove(name);
        log.info("[{}] 移除工具: {}", this.name, name);
    }
    
    /**
     * 获取默认系统提示词
     */
    @Override
    protected String getDefaultSystemPrompt() {
        return String.format("""
                你是一个名为"%s"的工具调用智能体。
                
                描述：%s
                
                你的核心能力：
                1. 理解任务需求并分析所需工具
                2. 智能选择和调用合适的工具
                3. 处理工具执行结果并决定后续行动
                4. 处理工具调用失败的情况
                
                可用工具：
                %s
                
                工作流程：
                1. Think (思考): 分析任务，确定需要哪些工具
                2. Act (行动): 调用选定的工具
                3. Observe (观察): 分析结果，决定是否需要进一步行动
                
                请始终遵循这个流程，并提供清晰的推理过程。
                """, name, description, getAvailableToolsDescription());
    }
    
    // Getter and Setter methods
    public Map<String, String> getAvailableTools() {
        return new HashMap<>(availableTools);
    }
    
    public List<ToolCall> getCurrentToolCalls() {
        return new ArrayList<>(currentToolCalls);
    }
    
    public List<ToolCall> getToolCallHistory() {
        return new ArrayList<>(toolCallHistory);
    }
    
    public ToolSelectionStrategy getToolSelectionStrategy() {
        return toolSelectionStrategy;
    }
    
    public void setToolSelectionStrategy(ToolSelectionStrategy toolSelectionStrategy) {
        this.toolSelectionStrategy = toolSelectionStrategy;
    }
    
    public int getToolCallTimeoutSeconds() {
        return toolCallTimeoutSeconds;
    }
    
    public void setToolCallTimeoutSeconds(int toolCallTimeoutSeconds) {
        this.toolCallTimeoutSeconds = toolCallTimeoutSeconds;
    }
    
    public int getMaxConcurrentToolCalls() {
        return maxConcurrentToolCalls;
    }
    
    public void setMaxConcurrentToolCalls(int maxConcurrentToolCalls) {
        this.maxConcurrentToolCalls = maxConcurrentToolCalls;
    }
}