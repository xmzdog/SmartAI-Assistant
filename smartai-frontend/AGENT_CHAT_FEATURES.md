# Agent对话页面功能说明

## 新增功能概览

基于docs/nginx/index.html的功能实现，我们为Agent对话页面添加了以下新功能：

### 1. 提示词模板选择
- 新增提示词模板下拉选择器
- 支持选择预定义的提示词模板
- 在对话时自动应用选中的模板

### 2. 流式对话响应
- 使用EventSource实现实时流式输出
- 支持打字机效果的实时显示
- 自动降级到普通对话（兼容性处理）
- 流式响应指示器（打字动画）

### 3. 文件上传功能
- 支持拖拽上传多个文件
- 可指定知识库名称和标签
- 上传进度显示和状态管理
- 自动刷新知识库列表

### 4. Git仓库解析
- 支持输入Git仓库地址
- 可指定分支名称
- 自动解析仓库内容到知识库
- 支持私有和公开仓库

### 5. 改进的聊天历史管理
- 自动保存聊天记录
- 支持删除单个对话
- 更好的历史对话预览
- 智能的对话切换提示

### 6. 增强的Markdown渲染
- 代码高亮支持
- 更好的代码块显示
- 支持GitHub风格的Markdown
- 响应式代码显示

### 7. 用户体验改进
- 自动保存功能（2秒延迟）
- 更好的错误处理
- 流式响应状态指示
- 响应式设计优化

## 技术实现

### 流式对话
```javascript
// 使用EventSource实现流式响应
const eventSource = new EventSource(apiUrl)
eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data)
  // 实时更新UI
}
```

### 文件上传
```javascript
// 支持多文件上传
const formData = new FormData()
files.forEach(file => {
  formData.append('files', file)
})
```

### 自动保存
```javascript
// 监听消息变化，自动保存
watch(currentMessages, (newMessages) => {
  if (newMessages.length > 0) {
    setTimeout(() => saveChatRecord(), 2000)
  }
}, { deep: true })
```

## API接口

### 新增接口
- `GET /api/v1/ai/prompt/templates` - 获取提示词模板
- `POST /api/v1/ai/agent/chat_stream` - 流式对话
- `POST /api/v1/ai/agent/parse_git` - 解析Git仓库
- `POST /api/v1/ai/agent/build_local` - 构建本地仓库
- `GET /api/v1/ai/agent/chat/history` - 获取聊天历史
- `POST /api/v1/ai/agent/chat/save` - 保存聊天记录
- `DELETE /api/v1/ai/agent/chat/{id}` - 删除聊天记录

## 使用说明

### 开始对话
1. 选择智能体
2. 选择知识库（可选）
3. 选择提示词模板（可选）
4. 输入问题开始对话

### 上传知识
1. 点击"上传知识"按钮
2. 选择"上传文件"或"解析Git仓库"
3. 填写相关信息
4. 开始上传/解析

### 管理对话历史
1. 左侧面板显示所有对话
2. 点击对话可切换
3. 右键可删除对话
4. 自动保存当前对话

## 注意事项

1. 流式对话需要浏览器支持EventSource
2. 文件上传支持常见文档格式
3. Git仓库解析需要有效的仓库地址
4. 聊天记录会自动保存，无需手动操作
5. 代码高亮需要引入highlight.js库

## 兼容性

- 现代浏览器：完整功能支持
- 旧版浏览器：自动降级到普通对话
- 移动端：响应式设计，功能完整
- 网络异常：自动重试和错误提示
