# 基于OpenManus架构的分层智能体系统

## 系统概述

参考OpenManus的设计理念，实现了一个**分层智能体系统**，结合SpringAI的原生能力，提供了从基础到高级的智能体功能体系。

### 核心设计理念

✅ **分层架构设计** - 参考OpenManus的BaseAgent -> ReActAgent -> ToolCallAgent -> Manus层次结构  
✅ **CoT深度推理** - 实现Chain-of-Thought推理机制，支持多种推理策略  
✅ **SpringAI集成** - 完美结合SpringAI的ChatClient和Function Calling能力  
✅ **ReAct模式** - Think-Act-Observe循环，实现推理驱动的行动  
✅ **自主规划能力** - 智能任务分解、工具选择、执行监控  

## 架构层次

```
BaseAgent (基础智能体)
    ↓
ReActAgent (推理-行动智能体)
    ↓  
ToolCallAgent (工具调用智能体)
    ↓
ManusAgent (通用多功能智能体)
```

### 各层职责

| 层次 | 核心功能 | 主要职责 |
|------|----------|----------|
| **BaseAgent** | 基础框架 | 状态管理、记忆管理、执行控制、错误处理 |
| **ReActAgent** | 推理循环 | Think-Act-Observe循环、逐步推理、任务完成判断 |
| **ToolCallAgent** | 工具调用 | 工具发现选择、调用执行、结果处理、错误恢复 |
| **ManusAgent** | 综合能力 | CoT推理、SpringAI集成、任务规划、自主执行 |

## 核心组件详解

### 1. BaseAgent - 基础智能体框架

**位置**: `com.onepiece.xmz.app.agent.manus.BaseAgent`

```java
public abstract class BaseAgent {
    protected AgentState state;          // 智能体状态
    protected AgentMemory memory;        // 对话记忆
    protected ChatClient chatClient;     // SpringAI客户端
    protected int maxSteps;              // 最大执行步数
    protected AtomicInteger currentStep; // 当前步数
    
    // 核心方法
    public String run(String request);   // 主执行方法
    public abstract String step();       // 单步执行
    public boolean isStuck();           // 卡住检测
}
```

**核心能力**:
- **状态管理**: 10种智能体状态（IDLE、RUNNING、THINKING、ACTING等）
- **记忆管理**: 对话历史、工具调用记录、上下文维护
- **执行控制**: 步数限制、超时检测、错误恢复
- **生命周期**: 创建、运行、暂停、恢复、清理

### 2. ReActAgent - 推理-行动智能体

**位置**: `com.onepiece.xmz.app.agent.manus.ReActAgent`

```java
public abstract class ReActAgent extends BaseAgent {
    protected abstract boolean think();  // 思考阶段
    protected abstract String act();     // 行动阶段
    protected boolean observe(String actionResult); // 观察阶段
    
    // ReAct循环执行
    protected String execute(String request) {
        while (currentStep.get() < maxSteps && !isStuck()) {
            setState(AgentState.THINKING);
            boolean shouldAct = think();
            
            if (shouldAct) {
                setState(AgentState.ACTING);
                String result = act();
                
                setState(AgentState.OBSERVING);
                boolean shouldContinue = observe(result);
                
                if (!shouldContinue) break;
            }
        }
    }
}
```

**ReAct模式特点**:
- **Think (思考)**: 分析当前情况，理解问题，制定计划
- **Act (行动)**: 执行具体的行动或工具调用
- **Observe (观察)**: 观察行动结果，更新理解，决定下一步

### 3. ToolCallAgent - 工具调用智能体

**位置**: `com.onepiece.xmz.app.agent.manus.ToolCallAgent`

```java
public abstract class ToolCallAgent extends ReActAgent {
    protected Map<String, String> availableTools;     // 可用工具
    protected List<ToolCall> currentToolCalls;        // 当前工具调用
    protected ToolSelectionStrategy toolSelectionStrategy; // 工具选择策略
    
    @Override
    protected boolean think() {
        // 分析任务并选择工具
        String analysis = reasoning(buildAnalysisPrompt());
        List<ToolCall> selectedTools = selectTools(analysis);
        this.currentToolCalls = selectedTools;
        return !selectedTools.isEmpty();
    }
    
    @Override
    protected String act() {
        // 执行选定的工具调用
        StringBuilder results = new StringBuilder();
        for (ToolCall toolCall : currentToolCalls) {
            String result = executeToolCall(toolCall);
            results.append(result).append("\n");
        }
        return results.toString();
    }
}
```

**工具调用流程**:
1. **工具发现**: 基于任务需求智能识别需要的工具
2. **工具选择**: 支持AUTO、MANUAL、HYBRID三种选择策略  
3. **执行管理**: 并发执行、超时控制、错误处理
4. **结果处理**: 结果解析、状态更新、历史记录

### 4. CoT推理引擎

**位置**: `com.onepiece.xmz.app.agent.manus.CoTReasoning`

