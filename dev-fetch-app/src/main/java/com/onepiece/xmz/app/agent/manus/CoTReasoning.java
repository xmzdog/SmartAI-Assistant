package com.onepiece.xmz.app.agent.manus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chain-of-Thought (CoT) 深度推理引擎
 * 参考OpenManus的推理能力设计，实现逐步思维链推理
 * 
 * CoT推理的核心思想：
 * 1. 问题分解 - 将复杂问题分解为更小的子问题
 * 2. 逐步推理 - 一步一步地解决每个子问题
 * 3. 逻辑验证 - 验证每一步推理的合理性
 * 4. 结果综合 - 将各步骤结果综合得出最终答案
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
public class CoTReasoning {
    
    private final ChatClient chatClient;
    private final AgentMemory memory;
    
    /**
     * 推理步骤记录
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasoningStep {
        private int stepNumber;
        private String stepType;
        private String description;
        private String input;
        private String output;
        private String reasoning;
        private boolean verified;
        private LocalDateTime timestamp;
        private long executionTimeMs;
        
        public ReasoningStep(int stepNumber, String stepType, String description) {
            this.stepNumber = stepNumber;
            this.stepType = stepType;
            this.description = description;
            this.timestamp = LocalDateTime.now();
            this.verified = false;
        }
    }
    
    /**
     * 推理链记录
     */
    @Data
    public static class ReasoningChain {
        private String problemStatement;
        private List<ReasoningStep> steps = new ArrayList<>();
        private String finalAnswer;
        private boolean isValid;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String reasoningStrategy;
        
        public ReasoningChain(String problemStatement, String strategy) {
            this.problemStatement = problemStatement;
            this.reasoningStrategy = strategy;
            this.startTime = LocalDateTime.now();
        }
        
        public void addStep(ReasoningStep step) {
            steps.add(step);
        }
        
        public void complete(String finalAnswer, boolean isValid) {
            this.finalAnswer = finalAnswer;
            this.isValid = isValid;
            this.endTime = LocalDateTime.now();
        }
        
        public long getTotalExecutionTimeMs() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
    }
    
    public CoTReasoning(ChatClient chatClient, AgentMemory memory) {
        this.chatClient = chatClient;
        this.memory = memory;
    }
    
    /**
     * 执行深度推理
     */
    public ReasoningChain deepReason(String problem) {
        return deepReason(problem, ReasoningStrategy.COMPREHENSIVE);
    }
    
    /**
     * 执行深度推理（指定策略）
     */
    public ReasoningChain deepReason(String problem, ReasoningStrategy strategy) {
        log.info("开始CoT深度推理: {}", problem);
        
        ReasoningChain chain = new ReasoningChain(problem, strategy.getDisplayName());
        
        try {
            switch (strategy) {
                case STEP_BY_STEP:
                    return stepByStepReasoning(chain);
                case PROBLEM_DECOMPOSITION:
                    return problemDecompositionReasoning(chain);
                case ANALYTICAL:
                    return analyticalReasoning(chain);
                case COMPREHENSIVE:
                    return comprehensiveReasoning(chain);
                default:
                    return basicReasoning(chain);
            }
            
        } catch (Exception e) {
            log.error("CoT推理过程中发生错误: {}", e.getMessage(), e);
            chain.complete("推理过程中发生错误: " + e.getMessage(), false);
            return chain;
        }
    }
    
