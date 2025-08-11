package com.onepiece.xmz.app.agent.manus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * ReAct (Reasoning-Acting) Agent
 * 参考OpenManus的ReActAgent设计，实现思考-行动循环模式
 * 
 * ReAct模式核心思想：
 * 1. Think (思考) - 分析当前情况，理解问题，制定计划
 * 2. Act (行动) - 执行具体的行动或工具调用
 * 3. Observe (观察) - 观察行动结果，更新理解
 * 
 * 这种模式使Agent能够进行推理驱动的行动，而不是盲目执行
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
public abstract class ReActAgent extends BaseAgent {
    
    /**
     * 系统提示词
     */
    protected String systemPrompt;
    
    /**
     * 下一步提示词
     */
    protected String nextStepPrompt;
    
    /**
     * 是否启用逐步推理
     */
    protected boolean stepByStepReasoning = true;
    
    /**
     * 推理链最大长度
     */
    protected int maxReasoningSteps = 5;
    
    public ReActAgent(String name, String description) {
        super(name, description);
        // 不在这里初始化提示词，让子类在工具初始化后再调用
    }
    
    public ReActAgent(String name, String description, ChatClient chatClient) {
        super(name, description);
        this.chatClient = chatClient;
        // 不在这里初始化提示词，让子类在工具初始化后再调用
    }
    
    /**
     * 初始化提示词
     */
    protected void initializePrompts() {
        this.systemPrompt = getDefaultSystemPrompt();
        this.nextStepPrompt = getDefaultNextStepPrompt();
    }
    