```java
public class CoTReasoning {
    // 推理策略
    public enum ReasoningStrategy {
        STEP_BY_STEP,           // 逐步推理
        PROBLEM_DECOMPOSITION,  // 问题分解
        ANALYTICAL,             // 分析式推理
        COMPREHENSIVE,          // 综合推理
        BASIC                   // 基础推理
    }
    
    // 执行深度推理
    public ReasoningChain deepReason(String problem, ReasoningStrategy strategy) {
        switch (strategy) {
            case COMPREHENSIVE:
                return comprehensiveReasoning(chain);
            case PROBLEM_DECOMPOSITION:
                return problemDecompositionReasoning(chain);
            // ... 其他策略
        }
    }
}
```

**推理策略详解**:

| 策略 | 适用场景 | 推理步骤 |
|------|----------|----------|
| **STEP_BY_STEP** | 线性问题 | 理解→规划→执行→验证 |
| **PROBLEM_DECOMPOSITION** | 复杂问题 | 分解→逐个求解→综合结果 |
| **ANALYTICAL** | 逻辑分析 | 条件分析→逻辑推导→结论验证 |
| **COMPREHENSIVE** | 综合问题 | 多维分析→多路径推理→结果整合 |

### 5. ManusAgent - 通用智能体

**位置**: `com.onepiece.xmz.app.agent.manus.ManusAgent`

```java
@Component
public class ManusAgent extends ToolCallAgent {
    private CoTReasoning cotReasoning;              // CoT推理引擎
    private ChatClient springAIChatClient;          // SpringAI客户端
    private Map<String, Object> taskContext;       // 任务上下文
    
    @Override
    protected String execute(String request) {
        // 智能执行策略选择
        if (shouldUseDeepReasoning(request)) {
            return executeWithCoTReasoning(request);
        }
        if (shouldUseSpringAIFunctions(request)) {
            return executeWithSpringAIFunctions(request);
        }
        return super.execute(request); // 标准ReAct循环
    }
}
```

**ManusAgent核心特性**:
- **智能策略选择**: 根据任务复杂度自动选择执行策略
- **CoT深度推理**: 复杂问题使用Chain-of-Thought推理
- **SpringAI集成**: 无缝集成SpringAI的Function Calling
- **任务规划**: 自动分解复杂任务为可执行步骤
- **多模式执行**: 支持同步、异步、流式等多种执行模式

## API接口说明

### HTTP API端点

**基础路径**: `/api/v1/manus`

| 端点 | 方法 | 功能 | 说明 |
|------|------|------|------|
| `/execute` | POST | 执行任务 | 支持同步/异步执行 |
| `/execute` | GET | 快速执行 | 简单任务快速执行 |
| `/reasoning` | POST | 深度推理 | CoT推理分析 |
| `/task/{id}/status` | GET | 任务状态 | 查询异步任务状态 |
| `/task/{id}/result` | GET | 任务结果 | 获取任务执行结果 |
| `/status` | GET | Agent状态 | 获取智能体状态 |
| `/config` | POST | 配置管理 | 修改运行参数 |

### 使用示例

#### 1. 基础任务执行

```bash
curl -X POST http://localhost:8090/api/v1/manus/execute \
  -H "Content-Type: application/json" \
  -d '{
    "task": "请分析SpringAI框架的主要优势",
    "async": false
  }'
```

#### 2. 深度推理分析

```bash
curl -X POST http://localhost:8090/api/v1/manus/reasoning \
  -H "Content-Type: application/json" \
  -d '{
    "problem": "如何设计一个高可用的微服务架构？",
    "strategy": "COMPREHENSIVE"
  }'
```

#### 3. 复杂任务异步执行

```bash
curl -X POST http://localhost:8090/api/v1/manus/execute \
  -H "Content-Type: application/json" \
  -d '{
    "task": "分析最近的技术会议纪要，生成技术趋势报告PDF",
    "async": true,
    "taskId": "complex-task-001"
  }'
```

#### 4. Agent配置管理

```bash
# 启用CoT推理
curl -X POST http://localhost:8090/api/v1/manus/config \
  -H "Content-Type: application/json" \
  -d '{
    "property": "cot_enabled",
    "value": "true"
  }'

# 设置工具选择策略
curl -X POST http://localhost:8090/api/v1/manus/config \
  -H "Content-Type: application/json" \
  -d '{
    "property": "tool_selection_strategy", 
    "value": "HYBRID"
  }'
```

## 部署和运行

### 1. 环境准备

```bash
# 启动基础服务
cd docs/docker
docker-compose up -d

# 安装Ollama模型
ollama pull deepseek-r1:1.5b
ollama pull nomic-embed-text
```

### 2. 启动应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run -pl dev-fetch-app

# 启动演示模式
mvn spring-boot:run -pl dev-fetch-app -Dspring-boot.run.arguments=manus-demo
```

### 3. 功能验证

```bash
# 健康检查
curl http://localhost:8090/api/v1/manus/health

