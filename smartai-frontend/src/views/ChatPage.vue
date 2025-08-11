<template>
  <div class="chat-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">智能对话</h1>
      <p class="page-description">基于知识库的智能问答对话系统</p>
    </div>

    <div class="chat-container">
      <!-- 左侧知识库选择面板 -->
      <div class="knowledge-panel">
        <div class="panel-header">
          <h3>知识库</h3>
          <el-button 
            size="small" 
            type="primary" 
            text
            @click="refreshKnowledgeBases"
            :loading="isRefreshingKB"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        
        <div class="knowledge-list">
          <div 
            v-for="kb in knowledgeBases" 
            :key="kb.id"
            class="knowledge-item"
            :class="{ active: selectedKnowledgeBase?.id === kb.id }"
            @click="selectKnowledgeBase(kb)"
          >
            <div class="kb-icon">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="kb-info">
              <div class="kb-name">{{ kb.name }}</div>
              <div class="kb-desc">{{ kb.description || '暂无描述' }}</div>
              <div class="kb-stats">
                <span>{{ kb.documentCount || 0 }} 文档</span>
                <span>{{ formatFileSize(kb.size || 0) }}</span>
              </div>
            </div>
          </div>
          
          <div v-if="knowledgeBases.length === 0" class="empty-state">
            <el-icon><DocumentRemove /></el-icon>
            <p>暂无知识库</p>
            <el-button size="small" type="primary" text>
              创建知识库
            </el-button>
          </div>
        </div>
      </div>

      <!-- 右侧对话区域 -->
      <div class="chat-area">
        <!-- 对话头部 -->
        <div class="chat-header">
          <div class="current-kb">
            <template v-if="selectedKnowledgeBase">
              <el-icon><Folder /></el-icon>
              <span>{{ selectedKnowledgeBase.name }}</span>
            </template>
            <template v-else>
              <el-icon><ChatDotRound /></el-icon>
              <span>请选择知识库开始对话</span>
            </template>
          </div>
          
          <div class="chat-actions">
            <el-button 
              size="small" 
              @click="clearHistory" 
              :disabled="messages.length === 0"
            >
              <el-icon><Delete /></el-icon>
              清空对话
            </el-button>
            
            <el-button 
              size="small" 
              @click="exportHistory" 
              :disabled="messages.length === 0"
            >
              <el-icon><Download /></el-icon>
              导出对话
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <h3>开始智能对话</h3>
            <p>选择知识库后，您可以向AI提问，获得基于知识库内容的准确回答</p>
            
            <div class="quick-questions" v-if="selectedKnowledgeBase">
              <h4>推荐问题</h4>
              <div class="question-list">
                <div 
                  v-for="question in suggestedQuestions" 
                  :key="question"
                  class="question-item"
                  @click="askQuestion(question)"
                >
                  {{ question }}
                </div>
              </div>
            </div>
          </div>

          <div 
            v-for="(message, index) in messages" 
            :key="index"
            class="message-item"
            :class="{ 'user': message.role === 'user', 'assistant': message.role === 'assistant' }"
          >
            <div class="message-avatar">
              <el-icon v-if="message.role === 'user'"><User /></el-icon>
              <el-icon v-else><Avatar /></el-icon>
            </div>
            
            <div class="message-content">
              <div class="message-header">
                <span class="message-role">
                  {{ message.role === 'user' ? '您' : 'AI助手' }}
                </span>
                <span class="message-time">
                  {{ formatTime(message.timestamp) }}
                </span>
              </div>
              
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              
              <!-- 引用来源 -->
              <div v-if="message.sources && message.sources.length > 0" class="message-sources">
                <div class="sources-header">
                  <el-icon><Document /></el-icon>
                  <span>参考来源</span>
                </div>
                <div class="sources-list">
                  <div 
                    v-for="source in message.sources" 
                    :key="source.id"
                    class="source-item"
                  >
                    <span class="source-name">{{ source.name }}</span>
                    <span class="source-score">相似度: {{ (source.similarity * 100).toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
              
              <!-- 消息操作 -->
              <div class="message-actions" v-if="message.role === 'assistant'">
                <el-button size="small" text @click="copyMessage(message.content)">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
                <el-button size="small" text @click="regenerateResponse(index)">
                  <el-icon><Refresh /></el-icon>
                  重新生成
                </el-button>
              </div>
            </div>
          </div>

          <!-- 加载中消息 -->
          <div v-if="isLoading" class="message-item assistant loading">
            <div class="message-avatar">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="message-role">AI助手</span>
                <span class="message-time">思考中...</span>
              </div>
              <div class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="请输入您的问题..."
              :disabled="!selectedKnowledgeBase || isLoading"
              @keydown.enter.prevent="handleKeyDown"
              class="message-input"
            />
            
            <div class="input-actions">
              <el-button
                type="primary"
                :disabled="!inputMessage.trim() || !selectedKnowledgeBase || isLoading"
                @click="sendMessage"
                :loading="isLoading"
              >
                <el-icon v-if="!isLoading"><Promotion /></el-icon>
                {{ isLoading ? '发送中...' : '发送' }}
              </el-button>
            </div>
          </div>
          
          <div class="input-tips">
            <span v-if="!selectedKnowledgeBase" class="tip-warning">
              <el-icon><Warning /></el-icon>
              请先选择知识库
            </span>
            <span v-else class="tip-info">
              按 Ctrl+Enter 发送消息
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import {
  Refresh,
  Folder,
  DocumentRemove,
  ChatDotRound,
  Delete,
  Download,
  User,
  Avatar,
  Document,
  CopyDocument,
  Promotion,
  Warning
} from '@element-plus/icons-vue'
import { knowledgeBaseApi } from '@/api/knowledgeBase'

// 路由
const route = useRoute()

// 响应式数据
const knowledgeBases = ref([])
const selectedKnowledgeBase = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const isRefreshingKB = ref(false)

// DOM引用
const messageListRef = ref(null)

// 推荐问题
const suggestedQuestions = computed(() => {
  if (!selectedKnowledgeBase.value) return []
  
  return [
    '这个知识库包含哪些内容？',
    '能否总结一下主要信息？',
    '有什么重要的注意事项？',
    '如何快速上手？'
  ]
})

// 生命周期
onMounted(() => {
  loadKnowledgeBases()
})

// 方法
const loadKnowledgeBases = async () => {
  try {
    isRefreshingKB.value = true
    const response = await knowledgeBaseApi.getKnowledgeBaseList()
    if (response.code === '0000') {
      // 将字符串数组转换为对象数组以适配现有组件
      knowledgeBases.value = (response.data || []).map(ragTag => ({
        id: ragTag,
        name: ragTag,
        description: `知识库: ${ragTag}`,
        documentCount: Math.floor(Math.random() * 10) + 1, // 临时随机数
        size: Math.floor(Math.random() * 1000000) + 100000 // 临时随机数
      }))
      
      // 检查URL参数，如果有预选知识库则自动选择
      const preSelectedKB = route.query.kb
      if (preSelectedKB) {
        const targetKB = knowledgeBases.value.find(kb => kb.id === preSelectedKB)
        if (targetKB) {
          selectedKnowledgeBase.value = targetKB
          ElMessage.success(`已自动选择知识库: ${targetKB.name}`)
        }
      }
    } else {
      knowledgeBases.value = []
    }
  } catch (error) {
    console.error('加载知识库失败:', error)
    ElMessage.error('加载知识库失败')
  } finally {
    isRefreshingKB.value = false
  }
}

const refreshKnowledgeBases = () => {
  loadKnowledgeBases()
}

const selectKnowledgeBase = (kb) => {
  if (selectedKnowledgeBase.value?.id === kb.id) return
  
  // 如果有对话历史，询问是否切换
  if (messages.value.length > 0) {
    ElMessageBox.confirm(
      '切换知识库将清空当前对话历史，是否继续？',
      '确认切换',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      selectedKnowledgeBase.value = kb
      messages.value = []
    }).catch(() => {
      // 用户取消
    })
  } else {
    selectedKnowledgeBase.value = kb
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedKnowledgeBase.value || isLoading.value) {
    return
  }

  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date()
  }

  messages.value.push(userMessage)
  inputMessage.value = ''
  isLoading.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    const response = await knowledgeBaseApi.askQuestion(selectedKnowledgeBase.value.id, userMessage.content)

    if (response.code === '0000') {
      const assistantMessage = {
        role: 'assistant',
        content: response.data,
        sources: [], // 暂时没有来源信息
        timestamp: new Date()
      }
      messages.value.push(assistantMessage)
    } else {
      // 处理业务错误
      const errorMessage = {
        role: 'assistant',
        content: response.data || response.info || '抱歉，我无法回答您的问题。',
        timestamp: new Date()
      }
      messages.value.push(errorMessage)
    }
  } catch (error) {
    console.error('对话失败:', error)
    ElMessage.error('对话失败，请重试')
    
    // 添加错误消息
    const errorMessage = {
      role: 'assistant',
      content: '抱歉，我现在无法回答您的问题，请稍后重试。',
      timestamp: new Date()
    }
    messages.value.push(errorMessage)
  } finally {
    isLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const askQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const handleKeyDown = (event) => {
  if (event.ctrlKey && event.key === 'Enter') {
    sendMessage()
  }
}

const clearHistory = () => {
  ElMessageBox.confirm(
    '确定要清空所有对话记录吗？',
    '确认清空',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    messages.value = []
    ElMessage.success('对话记录已清空')
  }).catch(() => {
    // 用户取消
  })
}