    /**
     * 逐步推理策略
     */
    private ReasoningChain stepByStepReasoning(ReasoningChain chain) {
        log.debug("执行逐步推理策略");
        
        // Step 1: 问题理解
        ReasoningStep understandStep = new ReasoningStep(1, "问题理解", "理解问题的核心要求");
        long startTime = System.currentTimeMillis();
        
        String understandPrompt = String.format("""
                请仔细分析以下问题，明确问题的核心要求：
                
                问题：%s
                
                请回答：
                1. 这个问题要求我们做什么？
                2. 问题的关键信息有哪些？
                3. 需要什么样的输出结果？
                
                请提供清晰的分析。
                """, chain.getProblemStatement());
        
        String understanding = performReasoning(understandPrompt);
        understandStep.setInput(chain.getProblemStatement());
        understandStep.setOutput(understanding);
        understandStep.setReasoning("分析问题的核心要求和关键信息");
        understandStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        understandStep.setVerified(true);
        chain.addStep(understandStep);
        
        // Step 2: 解决方案规划
        ReasoningStep planStep = new ReasoningStep(2, "方案规划", "制定解决问题的步骤");
        startTime = System.currentTimeMillis();
        
        String planPrompt = String.format("""
                基于对问题的理解，请制定解决方案：
                
                问题理解：%s
                
                请制定：
                1. 解决这个问题需要哪些步骤？
                2. 每个步骤的目标是什么？
                3. 步骤之间的逻辑关系如何？
                
                请提供详细的解决方案。
                """, understanding);
        
        String plan = performReasoning(planPrompt);
        planStep.setInput(understanding);
        planStep.setOutput(plan);
        planStep.setReasoning("制定解决问题的详细步骤");
        planStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        planStep.setVerified(true);
        chain.addStep(planStep);
        
        // Step 3: 逐步执行
        ReasoningStep executeStep = new ReasoningStep(3, "逐步执行", "按计划逐步解决问题");
        startTime = System.currentTimeMillis();
        
        String executePrompt = String.format("""
                现在按照制定的方案逐步执行：
                
                解决方案：%s
                
                请逐步执行每个步骤，并对每一步的结果进行说明：
                1. 执行具体的计算或分析
                2. 说明每一步的逻辑
                3. 确保步骤之间的连贯性
                
                请提供详细的执行过程。
                """, plan);
        
        String execution = performReasoning(executePrompt);
        executeStep.setInput(plan);
        executeStep.setOutput(execution);
        executeStep.setReasoning("按照方案逐步执行并记录过程");
        executeStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        executeStep.setVerified(true);
        chain.addStep(executeStep);
        
        // Step 4: 结果验证
        ReasoningStep verifyStep = new ReasoningStep(4, "结果验证", "验证解决方案的正确性");
        startTime = System.currentTimeMillis();
        
        String verifyPrompt = String.format("""
                请验证解决方案的正确性：
                
                执行过程：%s
                
                请检查：
                1. 逻辑推理是否正确？
                2. 计算过程是否准确？
                3. 结果是否回答了原始问题？
                4. 是否有遗漏或错误？
                
                请提供验证结果和最终答案。
                """, execution);
        
        String verification = performReasoning(verifyPrompt);
        verifyStep.setInput(execution);
        verifyStep.setOutput(verification);
        verifyStep.setReasoning("验证解决方案的正确性并给出最终答案");
        verifyStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        verifyStep.setVerified(true);
        chain.addStep(verifyStep);
        
        // 提取最终答案
        String finalAnswer = extractFinalAnswer(verification);
        chain.complete(finalAnswer, true);
        
        return chain;
    }
    