    /**
     * 执行ReAct循环
     */
    @Override
    protected String execute(String request) {
        log.info("[{}] 开始ReAct执行循环", name);
        
        StringBuilder executionLog = new StringBuilder();
        executionLog.append(String.format("=== %s 执行记录 ===\n", name));
        
        try {
            while (currentStep.get() < maxSteps && !isStuck()) {
                currentStep.incrementAndGet();
                
                log.debug("[{}] 执行第{}步", name, currentStep.get());
                executionLog.append(String.format("\n--- 第%d步 ---\n", currentStep.get()));
                
                // Step 1: Think (思考阶段)
                setState(AgentState.THINKING);
                boolean shouldAct = think();
                
                if (!shouldAct) {
                    log.info("[{}] 思考完成，无需进一步行动", name);
                    executionLog.append("思考完成：无需进一步行动\n");
                    break;
                }
                
                // Step 2: Act (行动阶段)
                setState(AgentState.ACTING);
                String actionResult = act();
                executionLog.append(String.format("行动结果：%s\n", actionResult));
                
                // Step 3: Observe (观察阶段)
                setState(AgentState.OBSERVING);
                boolean shouldContinue = observe(actionResult);
                
                if (!shouldContinue) {
                    log.info("[{}] 观察完成，任务结束", name);
                    executionLog.append("观察完成：任务结束\n");
                    break;
                }
                
                // 检查是否应该继续
                if (isTaskCompleted()) {
                    log.info("[{}] 任务已完成", name);
                    executionLog.append("任务已完成\n");
                    break;
                }
            }
            
            // 处理特殊状态
            if (isStuck()) {
                handleStuckState();
                executionLog.append("检测到卡住状态，已处理\n");
            }
            
            String finalResult = generateFinalResult();
            executionLog.append(String.format("\n=== 最终结果 ===\n%s", finalResult));
            
            return executionLog.toString();
            
        } catch (Exception e) {
            log.error("[{}] ReAct执行过程中出错: {}", name, e.getMessage(), e);
            setState(AgentState.FAILED);
            return "执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行单步 - 实现完整的Think-Act-Observe循环
     */
    @Override
    public String step() {
        if (isStuck()) {
            handleStuckState();
            return "Agent已卡住，已处理";
        }
        
        currentStep.incrementAndGet();
        log.debug("[{}] 执行步骤 {}", name, currentStep.get());
        
        try {
            // Think
            setState(AgentState.THINKING);
            boolean shouldAct = think();
            
            if (!shouldAct) {
                return "思考完成，无需行动";
            }
            
            // Act
            setState(AgentState.ACTING);
            String actionResult = act();
            
            // Observe
            setState(AgentState.OBSERVING);
            observe(actionResult);
            
            return actionResult;
            
        } catch (Exception e) {
            log.error("[{}] 步骤执行失败: {}", name, e.getMessage(), e);
            setState(AgentState.FAILED);
            return "步骤执行失败: " + e.getMessage();
        }
    }
    
    /**
     * Think阶段 - 分析当前情况并决定下一步行动
     * 抽象方法，由子类实现具体的思考逻辑
     * 
     * @return 是否需要执行行动
     */
    protected abstract boolean think();
    
    /**
     * Act阶段 - 执行具体的行动
     * 抽象方法，由子类实现具体的行动逻辑
     * 
     * @return 行动结果
     */
    protected abstract String act();
    
    /**
     * Observe阶段 - 观察行动结果并更新状态
     * 可被子类重写以实现特定的观察逻辑
     * 
     * @param actionResult 行动结果
     * @return 是否应该继续执行
     */
    protected boolean observe(String actionResult) {
        log.debug("[{}] 观察行动结果: {}", name, actionResult);
        
        // 将行动结果添加到记忆中
        memory.addAssistantMessage("行动结果: " + actionResult);
        
        // 基础观察逻辑：检查结果是否表明任务完成
        if (actionResult.contains("任务完成") || 
            actionResult.contains("执行成功") ||
            actionResult.contains("已完成")) {
            return false; // 不需要继续
        }
        
        return true; // 需要继续执行
    }
    
    /**
     * 检查任务是否完成
     */
    protected boolean isTaskCompleted() {
        // 基础实现：检查记忆中是否包含完成标识
        return memory.containsContent("任务完成") || 
               memory.containsContent("执行成功");
    }
    
    /**
     * 生成最终结果
     */
    protected String generateFinalResult() {
        // 基础实现：返回最后的助手消息
        List<AgentMemory.AgentMessage> assistantMessages = memory.getMessagesByRole("assistant");
        if (!assistantMessages.isEmpty()) {
            return assistantMessages.get(assistantMessages.size() - 1).getContent();
        }
        
        return "任务执行完成";
    }
    
    /**
     * 使用ChatClient进行推理
     */
    protected String reasoning(String prompt) {
        if (chatClient == null) {
            log.warn("[{}] ChatClient未初始化，无法进行推理", name);
            return "无法进行推理：ChatClient未初始化";
        }
        
        try {
            log.debug("[{}] 执行推理: {}", name, prompt);
            
            List<Message> messages = memory.toSpringAIMessagesExcludingTools();
            
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .messages(messages)
                    .user(prompt)
                    .call()
                    .content();
            
            log.debug("[{}] 推理结果: {}", name, response);
            return response;
            
        } catch (Exception e) {
            log.error("[{}] 推理过程中出错: {}", name, e.getMessage(), e);
            return "推理失败: " + e.getMessage();
        }
    }
    
    /**
     * 深度推理（Chain-of-Thought）
     */
    protected String deepReasoning(String problem) {
        log.debug("[{}] 开始深度推理", name);
        
        String cotPrompt = String.format("""
                请对以下问题进行深度推理，使用Chain-of-Thought方法：
                
                问题：%s
                
                请按以下步骤进行推理：
                1. 问题分析：明确问题的核心要求
                2. 信息梳理：列出已知信息和条件
                3. 解决路径：制定解决问题的步骤
                4. 逻辑推理：逐步推理每个步骤
                5. 结果验证：检验解决方案的合理性
                
                请提供详细的推理过程。
                """, problem);
        
        return reasoning(cotPrompt);
    }
    
    /**
     * 获取默认系统提示词
     */
    protected String getDefaultSystemPrompt() {
        return String.format("""
                你是一个名为"%s"的ReAct智能体。
                
                描述：%s
                
                你使用ReAct (Reasoning-Acting) 模式工作：
                1. Think (思考)：分析当前情况，理解问题，制定计划
                2. Act (行动)：执行具体的行动或工具调用
                3. Observe (观察)：观察行动结果，更新理解
                
                工作原则：
                - 在行动前必须先思考
                - 基于观察结果调整后续行动
                - 保持逻辑推理的连贯性
                - 在不确定时寻求更多信息
                
                请始终遵循Think-Act-Observe的循环模式。
                """, name, description);
    }
    
    /**
     * 获取默认下一步提示词
     */
    protected String getDefaultNextStepPrompt() {
        return """
                基于当前情况，请：
                1. 分析现状和已有信息
                2. 确定下一步最合适的行动
                3. 说明选择该行动的理由
                
                如果任务已完成，请明确说明。
                如果需要更多信息，请说明需要什么信息。
                """;
    }
    
    // Getter and Setter methods
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    
    public String getNextStepPrompt() {
        return nextStepPrompt;
    }
    
    public void setNextStepPrompt(String nextStepPrompt) {
        this.nextStepPrompt = nextStepPrompt;
    }
    
    public boolean isStepByStepReasoning() {
        return stepByStepReasoning;
    }
    
    public void setStepByStepReasoning(boolean stepByStepReasoning) {
        this.stepByStepReasoning = stepByStepReasoning;
    }
    
    public int getMaxReasoningSteps() {
        return maxReasoningSteps;
    }
    
    public void setMaxReasoningSteps(int maxReasoningSteps) {
        this.maxReasoningSteps = maxReasoningSteps;
    }
}