const exportHistory = () => {
  if (messages.value.length === 0) return

  const content = messages.value.map(msg => {
    const role = msg.role === 'user' ? '用户' : 'AI助手'
    const time = formatTime(msg.timestamp)
    return `[${time}] ${role}: ${msg.content}`
  }).join('\n\n')

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `对话记录_${new Date().toISOString().slice(0, 10)}.txt`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('对话记录已导出')
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

const regenerateResponse = async (messageIndex) => {
  if (messageIndex <= 0 || isLoading.value) return

  const userMessage = messages.value[messageIndex - 1]
  if (userMessage.role !== 'user') return

  // 移除当前回复
  messages.value.splice(messageIndex, 1)
  
  isLoading.value = true
  
  try {
    const response = await chatWithKnowledgeBase({
      knowledgeBaseId: selectedKnowledgeBase.value.id,
      message: userMessage.content,
      history: messages.value.slice(0, messageIndex - 1).map(msg => ({
        role: msg.role,
        content: msg.content
      }))
    })

    const assistantMessage = {
      role: 'assistant',
      content: response.data.answer,
      sources: response.data.sources || [],
      timestamp: new Date()
    }

    messages.value.splice(messageIndex, 0, assistantMessage)
  } catch (error) {
    console.error('重新生成失败:', error)
    ElMessage.error('重新生成失败，请重试')
  } finally {
    isLoading.value = false
  }
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

const formatMessage = (content) => {
  // 简单的markdown格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

/* 知识库面板 */
.knowledge-panel {
  width: 300px;
  background: var(--bg-white);
  border-radius: 8px;
  border: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-light);
}

.panel-header h3 {
  margin: 0;
  color: var(--text-primary);
}

.knowledge-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.knowledge-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
}

.knowledge-item:hover {
  background: var(--fill-lighter);
}

.knowledge-item.active {
  background: var(--primary-color);
  color: white;
}

.knowledge-item.active .kb-desc,
.knowledge-item.active .kb-stats {
  color: rgba(255, 255, 255, 0.8);
}

.kb-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.knowledge-item.active .kb-icon {
  color: white;
}

.kb-info {
  flex: 1;
  min-width: 0;
}

.kb-name {
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.kb-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.kb-stats {
  font-size: 11px;
  color: var(--text-placeholder);
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: var(--text-placeholder);
}

/* 对话区域 */
.chat-area {
  flex: 1;
  background: var(--bg-white);
  border-radius: 8px;
  border: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-light);
}

.current-kb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: var(--text-primary);
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.welcome-icon {
  font-size: 64px;
  color: var(--primary-color);
  margin-bottom: 20px;
}

.welcome-message h3 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
}