    /**
     * 问题分解推理策略
     */
    private ReasoningChain problemDecompositionReasoning(ReasoningChain chain) {
        log.debug("执行问题分解推理策略");
        
        // Step 1: 识别子问题
        ReasoningStep decompositionStep = new ReasoningStep(1, "问题分解", "将复杂问题分解为子问题");
        long startTime = System.currentTimeMillis();
        
        String decompositionPrompt = String.format("""
                请将以下复杂问题分解为更小的子问题：
                
                主问题：%s
                
                请：
                1. 识别这个问题可以分解为哪些子问题
                2. 确定子问题之间的依赖关系
                3. 按照逻辑顺序排列子问题
                
                请提供详细的问题分解结果。
                """, chain.getProblemStatement());
        
        String decomposition = performReasoning(decompositionPrompt);
        decompositionStep.setInput(chain.getProblemStatement());
        decompositionStep.setOutput(decomposition);
        decompositionStep.setReasoning("将复杂问题分解为可管理的子问题");
        decompositionStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        decompositionStep.setVerified(true);
        chain.addStep(decompositionStep);
        
        // Step 2: 逐个解决子问题
        ReasoningStep solveStep = new ReasoningStep(2, "子问题求解", "逐个解决子问题");
        startTime = System.currentTimeMillis();
        
        String solvePrompt = String.format("""
                现在逐个解决子问题：
                
                子问题列表：%s
                
                请：
                1. 按照依赖关系的顺序解决每个子问题
                2. 确保每个子问题的解答都是完整和准确的
                3. 说明子问题解答之间的关联
                
                请提供每个子问题的详细解答。
                """, decomposition);
        
        String solutions = performReasoning(solvePrompt);
        solveStep.setInput(decomposition);
        solveStep.setOutput(solutions);
        solveStep.setReasoning("逐个解决分解后的子问题");
        solveStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        solveStep.setVerified(true);
        chain.addStep(solveStep);
        
        // Step 3: 综合结果
        ReasoningStep synthesisStep = new ReasoningStep(3, "结果综合", "综合子问题解答得出最终结果");
        startTime = System.currentTimeMillis();
        
        String synthesisPrompt = String.format("""
                请综合各子问题的解答，得出原问题的最终答案：
                
                原问题：%s
                
                子问题解答：%s
                
                请：
                1. 将子问题的解答进行逻辑整合
                2. 确保最终答案完整回答了原问题
                3. 检查答案的一致性和合理性
                
                请提供最终的综合答案。
                """, chain.getProblemStatement(), solutions);
        
        String synthesis = performReasoning(synthesisPrompt);
        synthesisStep.setInput(solutions);
        synthesisStep.setOutput(synthesis);
        synthesisStep.setReasoning("综合子问题解答得出最终结果");
        synthesisStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        synthesisStep.setVerified(true);
        chain.addStep(synthesisStep);
        
        String finalAnswer = extractFinalAnswer(synthesis);
        chain.complete(finalAnswer, true);
        
        return chain;
    }
    
    /**
     * 分析式推理策略
     */
    private ReasoningChain analyticalReasoning(ReasoningChain chain) {
        log.debug("执行分析式推理策略");
        
        // Step 1: 条件分析
        ReasoningStep conditionStep = new ReasoningStep(1, "条件分析", "分析问题的已知条件");
        long startTime = System.currentTimeMillis();
        
        String conditionPrompt = String.format("""
                请分析问题的已知条件和约束：
                
                问题：%s
                
                请列出：
                1. 明确给出的条件有哪些？
                2. 隐含的条件或假设有哪些？
                3. 需要遵守的约束或限制有哪些？
                4. 未知的变量或信息有哪些？
                
                请提供详细的条件分析。
                """, chain.getProblemStatement());
        
        String conditions = performReasoning(conditionPrompt);
        conditionStep.setInput(chain.getProblemStatement());
        conditionStep.setOutput(conditions);
        conditionStep.setReasoning("分析问题的已知条件和约束");
        conditionStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        conditionStep.setVerified(true);
        chain.addStep(conditionStep);
        
        // Step 2: 逻辑推导
        ReasoningStep deductionStep = new ReasoningStep(2, "逻辑推导", "基于条件进行逻辑推导");
        startTime = System.currentTimeMillis();
        
        String deductionPrompt = String.format("""
                基于已分析的条件进行逻辑推导：
                
                已知条件：%s
                
                请：
                1. 从已知条件出发进行逻辑推导
                2. 每一步推导都要有清晰的逻辑依据
                3. 识别推导过程中的关键转折点
                4. 验证每一步推导的有效性
                
                请提供详细的推导过程。
                """, conditions);
        
        String deduction = performReasoning(deductionPrompt);
        deductionStep.setInput(conditions);
        deductionStep.setOutput(deduction);
        deductionStep.setReasoning("基于条件进行严密的逻辑推导");
        deductionStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        deductionStep.setVerified(true);
        chain.addStep(deductionStep);
        
        // Step 3: 结论验证
        ReasoningStep conclusionStep = new ReasoningStep(3, "结论验证", "验证推导结论的正确性");
        startTime = System.currentTimeMillis();
        
        String conclusionPrompt = String.format("""
                请验证推导结论的正确性并给出最终答案：
                
                推导过程：%s
                
                请：
                1. 检查推导链的完整性
                2. 验证每个推理步骤的正确性
                3. 确认结论符合原问题要求
                4. 提供清晰的最终答案
                
                请提供验证结果和最终答案。
                """, deduction);
        
        String conclusion = performReasoning(conclusionPrompt);
        conclusionStep.setInput(deduction);
        conclusionStep.setOutput(conclusion);
        conclusionStep.setReasoning("验证推导结论并给出最终答案");
        conclusionStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        conclusionStep.setVerified(true);
        chain.addStep(conclusionStep);
        
        String finalAnswer = extractFinalAnswer(conclusion);
        chain.complete(finalAnswer, true);
        
        return chain;
    }
    
