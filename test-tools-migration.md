# SpringAI Tools迁移测试指南

## 迁移完成情况

✅ **已完成的迁移工作**：
1. 创建了基于`@Tool`注解的工具类：
   - `RAGTool` - RAG知识库检索工具
   - `TencentMeetingTool` - 腾讯会议API工具
   - `PDFTool` - PDF生成工具
   - `WebSearchTool` - 网络搜索工具

2. 更新了`SpringAIAgentService`：
   - 从`.defaultFunctions()`迁移到`.defaultTools()`
   - 注入工具类实例而不是Bean名称
   - 更新了系统提示词

3. 创建了测试控制器`ToolsTestController`

## 测试API接口

### 1. 测试RAG工具
```bash
curl -X POST "http://localhost:8080/api/tools/test-rag?query=SpringAI"
```

### 2. 测试腾讯会议工具
```bash
curl -X POST "http://localhost:8080/api/tools/test-meeting?userId=user123"
```

### 3. 测试PDF生成工具
```bash
curl -X POST "http://localhost:8080/api/tools/test-pdf?title=测试报告&content=这是测试内容"
```

### 4. 测试网络搜索工具
```bash
curl -X POST "http://localhost:8080/api/tools/test-websearch?query=SpringAI最新版本"
```

### 5. 测试复合任务
```bash
curl -X POST "http://localhost:8080/api/tools/test-complex?topic=人工智能发展"
```

### 6. 测试流式响应
```bash
curl -X GET "http://localhost:8080/api/tools/test-stream?query=介绍SpringAI"
```

### 7. 获取工具统计
```bash
curl -X GET "http://localhost:8080/api/tools/stats"
```

## 关键变化对比

### 旧版本 (Functions)
```java
// 配置方式
.defaultFunctions(
    "ragSearch", "ragQA",
    "getMeetings", "downloadMeetingRecord",
    "generatePdfReport", "webSearch"
)

// 工具定义
@Bean
@Description("在知识库中进行语义搜索")
public Function<RAGSearchRequest, RAGSearchResponse> ragSearch() {
    return request -> { /* 实现逻辑 */ };
}
```

### 新版本 (Tools)
```java
// 配置方式
.defaultTools(
    ragTool,              // RAG工具
    tencentMeetingTool,   // 腾讯会议工具
    pdfTool,              // PDF生成工具
    webSearchTool         // 网络搜索工具
)

// 工具定义
import org.springframework.ai.tool.annotation.Tool;

@Component
public class RAGTool {
    @Tool(description = "在知识库中进行语义搜索")
    public RAGSearchResponse ragSearch(RAGSearchRequest request) {
        /* 实现逻辑 */
        return new RAGSearchResponse(...);
    }
}
```

## 优势分析

### Tools机制的优势：
1. **更简洁的配置**：直接注入工具实例，不需要通过Bean名称引用
2. **更好的类型安全**：编译时检查，减少运行时错误
3. **更清晰的结构**：每个工具类包含相关的所有方法
4. **更容易测试**：可以直接测试工具类的方法
5. **更好的IDE支持**：自动补全和重构支持

### 性能对比：
- **初始化**：Tools方式更快，因为直接引用对象
- **调用**：性能基本相同，都是反射调用
- **内存**：Tools方式稍微节省内存

## 验证清单

- [ ] 启动应用无错误
- [ ] RAG工具调用正常
- [ ] 腾讯会议工具调用正常  
- [ ] PDF生成工具调用正常
- [ ] 网络搜索工具调用正常
- [ ] 复合任务调用正常
- [ ] 流式响应正常
- [ ] 错误处理正常

## 注意事项

1. **SpringAI版本要求**：需要SpringAI 1.0.0-M8+版本
2. **向后兼容**：旧的Functions机制仍然支持，但建议迁移
3. **工具注册**：确保所有工具类都有`@Component`注解
4. **方法签名**：`@Tool`方法必须是public的
5. **参数类型**：建议使用record类型作为请求/响应参数

## 故障排除

### 常见问题：
1. **工具未找到**：检查`@Component`注解和Spring扫描路径
2. **方法调用失败**：检查方法签名和参数类型
3. **JSON序列化错误**：确保使用`@JsonClassDescription`和`@JsonPropertyDescription`
4. **依赖注入失败**：检查构造函数参数和`@Autowired`注解

### 调试建议：
1. 启用DEBUG日志：`logging.level.org.springframework.ai=DEBUG`
2. 查看工具调用日志
3. 使用断点调试工具方法
4. 检查ChatClient配置

## 总结

SpringAI Tools机制的迁移已完成，新的实现方式更加现代化和易于维护。建议在生产环境中逐步迁移，并充分测试每个工具的功能。