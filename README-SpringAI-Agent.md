# SpringAI智能Agent系统

## 系统概述

基于SpringAI 1.0.0的**ChatClient**和**Function Calling**机制构建的智能Agent系统，具备任务规划、工具调用、自主执行等核心能力。

### 核心特性

- ✅ **基于SpringAI原生能力**：使用ChatClient + Function Calling
- ✅ **智能任务规划**：自动分解复杂任务为可执行步骤
- ✅ **工具函数调用**：支持RAG、会议API、PDF生成、网络搜索等
- ✅ **流式响应处理**：支持实时反馈和异步处理
- ✅ **对话记忆管理**：维护上下文和对话历史
- ✅ **错误处理重试**：智能错误恢复和重新规划

## 架构设计

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   用户请求      │───▶│ AgentController │───▶│SpringAIAgent    │
└─────────────────┘    └─────────────────┘    │   Service       │
                                              └─────────────────┘
                                                       │
                                                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ChatMemory    │◀───│   ChatClient    │◀───│ Function        │
└─────────────────┘    └─────────────────┘    │ Registry        │
                                              └─────────────────┘
                                                       │
                                                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  RAGFunction    │    │TencentMeeting   │    │  PDFFunction    │
│                 │    │   Function      │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 核心组件

### 1. SpringAI ChatClient

**位置**: `SpringAIAgentService`

```java
@Service
public class SpringAIAgentService implements IAgentService {
    
    private final ChatClient chatClient;
    
    public SpringAIAgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是智能任务规划和执行助手...")
                .defaultTools(
                        ragTool,              // RAG工具
                        tencentMeetingTool,   // 腾讯会议工具
                        pdfTool,              // PDF生成工具
                        webSearchTool         // 网络搜索工具
                )
                .build();
    }
}
```

### 2. Tools机制 (新版本)

**基于SpringAI的@Tool注解**：

```java
import org.springframework.ai.tool.annotation.Tool;

@Component
public class RAGTool {
    
    @Tool(description = "在知识库中进行语义搜索，返回相关文档片段")
    public RAGSearchResponse ragSearch(RAGSearchRequest request) {
        // 实现RAG搜索逻辑
        return new RAGSearchResponse(...);
    }
}
```

**迁移说明**：
- SpringAI 1.0.0-M8+ 版本推荐使用 `@Tool` 注解替代 `@Bean + @Description`
- 使用 `.defaultTools()` 替代 `.defaultFunctions()`
- 工具类直接作为Spring组件注入，更加简洁

### 3. 可用工具函数

| 函数名 | 描述 | 输入 | 输出 |
|--------|------|------|------|
| `ragSearch` | RAG语义搜索 | 查询、知识库、topK | 文档片段列表 |
| `ragQA` | RAG知识问答 | 问题、知识库、上下文数 | 答案和来源 |
| `getMeetings` | 获取会议列表 | 时间范围、用户ID | 会议信息列表 |
| `getMeetingDetail` | 获取会议详情 | 会议ID、用户ID | 详细会议信息 |
| `downloadMeetingRecord` | 下载会议录制 | 会议ID、录制ID、路径 | 下载结果 |
| `transcribeMeeting` | 转录会议音频 | 音频路径、语言 | 转录文本 |
| `generatePdfReport` | 生成PDF报告 | 标题、内容、模板 | PDF文件信息 |
| `generateExecutionPlanPdf` | 生成执行计划PDF | 计划内容 | PDF文件信息 |
| `generateMeetingSummaryPdf` | 生成会议纪要PDF | 会议内容 | PDF文件信息 |
| `webSearch` | 网络搜索 | 查询、语言、结果数 | 搜索结果列表 |

## API使用指南

### 1. 执行复杂任务

```bash
POST /api/v1/agent/execute
Content-Type: application/json

{
  "taskDescription": "获取2024年1月的会议纪要，进行语音转录，切片存入meeting_minutes知识库，最后生成包含会议总结的执行计划PDF报告",
  "taskType": "meeting_analysis",
  "parameters": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "knowledgeBase": "meeting_minutes",
    "reportTitle": "月度会议分析报告"
  },
  "async": true,
  "timeoutMinutes": 90,
  "userId": "admin"
}
```

### 2. 流式任务处理

```bash
POST /api/v1/agent/stream
Content-Type: application/json

{
  "taskDescription": "分析最近的会议趋势并生成报告",
  "parameters": {
    "analysisType": "trend_analysis"
  }
}
```

