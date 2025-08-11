# 智能Agent使用示例

## 系统概述

本智能Agent系统基于SpringAI + SpringBoot3 + RAG + ToolCalling构建，能够：
- **自主规划**：将复杂任务分解为可执行步骤
- **工具选择**：智能选择合适的工具执行任务
- **循环执行**：失败时自动重试和重新规划
- **结果整合**：生成完整的执行报告

## 主要功能

### 1. 会议分析流程
自动获取腾讯会议数据，转录音频，切片入库，生成报告

### 2. RAG知识问答
基于向量数据库的语义检索和智能问答

### 3. 文档生成
支持多种格式的PDF报告生成

### 4. 任务编排
智能任务分解和执行规划

## API使用示例

### 1. 执行复杂会议分析任务

```http
POST /api/v1/agent/execute
Content-Type: application/json

{
  "taskDescription": "获取2024年1月1日到1月7日的会议纪要，进行语音转录，切片存入meeting_minutes知识库，最后生成一份包含会议总结的执行计划PDF报告",
  "taskType": "meeting_analysis",
  "parameters": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-07",
    "knowledgeBase": "meeting_minutes",
    "reportTitle": "会议分析报告",
    "outputPath": "/reports/meeting-analysis-2024-01.pdf"
  },
  "priority": 2,
  "timeoutMinutes": 90,
  "userId": "admin",
  "async": true
}
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "任务已开始异步执行",
  "data": {
    "taskId": "task_20240115_001",
    "taskDescription": "获取2024年1月1日到1月7日的会议纪要...",
    "status": "RUNNING",
    "progress": 0,
    "startTime": "2024-01-15T10:30:00",
    "executionSteps": []
  }
}
```

### 2. 查询任务执行状态

```http
GET /api/v1/agent/status/task_20240115_001
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "taskId": "task_20240115_001",
    "status": "RUNNING",
    "progress": 60,
    "executionSteps": [
      {
        "stepIndex": 1,
        "stepName": "获取会议列表",
        "status": "COMPLETED",
        "toolUsed": "get_meetings",
        "duration": 5000,
        "result": {
          "meetingCount": 15,
          "meetings": [...]
        }
      },
      {
        "stepIndex": 2,
        "stepName": "下载会议录制",
        "status": "RUNNING",
        "toolUsed": "download_meeting_record",
        "startTime": "2024-01-15T10:35:00"
      }
    ],
    "usedTools": ["get_meetings", "download_meeting_record"],
    "duration": 300000
  }
}
```

### 3. 快速知识库问答

```http
POST /api/v1/agent/quick/knowledge-qa
Content-Type: application/json

{
  "question": "最近会议中讨论了哪些重要议题？",
  "knowledge": "meeting_minutes",
  "contextSize": 5,
  "userId": "user123"
}
```

### 4. 快速生成报告

```http
POST /api/v1/agent/quick/generate-report
Content-Type: application/json

{
  "title": "月度工作总结",
  "content": "本月完成了以下主要工作：1. 系统升级 2. 数据迁移 3. 性能优化...",
  "outputPath": "/reports/monthly-summary.pdf",
  "userId": "manager"
}
```

### 5. 获取支持的工具列表

```http
GET /api/v1/agent/tools
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": [
    "rag_search",
    "rag_qa",
    "get_meetings",
    "get_meeting_records",
    "download_meeting_record",
    "transcribe_meeting",
    "generate_execution_plan_pdf",
    "generate_meeting_summary_pdf",
    "web_search",
    "calculate",
    "get_current_time"
  ]
}
```

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   用户请求      │───▶│  AgentController │───▶│  AgentService   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ToolRegistry  │◀───│  TaskPlanner    │◀───│  AgentExecutor  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ToolInvoker   │    │   TaskPlan      │    │   TaskResult    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │
         ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     RAGTool     │    │ TencentMeeting  │    │  PDFGenerator   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 工具说明

### RAG工具
- `rag_search`: 语义搜索知识库
- `rag_qa`: 基于知识库回答问题
- `rag_count`: 统计文档数量

### 会议工具
- `get_meetings`: 获取会议列表
- `get_meeting_detail`: 获取会议详情
- `get_meeting_records`: 获取录制文件
- `download_meeting_record`: 下载录制文件
- `transcribe_meeting`: 音频转文本

### 文档工具
- `generate_execution_plan_pdf`: 生成执行计划PDF
- `generate_meeting_summary_pdf`: 生成会议纪要PDF
- `generate_report_pdf`: 生成通用报告PDF

### 辅助工具
- `web_search`: 网络搜索
- `calculate`: 数学计算
- `get_current_time`: 获取当前时间

## 配置说明

### 环境变量
```bash
# 腾讯会议API
TENCENT_MEETING_APP_ID=your_app_id
TENCENT_MEETING_SECRET=your_secret_key

# 数据库
POSTGRES_URL=jdbc:postgresql://localhost:5432/springai
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=16379
```

### 启动依赖服务
```bash
# 启动基础服务
cd docs/docker
docker-compose up -d

# 安装Ollama模型
ollama pull deepseek-r1:1.5b
ollama pull nomic-embed-text
```

## 部署指南

1. **环境准备**
   - Java 21+
   - Maven 3.8+
   - PostgreSQL 15+ (带pgvector扩展)
   - Redis 6.2+
   - Ollama服务

2. **编译运行**
   ```bash
   mvn clean compile
   mvn spring-boot:run -pl dev-fetch-app
   ```

3. **验证部署**
   ```bash
   curl http://localhost:8090/api/v1/agent/tools
   ```

## 错误代码说明

- `0000`: 成功
- `1001`: 参数错误
- `1002`: 任务不存在或操作失败
- `9999`: 系统错误

## 注意事项

1. **异步执行**：复杂任务建议使用异步模式，通过轮询获取状态
2. **超时设置**：根据任务复杂度合理设置超时时间
3. **资源限制**：系统限制最大并发任务数为10个
4. **重试机制**：失败步骤会自动重试最多3次
5. **重新规划**：连续失败时会触发重新规划，最多2次

## 扩展开发

### 添加新工具
1. 创建工具类，继承相应的基类
2. 使用`@Tool`注解标记方法
3. 在`ToolRegistry`中会自动发现和注册

### 自定义任务类型
1. 在`TaskPlannerService`中添加新的任务类型处理逻辑
2. 更新规划提示词模板
3. 添加相应的API接口

这个智能Agent系统为您提供了强大的任务自动化能力，可以根据需要进一步扩展和定制。