    /**
     * 综合推理策略
     */
    private ReasoningChain comprehensiveReasoning(ReasoningChain chain) {
        log.debug("执行综合推理策略");
        
        // 结合多种策略的优势
        // Step 1: 问题理解和分解
        ReasoningStep analysisStep = new ReasoningStep(1, "综合分析", "全面分析问题");
        long startTime = System.currentTimeMillis();
        
        String analysisPrompt = String.format("""
                请对以下问题进行全面的综合分析：
                
                问题：%s
                
                请按照以下维度进行分析：
                
                1. 问题理解：
                   - 问题的核心要求是什么？
                   - 关键信息和约束条件有哪些？
                
                2. 问题分解：
                   - 可以分解为哪些子问题？
                   - 子问题的优先级和依赖关系如何？
                
                3. 解决策略：
                   - 有哪些可能的解决方法？
                   - 最适合的方法是什么？
                
                4. 执行计划：
                   - 具体的执行步骤是什么？
                   - 每一步的预期结果是什么？
                
                请提供详细的综合分析。
                """, chain.getProblemStatement());
        
        String analysis = performReasoning(analysisPrompt);
        analysisStep.setInput(chain.getProblemStatement());
        analysisStep.setOutput(analysis);
        analysisStep.setReasoning("全面分析问题的各个维度");
        analysisStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        analysisStep.setVerified(true);
        chain.addStep(analysisStep);
        
        // Step 2: 多路径推理
        ReasoningStep multiPathStep = new ReasoningStep(2, "多路径推理", "探索多种解决路径");
        startTime = System.currentTimeMillis();
        
        String multiPathPrompt = String.format("""
                基于综合分析，请探索多种解决路径：
                
                分析结果：%s
                
                请：
                1. 提出至少2-3种不同的解决方法
                2. 分析每种方法的优缺点
                3. 选择最优的解决路径
                4. 详细执行选定的解决方案
                
                请提供多路径分析和详细执行过程。
                """, analysis);
        
        String multiPath = performReasoning(multiPathPrompt);
        multiPathStep.setInput(analysis);
        multiPathStep.setOutput(multiPath);
        multiPathStep.setReasoning("探索多种解决路径并选择最优方案");
        multiPathStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        multiPathStep.setVerified(true);
        chain.addStep(multiPathStep);
        
        // Step 3: 结果整合和验证
        ReasoningStep integrationStep = new ReasoningStep(3, "结果整合", "整合结果并进行最终验证");
        startTime = System.currentTimeMillis();
        
        String integrationPrompt = String.format("""
                请整合推理结果并进行最终验证：
                
                多路径推理结果：%s
                
                请：
                1. 整合所有有效的推理结果
                2. 进行逻辑一致性检查
                3. 验证答案的完整性和准确性
                4. 提供清晰简洁的最终答案
                5. 说明答案的可信度和局限性
                
                请提供最终的整合结果和答案。
                """, multiPath);
        
        String integration = performReasoning(integrationPrompt);
        integrationStep.setInput(multiPath);
        integrationStep.setOutput(integration);
        integrationStep.setReasoning("整合所有推理结果并进行最终验证");
        integrationStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        integrationStep.setVerified(true);
        chain.addStep(integrationStep);
        
        String finalAnswer = extractFinalAnswer(integration);
        chain.complete(finalAnswer, true);
        
        return chain;
    }
    
