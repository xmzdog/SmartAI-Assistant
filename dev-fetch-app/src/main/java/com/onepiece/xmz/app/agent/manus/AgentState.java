package com.onepiece.xmz.app.agent.manus;

/**
 * 智能体状态枚举
 * 参考OpenManus的Agent状态管理设计
 * 
 * @author SmartAI-Assistant
 */
public enum AgentState {
    /**
     * 空闲状态 - Agent等待任务
     */
    IDLE("空闲", "Agent等待新任务"),
    
    /**
     * 运行状态 - Agent正在执行任务
     */
    RUNNING("运行中", "Agent正在执行任务"),
    
    /**
     * 思考状态 - Agent正在分析问题
     */
    THINKING("思考中", "Agent正在分析问题和制定计划"),
    
    /**
     * 行动状态 - Agent正在执行工具调用
     */
    ACTING("执行中", "Agent正在调用工具执行任务"),
    
    /**
     * 观察状态 - Agent正在处理工具执行结果
     */
    OBSERVING("观察中", "Agent正在处理工具执行结果"),
    
    /**
     * 完成状态 - Agent成功完成任务
     */
    FINISHED("已完成", "Agent成功完成任务"),
    
    /**
     * 失败状态 - Agent执行失败
     */
    FAILED("执行失败", "Agent执行任务失败"),
    
    /**
     * 卡住状态 - Agent陷入循环或无法继续
     */
    STUCK("卡住", "Agent陷入循环或无法继续执行"),
    
    /**
     * 超时状态 - Agent执行超时
     */
    TIMEOUT("超时", "Agent执行任务超时"),
    
    /**
     * 暂停状态 - Agent暂停执行
     */
    PAUSED("暂停", "Agent暂停执行等待干预");

    private final String displayName;
    private final String description;

    AgentState(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否是终止状态
     */
    public boolean isTerminal() {
        return this == FINISHED || this == FAILED || this == TIMEOUT;
    }

    /**
     * 检查是否是活跃状态
     */
    public boolean isActive() {
        return this == RUNNING || this == THINKING || this == ACTING || this == OBSERVING;
    }

    /**
     * 检查是否可以接受新任务
     */
    public boolean canAcceptNewTask() {
        return this == IDLE || this == FINISHED || this == FAILED;
    }

    /**
     * 检查是否需要干预
     */
    public boolean needsIntervention() {
        return this == STUCK || this == FAILED || this == PAUSED;
    }
}