.quick-questions {
  margin-top: 40px;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.quick-questions h4 {
  margin: 0 0 16px 0;
  color: var(--text-primary);
  text-align: center;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-item {
  padding: 12px 16px;
  background: var(--fill-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-regular);
}

.question-item:hover {
  background: var(--primary-color);
  color: white;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-item.user .message-avatar {
  background: var(--primary-color);
  color: white;
}

.message-item.assistant .message-avatar {
  background: var(--fill-light);
  color: var(--text-regular);
}

.message-content {
  flex: 1;
  max-width: calc(100% - 52px);
}

.message-item.user .message-content {
  text-align: right;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.message-item.user .message-header {
  flex-direction: row-reverse;
}

.message-role {
  font-weight: 500;
}

.message-text {
  background: var(--fill-lighter);
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-item.user .message-text {
  background: var(--primary-color);
  color: white;
}

.message-sources {
  margin-top: 12px;
  padding: 12px;
  background: var(--fill-lighter);
  border-radius: 8px;
  border-left: 3px solid var(--primary-color);
}

.sources-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.sources-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.source-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.source-name {
  color: var(--text-primary);
  font-weight: 500;
}

.source-score {
  color: var(--text-secondary);
}

.message-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.loading {
  opacity: 0.8;
}

.loading-dots {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--text-placeholder);
  animation: loading 1.4s infinite ease-in-out;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes loading {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 输入区域 */
.input-area {
  padding: 20px;
  border-top: 1px solid var(--border-light);
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.input-tips {
  margin-top: 8px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tip-warning {
  color: var(--warning-color);
}

.tip-info {
  color: var(--text-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }
  
  .knowledge-panel {
    width: 100%;
    height: 200px;
  }
  
  .message-item {
    flex-direction: column;
    gap: 8px;
  }
  
  .message-item.user {
    flex-direction: column;
  }
  
  .message-item.user .message-content {
    text-align: left;
  }
  
  .message-item.user .message-header {
    flex-direction: row;
  }
}
</style>