    /**
     * 基础推理策略
     */
    private ReasoningChain basicReasoning(ReasoningChain chain) {
        log.debug("执行基础推理策略");
        
        ReasoningStep basicStep = new ReasoningStep(1, "基础推理", "直接推理求解");
        long startTime = System.currentTimeMillis();
        
        String basicPrompt = String.format("""
                请直接分析并解决以下问题：
                
                问题：%s
                
                请提供清晰的分析过程和答案。
                """, chain.getProblemStatement());
        
        String result = performReasoning(basicPrompt);
        basicStep.setInput(chain.getProblemStatement());
        basicStep.setOutput(result);
        basicStep.setReasoning("直接分析并解决问题");
        basicStep.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        basicStep.setVerified(true);
        chain.addStep(basicStep);
        
        String finalAnswer = extractFinalAnswer(result);
        chain.complete(finalAnswer, true);
        
        return chain;
    }
    
    /**
     * 执行推理
     */
    private String performReasoning(String prompt) {
        try {
            return chatClient.prompt()
                    .system(getCoTSystemPrompt())
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("推理执行失败: {}", e.getMessage(), e);
            return "推理执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 提取最终答案
     */
    private String extractFinalAnswer(String reasoningOutput) {
        // 尝试从推理输出中提取最终答案
        String[] lines = reasoningOutput.split("\n");
        
        // 查找包含"最终答案"、"答案"、"结论"等关键词的行
        for (String line : lines) {
            if (line.contains("最终答案") || line.contains("答案是") || 
                line.contains("结论是") || line.contains("因此")) {
                return line.trim();
            }
        }
        
        // 如果没有找到明确的答案标识，返回最后几行作为答案
        if (lines.length > 0) {
            return lines[lines.length - 1].trim();
        }
        
        return reasoningOutput;
    }
    
    /**
     * 获取CoT系统提示词
     */
    private String getCoTSystemPrompt() {
        return """
                你是一个专业的推理专家，擅长Chain-of-Thought (CoT) 推理。
                
                你的任务是：
                1. 仔细分析问题的要求
                2. 逐步进行逻辑推理
                3. 确保每一步推理都有明确的依据
                4. 保持推理的连贯性和准确性
                5. 提供清晰的推理过程和结论
                
                请始终遵循逻辑严密、步骤清晰的推理原则。
                """;
    }
    
    /**
     * 获取推理链摘要
     */
    public String getReasoningChainSummary(ReasoningChain chain) {
        StringBuilder summary = new StringBuilder();
        summary.append("=== CoT推理链摘要 ===\n");
        summary.append(String.format("问题: %s\n", chain.getProblemStatement()));
        summary.append(String.format("策略: %s\n", chain.getReasoningStrategy()));
        summary.append(String.format("步骤数: %d\n", chain.getSteps().size()));
        summary.append(String.format("总耗时: %dms\n", chain.getTotalExecutionTimeMs()));
        summary.append(String.format("推理结果: %s\n", chain.isValid() ? "有效" : "无效"));
        
        summary.append("\n推理步骤:\n");
        for (ReasoningStep step : chain.getSteps()) {
            summary.append(String.format("%d. %s (%s) - %dms\n", 
                    step.getStepNumber(), 
                    step.getDescription(), 
                    step.getStepType(),
                    step.getExecutionTimeMs()));
        }
        
        summary.append(String.format("\n最终答案: %s\n", chain.getFinalAnswer()));
        
        return summary.toString();
    }
    
    /**
     * 推理策略枚举
     */
    public enum ReasoningStrategy {
        STEP_BY_STEP("逐步推理"),
        PROBLEM_DECOMPOSITION("问题分解"),
        ANALYTICAL("分析式推理"),
        COMPREHENSIVE("综合推理"),
        BASIC("基础推理");
        
        private final String displayName;
        
        ReasoningStrategy(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}