# 查看Agent状态
curl http://localhost:8090/api/v1/manus/status

# 快速测试
curl "http://localhost:8090/api/v1/manus/execute?task=介绍一下SpringAI"
```

## 应用场景示例

### 场景1: 技术文档分析

```json
{
  "task": "分析SpringAI 1.0.0的新特性，总结主要改进点，并生成技术评估PDF报告",
  "description": "技术文档深度分析任务",
  "async": true
}
```

**执行流程**:
1. **任务分析**: ManusAgent分析任务复杂度为HIGH
2. **策略选择**: 选择CoT深度推理 + SpringAI Functions
3. **推理过程**: 使用COMPREHENSIVE策略分析SpringAI特性
4. **工具调用**: 调用ragSearch、webSearch、generatePdfReport
5. **结果整合**: 生成完整的技术评估报告

### 场景2: 会议智能分析

```json
{
  "task": "获取上周的技术会议纪要，提取关键决策点，分析技术趋势，生成执行计划PDF",
  "strategy": "PROBLEM_DECOMPOSITION"
}
```

**执行流程**:
1. **问题分解**: 拆分为会议获取→内容分析→趋势提取→计划生成
2. **逐步执行**: 
   - 调用getMeetings获取会议列表
   - 调用getMeetingDetail获取详细内容  
   - 使用CoT分析关键决策和趋势
   - 调用generateExecutionPlanPdf生成计划
3. **结果验证**: 确保每个步骤输出质量

### 场景3: 知识问答推理

```json
{
  "problem": "为什么微服务架构在大型企业中越来越受欢迎？请从技术、业务、组织三个维度深入分析。",
  "strategy": "ANALYTICAL"
}
```

**推理过程**:
1. **条件分析**: 识别微服务vs单体架构的关键差异
2. **逻辑推导**: 从技术、业务、组织维度逐一分析
3. **结论验证**: 综合各维度分析得出结论

## 核心优势

### 相比传统Agent系统

| 特性 | 传统Agent | OpenManus分层Agent |
|------|-----------|-------------------|
| **架构设计** | 单一架构 | ✅ 分层渐进式设计 |
| **推理能力** | 基础对话 | ✅ CoT深度推理 |
| **工具集成** | 手动配置 | ✅ 智能选择调用 |
| **状态管理** | 简单状态 | ✅ 复杂状态机 |
| **错误恢复** | 基础重试 | ✅ 智能恢复机制 |
| **可扩展性** | 修改困难 | ✅ 层次化扩展 |

### 技术创新点

1. **分层Agent架构**: 参考OpenManus设计，实现渐进式能力构建
2. **CoT推理集成**: 5种推理策略，支持复杂问题深度分析  
3. **SpringAI原生集成**: 完美结合SpringAI的ChatClient和Function Calling
4. **智能策略选择**: 根据任务特征自动选择最优执行策略
5. **多模式执行**: 同步、异步、流式等多种执行模式
6. **全生命周期管理**: 从任务接收到结果交付的完整管理

## 扩展开发

### 添加新的推理策略

```java
// 在CoTReasoning中添加新策略
public enum ReasoningStrategy {
    // ... 现有策略
    CREATIVE("创造性推理");
}

private ReasoningChain creativeReasoning(ReasoningChain chain) {
    // 实现创造性推理逻辑
}
```

### 扩展工具能力

```java
// 在ManusAgent中添加新工具
@Override
protected void initializeManusCapabilities() {
    super.initializeManusCapabilities();
    availableTools.put("newTool", "新工具描述");
}

@Override
protected String doExecuteToolCall(ToolCall toolCall) {
    switch (toolCall.getName()) {
        case "newTool":
            return executeNewTool(toolCall.getParameters());
        default:
            return super.doExecuteToolCall(toolCall);
    }
}
```

### 自定义Agent

```java
@Component
public class CustomAgent extends ToolCallAgent {
    public CustomAgent() {
        super("CustomAgent", "自定义智能体");
        // 添加特定工具和配置
    }
    
    @Override
    protected String doExecuteToolCall(ToolCall toolCall) {
        // 实现特定的工具调用逻辑
    }
}
```

## 监控和运维

### 关键指标

- **执行成功率**: 任务完成情况统计
- **平均响应时间**: 不同复杂度任务的耗时分析
- **工具调用统计**: 各工具使用频率和成功率
- **推理策略效果**: 不同策略的适用性分析
- **错误恢复率**: 异常处理和恢复成功率

### 日志监控

```bash
# 查看Agent执行日志
tail -f logs/spring.log | grep -E "(ManusAgent|ReActAgent|ToolCallAgent)"

# 监控推理过程
tail -f logs/spring.log | grep "CoT推理"

# 工具调用监控
tail -f logs/spring.log | grep "工具调用"
```

这个基于OpenManus架构的分层智能体系统为您提供了**企业级、可扩展、智能化**的AI Agent解决方案，完美结合了OpenManus的设计理念和SpringAI的强大能力！