package com.onepiece.xmz.app.agent.manus;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础Agent抽象类
 * 参考OpenManus的BaseAgent设计，提供Agent的基础框架
 * 
 * 核心功能：
 * 1. 状态管理 - 管理Agent的执行状态
 * 2. 记忆管理 - 维护对话历史和上下文
 * 3. 执行控制 - 提供执行循环和步骤控制
 * 4. 错误处理 - 处理执行过程中的异常
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Data
public abstract class BaseAgent {
    
    /**
     * Agent名称
     */
    protected String name;
    
    /**
     * Agent描述
     */
    protected String description;
    
    /**
     * Agent当前状态
     */
    protected AgentState state = AgentState.IDLE;
    
    /**
     * Agent记忆
     */
    protected AgentMemory memory;
    
    /**
     * ChatClient实例
     */
    protected ChatClient chatClient;
    
    /**
     * 最大执行步数
     */
    protected int maxSteps = 10;
    
    /**
     * 当前执行步数
     */
    protected AtomicInteger currentStep = new AtomicInteger(0);
    
    /**
     * 创建时间
     */
    protected LocalDateTime createdAt;
    
    /**
     * 最后活动时间
     */
    protected LocalDateTime lastActiveAt;
    
    /**
     * 超时时间（分钟）
     */
    protected int timeoutMinutes = 30;
    
    /**
     * 是否启用详细日志
     */
    protected boolean verboseLogging = false;
    
    /**
     * 构造函数
     */
    public BaseAgent(String name, String description) {
        this.name = name;
        this.description = description;
        this.memory = new AgentMemory();
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /**
     * 主要执行方法 - 处理用户请求
     * 
     * @param request 用户请求
     * @return 执行结果
     */
    public String run(String request) {
        log.info("[{}] 开始执行任务: {}", name, request);
        
        try {
            // 状态检查
            if (!state.canAcceptNewTask()) {
                return String.format("Agent当前状态为%s，无法接受新任务", state.getDisplayName());
            }
            
            // 重置执行状态
            resetForNewTask();
            
            // 添加用户请求到记忆
            memory.addUserMessage(request);
            
            // 开始执行
            return executeWithStateManagement(request);
            
        } catch (Exception e) {
            log.error("[{}] 执行任务失败: {}", name, e.getMessage(), e);
            setState(AgentState.FAILED);
            return "执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 异步执行任务
     */
    public CompletableFuture<String> runAsync(String request) {
        return CompletableFuture.supplyAsync(() -> run(request));
    }
    
    /**
     * 带状态管理的执行
     */
    protected String executeWithStateManagement(String request) {
        try {
            setState(AgentState.RUNNING);
            updateLastActiveTime();
            
            String result = execute(request);
            
            // 任务完成后重置为IDLE状态，准备接受下一个任务
            setState(AgentState.FINISHED);
            // 延迟重置为IDLE，让前端有时间看到FINISHED状态
            scheduleStateReset();
            
            memory.addAssistantMessage(result);
            return result;
            
        } catch (Exception e) {
            setState(AgentState.FAILED);
            // 失败后也需要重置状态
            scheduleStateReset();
            throw e;
        }
    }
    
    /**
     * 具体的执行逻辑 - 由子类实现
     */
    protected abstract String execute(String request);
    
    /**
     * 执行单个步骤
     * 参考OpenManus的step方法设计
     */
    public abstract String step();
    
    /**
     * 检查Agent是否卡住
     * 参考OpenManus的is_stuck检查机制
     */
    public boolean isStuck() {
        // 检查执行步数是否超过限制
        if (currentStep.get() >= maxSteps) {
            log.warn("[{}] 达到最大执行步数限制: {}", name, maxSteps);
            return true;
        }
        
        // 检查是否超时
        if (isTimeout()) {
            log.warn("[{}] 执行超时", name);
            return true;
        }
        
        // 检查是否有重复的回复（可能陷入循环）
        if (memory.hasRecentDuplicateAssistantReplies(3)) {
            log.warn("[{}] 检测到重复回复，可能陷入循环", name);
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理卡住状态
     */
    public void handleStuckState() {
        log.warn("[{}] Agent卡住，尝试恢复", name);
        setState(AgentState.STUCK);
        
        // 添加系统消息帮助Agent跳出循环
        memory.addSystemMessage("""
            检测到可能陷入循环或执行困难。请：
            1. 重新分析当前情况
            2. 尝试不同的方法
            3. 如果任务过于复杂，请明确说明无法完成的原因
            4. 提供替代解决方案
            """);
    }
    
    /**
     * 检查是否超时
     */
    public boolean isTimeout() {
        if (lastActiveAt == null) {
            return false;
        }
        
        return LocalDateTime.now().isAfter(
                lastActiveAt.plusMinutes(timeoutMinutes)
        );
    }
    
    /**
     * 重置为新任务准备
     */
    protected void resetForNewTask() {
        currentStep.set(0);
        updateLastActiveTime();
        
        if (verboseLogging) {
            log.debug("[{}] 重置Agent状态，准备执行新任务", name);
        }
    }
    
    /**
     * 调度状态重置 - 延迟重置为IDLE状态
     */
    protected void scheduleStateReset() {
        // 使用Timer延迟3秒后重置状态为IDLE
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if (state == AgentState.FINISHED || state == AgentState.FAILED) {
                    setState(AgentState.IDLE);
                    if (verboseLogging) {
                        log.debug("[{}] 延迟状态重置完成，当前状态: {}", name, state.getDisplayName());
                    }
                }
            }
        }, 3000); // 3秒延迟
    }
    
    /**
     * 设置状态
     */
    public void setState(AgentState newState) {
        AgentState oldState = this.state;
        this.state = newState;
        updateLastActiveTime();
        
        if (verboseLogging) {
            log.debug("[{}] 状态变更: {} -> {}", name, 
                    oldState.getDisplayName(), newState.getDisplayName());
        }
        
        // 记录状态变更到记忆
        if (oldState != newState) {
            memory.addSystemMessage(String.format("Agent状态变更: %s -> %s", 
                    oldState.getDisplayName(), newState.getDisplayName()));
        }
    }
    
    /**
     * 更新最后活动时间
     */
    protected void updateLastActiveTime() {
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /**
     * 获取Agent状态摘要
     */
    public String getStatusSummary() {
        return String.format("""
                Agent: %s
                状态: %s
                描述: %s
                执行步数: %d/%d
                创建时间: %s
                最后活动: %s
                记忆摘要: %s
                """,
                name,
                state.getDisplayName(),
                description,
                currentStep.get(),
                maxSteps,
                createdAt,
                lastActiveAt,
                memory.getSummary()
        );
    }
    
    /**
     * 停止Agent执行
     */
    public void stop() {
        setState(AgentState.PAUSED);
        log.info("[{}] Agent已停止", name);
    }
    
    /**
     * 恢复Agent执行
     */
    public void resume() {
        if (state == AgentState.PAUSED) {
            setState(AgentState.IDLE);
            log.info("[{}] Agent已恢复", name);
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (memory != null) {
            memory.clear();
        }
        setState(AgentState.IDLE);
        currentStep.set(0);
        log.info("[{}] Agent资源已清理", name);
    }
    
    /**
     * 获取系统提示词（由子类重写）
     */
    protected String getSystemPrompt() {
        return String.format("""
                你是一个名为"%s"的AI助手。
                
                描述: %s
                
                请根据用户的请求提供帮助。
                """, name, description);
    }
    
    /**
     * 获取下一步提示词（由子类重写）
     */
    protected String getNextStepPrompt() {
        return "请分析当前情况并决定下一步行动。";
    }
}