### 3. 快速知识问答

```bash
POST /api/v1/agent/quick/knowledge-qa
Content-Type: application/json

{
  "question": "SpringAI ChatClient有哪些核心功能？",
  "knowledge": "technical_docs",
  "contextSize": 5,
  "userId": "user123"
}
```

### 4. 快速报告生成

```bash
POST /api/v1/agent/quick/generate-report
Content-Type: application/json

{
  "title": "SpringAI Agent系统技术报告",
  "content": "本报告详细介绍了基于SpringAI的智能Agent系统...",
  "outputPath": "/reports/springai-agent-tech-report.pdf",
  "userId": "tech_lead"
}
```

## 配置说明

### 1. 应用配置

**application.yml**:
```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        enabled: true
        options:
          model: deepseek-r1:1.5b
          temperature: 0.7
      embedding:
        enabled: true
        options:
          model: nomic-embed-text

  datasource:
    url: jdbc:postgresql://localhost:5432/springai
    username: postgres
    password: postgres

  data:
    redis:
      host: localhost
      port: 16379
```

### 2. ChatClient配置

**SpringAIConfig.java**:
```java
@Configuration
public class SpringAIConfig {
    
    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("智能助手系统提示...");
    }
    
    @Bean("agentChatClient")
    public ChatClient agentChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("智能任务执行代理...")
                .build();
    }
}
```

## 运行和测试

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

# 带演示模式启动
mvn spring-boot:run -pl dev-fetch-app -Dspring-boot.run.arguments=demo
```

### 3. 功能验证

```bash
# 检查服务状态
curl http://localhost:8090/api/v1/agent/status

# 简单任务测试
curl -X POST http://localhost:8090/api/v1/agent/quick/knowledge-qa \
  -H "Content-Type: application/json" \
  -d '{
    "question": "SpringAI的主要功能是什么？",
    "knowledge": "technical_docs",
    "contextSize": 3,
    "userId": "test"
  }'
```

## 示例场景

### 场景1：会议智能分析

```
用户输入：
"分析上周所有会议的讨论内容，识别重要决策点，生成执行计划"

Agent执行步骤：
1. 调用getMeetings获取上周会议列表
2. 对每个会议调用getMeetingDetail获取详情  
3. 调用downloadMeetingRecord下载录制文件
4. 调用transcribeMeeting转录音频内容
5. 调用ragSearch分析讨论内容
6. 调用generateExecutionPlanPdf生成执行计划
```

### 场景2：知识整合研究

```
用户输入：
"研究SpringAI最新发展，结合我们的技术文档，生成技术调研报告"

Agent执行步骤：
1. 调用webSearch搜索SpringAI最新信息
2. 调用ragSearch搜索内部技术文档
3. 调用ragQA提取关键技术点
4. 整合分析所有信息
5. 调用generatePdfReport生成技术报告
```

## 核心优势

### 相比自定义实现的优势

1. **原生集成**：直接使用SpringAI的ChatClient和Function Calling
2. **标准化**：符合SpringAI的设计模式和最佳实践
3. **易维护**：跟随SpringAI版本更新，无需维护自定义框架
4. **高性能**：利用SpringAI内置的优化和缓存机制
5. **扩展性**：轻松添加新的Function Bean即可扩展功能

### 技术特点

- **声明式Function定义**：使用@Bean + @Description
- **类型安全**：Record类型的请求/响应参数
- **自动注册**：SpringAI自动发现和注册Function Bean
- **流式支持**：原生支持Flux流式响应
- **Advisor机制**：支持RAG增强和记忆管理

## 扩展开发

### 添加新的工具函数

1. 创建Function配置类：
```java
@Configuration  
public class NewToolFunction {
    
    @Bean
    @Description("新工具的描述")
    public Function<RequestType, ResponseType> newTool() {
        return request -> {
            // 实现逻辑
        };
    }
}
```

2. 在ChatClient中注册：
```java
.defaultFunctions("newTool")
```

### 自定义Advisor

```java
@Bean
public RequestResponseAdvisor customAdvisor() {
    return new CustomAdvisor();
}
```

## 部署建议

- **本地开发**：使用Ollama + PostgreSQL + Redis
- **测试环境**：Docker Compose一键部署
- **生产环境**：Kubernetes + 云服务集成
- **监控告警**：集成SpringBoot Actuator + Prometheus

这个基于SpringAI原生能力的Agent系统为您提供了企业级的智能任务自动化解决方案！