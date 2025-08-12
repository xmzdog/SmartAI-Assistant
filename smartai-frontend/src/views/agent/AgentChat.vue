<template>
  <div class="agent-chat-page page-container">
    <!-- 页面标题栏 -->
    <div class="page-header">
             <h1 class="page-title">AutoAgent 智能体 <span class="page-subtitle">基于AI智能体的自动化任务执行，支持知识库检索和提示词模板</span></h1>
    </div>
    
    <!-- 顶部配置栏 -->
    <div class="top-config-bar">
      <div class="config-left">
        <!-- 这里可以放其他左侧配置项 -->
      </div>
      
      <div class="config-center">
        <div class="config-item">
          <label>智能体:</label>
            <el-select
              v-model="selectedAgentId"
            placeholder="请选择智能体"
            @change="handleAgentChange"
            size="default"
            style="width: 180px"
            >
              <el-option
                v-for="agent in agentList"
                :key="agent.id"
                :label="agent.agentName"
                :value="agent.id"
              />
            </el-select>
        </div>

        <div class="config-item">
          <label>知识库:</label>
            <el-select
              v-model="selectedRagId"
            placeholder="选择知识库"
            @change="handleKnowledgeBaseChange"
            size="default"
            style="width: 180px"
          >
            <el-option label="无知识库" value="" />
              <el-option
                v-for="rag in ragList"
                :key="rag.id"
                :label="rag.name"
                :value="rag.id"
              />
            </el-select>
        </div>

                 <div class="config-item">
           <label>提示词:</label>
           <el-select
             v-model="selectedPromptTemplate"
             placeholder="选择提示词模板"
             @change="handlePromptTemplateChange"
             size="default"
             style="width: 180px"
           >
             <el-option label="无模板" value="" />
             <el-option
               v-for="template in promptTemplates"
               :key="template.id"
               :label="template.name"
               :value="template.id"
             />
           </el-select>
         </div>

         <div class="config-item">
           <label>最大步数:</label>
           <el-input-number 
             v-model="autoAgentConfig.maxStep" 
             :min="1" 
             :max="20"
             size="default"
             controls-position="right"
             style="width: 120px"
           />
         </div>
           </div>

      <div class="config-right">
        <el-button size="default" @click="refreshConfigs" :loading="isRefreshingConfigs" :icon="Refresh">
          刷新
            </el-button>

        <el-button size="default" type="primary" @click="startNewChat" :icon="Plus">
          新建对话
        </el-button>
        
        
        
        <el-button size="default" @click="clearChat" :icon="Delete" type="danger" plain>
          清空对话
        </el-button>
        
        <el-dropdown @command="handleBuildCommand">
          <el-button size="default" :icon="Tools">
            构建仓库 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="buildRepo">构建知识库</el-dropdown-item>
              <el-dropdown-item command="buildAgent">构建智能体</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown @command="handleUploadCommand">
          <el-button size="default" :icon="Upload">
            上传知识 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="uploadFiles">上传文件</el-dropdown-item>
              <el-dropdown-item command="parseGit">解析Git仓库</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="chat-container">
      <!-- 左侧历史对话面板 -->
      <div class="history-panel">
        <div class="panel-header">
          <h3>历史对话</h3>
          <div class="panel-actions">
            <el-button 
              size="small"
              type="primary" 
              text
              @click="loadChatHistory"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
        
        <div class="history-content">
          <!-- 历史对话列表 -->
          <div class="history-list">
            <div 
              v-for="(chat, index) in chatHistory" 
              :key="index"
              class="history-item"
              :class="{ active: currentChatIndex === index }"
              @click="loadHistoryChat(index)"
            >
              <div class="history-content">
                <div class="history-title">{{ chat.title || `对话 ${index + 1}` }}</div>
                <div class="history-time">{{ formatDate(chat.timestamp) }}</div>
                <div class="history-preview">{{ chat.preview }}</div>
              </div>
              <div class="history-actions">
                <el-button 
                  size="small" 
                  text 
                  @click.stop="deleteChatRecord(chat.id)"
                  class="delete-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div v-if="chatHistory.length === 0" class="empty-state">
              <el-icon><ChatDotRound /></el-icon>
              <p>暂无历史对话</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧对话区域 -->
      <div class="chat-area">
        <!-- 对话头部 -->
        <div class="chat-header">
          <div class="current-config">
            <template v-if="selectedAgentId">
              <el-icon><Avatar /></el-icon>
              <span>{{ getCurrentAgentName() }}</span>
              <template v-if="selectedRagId">
                <span class="separator">•</span>
                <el-icon><Folder /></el-icon>
                <span>{{ getCurrentRagName() }}</span>
              </template>
            </template>
            <template v-else>
              <el-icon><Warning /></el-icon>
              <span>请选择智能体开始对话</span>
            </template>
        </div>
        
          <div class="chat-actions">
              <el-button 
              size="small" 
              @click="clearHistory" 
              :disabled="currentMessages.length === 0"
            >
              <el-icon><Delete /></el-icon>
              清空当前对话
              </el-button>
            
            <el-button 
              size="small" 
              @click="exportHistory" 
              :disabled="currentMessages.length === 0"
            >
              <el-icon><Download /></el-icon>
              导出对话
            </el-button>
            </div>
          </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div v-if="currentMessages.length === 0" class="welcome-message">
            <div class="welcome-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
                         <h3>开始 AutoAgent 任务</h3>
             <p>选择智能体后，您可以发布任务，让AI自动化执行复杂的多步骤任务</p>
            
            <div class="quick-questions" v-if="selectedAgentId">
                             <h4>推荐任务</h4>
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
            v-for="(message, index) in currentMessages" 
            :key="message.id"
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
              
              <div class="message-text" v-html="renderMarkdown(message.content)"></div>
              
              <!-- 流式响应指示器 -->
              <div v-if="message.isStreaming" class="streaming-indicator">
                <span class="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </span>
              </div>
              
              <!-- 消息操作 -->
              <div class="message-actions" v-if="message.role === 'assistant' && !message.isStreaming">
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
                  v-model="messageInput"
                  type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
                             :placeholder="'请输入您的任务描述...'"
              :disabled="!selectedAgentId || isLoading"
              @keydown.enter.prevent="handleKeyDown"
              class="message-input"
            />
            
            <div class="input-actions">
                <el-button
                  type="primary"
                :disabled="!messageInput.trim() || !selectedAgentId || isLoading"
                  @click="sendMessage"
                  :loading="isLoading"
                >
                <el-icon v-if="!isLoading"><Promotion /></el-icon>
                                 {{ isLoading ? '执行中...' : '执行' }}
                </el-button>
              </div>
            </div>
            
          <div class="input-tips">
            <span v-if="!selectedAgentId" class="tip-warning">
              <el-icon><Warning /></el-icon>
              请先选择智能体
            </span>
                         <span v-else class="tip-info">
               按 Ctrl+Enter 执行任务
             </span>
          </div>
        </div>
      </div>
    </div>

    

    <!-- 文件上传模态框 -->
    <el-dialog
      v-model="showUploadModal"
      title="上传文件到知识库"
      width="600px"
      class="upload-modal"
    >
      <div class="upload-content">
        <el-form :model="uploadForm" label-width="100px">
          <el-form-item label="知识库名称">
            <el-input v-model="uploadForm.name" placeholder="请输入知识库名称" />
          </el-form-item>
          <el-form-item label="知识库标签">
            <el-input v-model="uploadForm.tag" placeholder="请输入知识库标签" />
          </el-form-item>
          <el-form-item label="选择文件">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              multiple
              drag
            >
              <el-icon class="el-icon--upload"><Upload /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showUploadModal = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :loading="isUploading">
            开始上传
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Git仓库解析模态框 -->
    <el-dialog
      v-model="showGitModal"
      title="解析Git仓库"
      width="600px"
      class="git-modal"
    >
      <div class="git-content">
        <el-form :model="gitForm" label-width="100px">
          <el-form-item label="仓库地址">
            <el-input 
              v-model="gitForm.repoUrl" 
              placeholder="请输入Git仓库地址，如：https://github.com/username/repo.git"
            />
          </el-form-item>
          <el-form-item label="分支名称">
            <el-input v-model="gitForm.branch" placeholder="main" />
          </el-form-item>
          <el-form-item label="知识库标签">
            <el-input v-model="gitForm.tag" placeholder="请输入知识库标签" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showGitModal = false">取消</el-button>
          <el-button type="primary" @click="submitGitParse" :loading="isGitParsing">
            开始解析
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { 
  Refresh,
  User,
  Avatar,
  UserFilled,
  Folder,
  FolderOpened,
  DocumentRemove,
  ChatDotRound,
  Delete,
  Download,
  Plus,
  Document,
  Link,
  ArrowDown,
  Warning,
  CopyDocument,
  Promotion,
  Tools,
  Upload
} from '@element-plus/icons-vue'
import { agentApi } from '@/api/agent'
import { knowledgeBaseApi } from '@/api/knowledgeBase'
import { marked } from 'marked'

// 路由
const route = useRoute()

// 响应式数据
const agentList = ref([])
const ragList = ref([])
const promptTemplates = ref([])
const selectedAgentId = ref('')
const selectedRagId = ref('')
const selectedPromptTemplate = ref('')
const currentChatId = ref(null)
const chatList = ref([])
const currentMessages = ref([])
const messageInput = ref('')
const isLoading = ref(false)
const isRefreshingConfigs = ref(false)
const showRecordModal = ref(false)
const qaRecords = ref([])
const currentChatIndex = ref(-1)

 // AutoAgent配置
 const autoAgentConfig = ref({
   maxStep: 10,
   sessionId: null
 })

// 文件上传相关
const showUploadModal = ref(false)
const isUploading = ref(false)
const uploadForm = ref({
  name: '',
  tag: '',
  files: []
})

// Git仓库解析相关
const showGitModal = ref(false)
const isGitParsing = ref(false)
const gitForm = ref({
  repoUrl: '',
  branch: 'main',
  tag: ''
})

// DOM引用
const messageListRef = ref(null)
const uploadRef = ref(null)

// 计算属性
const isMobile = computed(() => {
  return window.innerWidth < 768
})

 const suggestedQuestions = computed(() => {
   if (!selectedAgentId.value) return []
   
   return [
     '分析这个项目的代码结构并生成文档',
     '帮我制定一个完整的项目开发计划',
     '自动化测试当前代码库的质量',
     '生成API文档并创建示例代码'
   ]
 })

// 生命周期
onMounted(() => {
  loadConfigs()
  
  // 检查URL参数
  const preSelectedAgent = route.query.agent
  if (preSelectedAgent) {
    selectedAgentId.value = preSelectedAgent
  }
  
  // 禁用父容器滚动
  const pageContent = document.querySelector('.page-content')
  if (pageContent) {
    pageContent.style.overflow = 'hidden'
  }
})

// 监听器
watch([selectedAgentId, selectedRagId], () => {
  if (selectedAgentId.value) {
    loadChatHistory()
  }
})

// 自动保存聊天记录
watch(currentMessages, (newMessages) => {
  if (newMessages.length > 0 && currentChatId.value) {
    // 延迟保存，避免频繁保存
    clearTimeout(autoSaveTimer.value)
    autoSaveTimer.value = setTimeout(() => {
      saveChatRecord()
    }, 2000)
  }
}, { deep: true })

// 自动保存定时器
const autoSaveTimer = ref(null)

// 组件销毁时清理定时器
onBeforeUnmount(() => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
  
  const pageContent = document.querySelector('.page-content')
  if (pageContent) {
    pageContent.style.overflow = 'auto'
  }
})

// 方法
const loadConfigs = async () => {
  isRefreshingConfigs.value = true
  try {
    await Promise.all([
      loadAgents(),
      loadKnowledgeBases(),
      loadPromptTemplates()
    ])
  } finally {
    isRefreshingConfigs.value = false
  }
}

const refreshConfigs = () => {
  loadConfigs()
}

const loadAgents = async () => {
  try {
    const response = await agentApi.getAgentList()
    
    // 处理直接返回数组的情况
    if (Array.isArray(response)) {
      agentList.value = response
    } else if (response && response.code === '0000') {
      agentList.value = response.data || []
    } else {
      agentList.value = []
    }
  } catch (error) {
    console.error('加载智能体失败:', error)
    ElMessage.error('加载智能体失败')
  }
}

const loadKnowledgeBases = async () => {
  try {
    const response = await knowledgeBaseApi.getKnowledgeBaseList()
    // 直接使用ResponseEntity返回的数据数组
    if (Array.isArray(response)) {
      ragList.value = response.filter(ragOrder => ragOrder.status === 1).map(ragOrder => ({
        id: ragOrder.id,
        name: ragOrder.ragName,
        tag: ragOrder.knowledgeTag,
        description: `知识库: ${ragOrder.ragName}`
      }))
    } else if (response && response.code === '0000') {
      ragList.value = (response.data || []).filter(ragOrder => ragOrder.status === 1).map(ragOrder => ({
        id: ragOrder.id,
        name: ragOrder.ragName,
        tag: ragOrder.knowledgeTag,
        description: `知识库: ${ragOrder.ragName}`
      }))
    } else {
      ragList.value = []
    }
  } catch (error) {
    console.error('加载知识库失败:', error)
    ElMessage.error('加载知识库失败')
  }
}

const loadPromptTemplates = async () => {
  try {
    const response = await agentApi.getPromptTemplates()
    if (response.code === '0000') {
      promptTemplates.value = response.data || []
    } else {
      promptTemplates.value = []
    }
  } catch (error) {
    console.error('加载提示词模板失败:', error)
    ElMessage.error('加载提示词模板失败')
  }
}

const selectAgent = (agentId) => {
  if (selectedAgentId.value === agentId) return
  
  // 如果有对话历史，询问是否切换
  if (currentMessages.value.length > 0) {
    ElMessageBox.confirm(
      '切换智能体将清空当前对话历史，是否继续？',
      '确认切换',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      selectedAgentId.value = agentId
      currentMessages.value = []
      currentChatId.value = null
    }).catch(() => {
      // 用户取消
    })
  } else {
    selectedAgentId.value = agentId
  }
}

const selectKnowledgeBase = (ragId) => {
  selectedRagId.value = ragId
}

const selectPromptTemplate = (templateId) => {
  selectedPromptTemplate.value = templateId
}

// 处理顶部配置栏的事件
const handleAgentChange = (agentId) => {
  selectAgent(agentId)
}

const handleKnowledgeBaseChange = (ragId) => {
  selectKnowledgeBase(ragId)
}

const handlePromptTemplateChange = (templateId) => {
  selectPromptTemplate(templateId)
}

const startNewChat = () => {
  createNewChat()
}



const clearChat = () => {
  clearHistory()
}

const handleBuildCommand = (command) => {
  if (command === 'buildRepo') {
    handleRepoCommand('local')
  } else if (command === 'buildAgent') {
    // 这里可以添加构建智能体的逻辑
    ElMessage.info('构建智能体功能待实现')
  }
}

const handleUploadCommand = (command) => {
  if (command === 'uploadFiles') {
    handleUploadFiles()
  } else if (command === 'parseGit') {
    handleParseGit()
  }
}

const getCurrentAgentName = () => {
  const agent = agentList.value.find(a => a.id === selectedAgentId.value)
  return agent ? agent.agentName : '未知智能体'
}

const getCurrentRagName = () => {
  const rag = ragList.value.find(r => r.id === selectedRagId.value)
  return rag ? rag.name : '未知知识库'
}

// 历史对话相关方法
const chatHistory = computed(() => {
  return chatList.value.map((chat, index) => ({
    id: chat.id,
    title: chat.title || `对话 ${index + 1}`,
    timestamp: chat.createdTime || new Date(),
    preview: chat.messages?.[0]?.content?.substring(0, 50) + '...' || '空对话'
  }))
})

const loadChatHistory = async () => {
  if (!selectedAgentId.value) return
  
  try {
    const response = await agentApi.getChatHistory(selectedAgentId.value, selectedRagId.value)
    if (response.code === '0000') {
      chatList.value = response.data || []
    } else {
      chatList.value = []
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    ElMessage.error('加载聊天历史失败')
  }
}

const loadHistoryChat = (index) => {
  currentChatIndex.value = index
  if (chatList.value[index]) {
    currentMessages.value = chatList.value[index].messages || []
    currentChatId.value = chatList.value[index].id
    scrollToBottom()
  }
}

const saveChatRecord = async () => {
  if (!currentChatId.value || currentMessages.value.length === 0) return
  
  try {
    const chatData = {
      id: currentChatId.value,
      agentId: selectedAgentId.value,
      ragId: selectedRagId.value,
      title: `与 ${getCurrentAgentName()} 的对话`,
      messages: currentMessages.value,
      createdTime: new Date()
    }
    
    await agentApi.saveChatRecord(chatData)
    
    // 更新本地聊天列表
    const existingIndex = chatList.value.findIndex(chat => chat.id === currentChatId.value)
    if (existingIndex >= 0) {
      chatList.value[existingIndex] = chatData
    } else {
      chatList.value.unshift(chatData)
    }
  } catch (error) {
    console.error('保存聊天记录失败:', error)
  }
}

const deleteChatRecord = async (chatId) => {
  try {
    await agentApi.deleteChatRecord(chatId)
    
    // 从本地列表中移除
    chatList.value = chatList.value.filter(chat => chat.id !== chatId)
    
    // 如果删除的是当前对话，清空当前对话
    if (currentChatId.value === chatId) {
      currentMessages.value = []
      currentChatId.value = null
      currentChatIndex.value = -1
    }
    
    ElMessage.success('聊天记录已删除')
  } catch (error) {
    console.error('删除聊天记录失败:', error)
    ElMessage.error('删除失败')
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const createNewChat = () => {
  if (!selectedAgentId.value) {
    ElMessage.warning('请先选择智能体')
    return
  }

  // 保存当前对话
  if (currentChatId.value && currentMessages.value.length > 0) {
    saveChatRecord()
  }

  currentChatId.value = Date.now().toString()
  currentMessages.value = []
  currentChatIndex.value = -1
  
  // 添加到聊天列表
  const newChat = {
    id: currentChatId.value,
    title: `与 ${getCurrentAgentName()} 的对话`,
    createdAt: new Date(),
    agentId: selectedAgentId.value,
    ragId: selectedRagId.value,
    messages: []
  }
  
  chatList.value.unshift(newChat)
  
  ElMessage.success('新对话已创建')
}



const sendMessage = async () => {
  if (!messageInput.value.trim() || !selectedAgentId.value || isLoading.value) {
    return
  }

  // 如果没有当前对话，自动创建
  if (!currentChatId.value) {
    createNewChat()
  }

  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: messageInput.value.trim(),
    timestamp: new Date()
  }

  currentMessages.value.push(userMessage)
  const userInput = messageInput.value.trim()
  messageInput.value = ''
  isLoading.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    // 创建AI回复占位符
    const assistantMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      isStreaming: true
    }
    currentMessages.value.push(assistantMessage)

    // 滚动到底部
    await nextTick()
    scrollToBottom()

         // 使用AutoAgent模式
     await sendAutoAgentMessage(userInput, assistantMessage)

    // 保存问答记录
    qaRecords.value.push({
      id: Date.now() + 2,
      question: userInput,
      answer: assistantMessage.content,
      timestamp: new Date(),
      agent: getCurrentAgentName(),
      rag: selectedRagId.value ? getCurrentRagName() : null
    })

  } catch (error) {
    console.error('对话失败:', error)
    ElMessage.error('对话失败，请重试')
    
    // 更新错误消息
    const lastMessage = currentMessages.value[currentMessages.value.length - 1]
    if (lastMessage && lastMessage.role === 'assistant') {
      lastMessage.content = '抱歉，我现在无法回答您的问题，请稍后重试。'
      lastMessage.isStreaming = false
    }
  } finally {
    isLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}



// AutoAgent对话
const sendAutoAgentMessage = async (message, assistantMessage) => {
  // 生成会话ID（如果没有的话）
  if (!autoAgentConfig.value.sessionId) {
    autoAgentConfig.value.sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
  
  const params = {
    aiAgentId: selectedAgentId.value,
    message: message,
    sessionId: autoAgentConfig.value.sessionId,
    maxStep: autoAgentConfig.value.maxStep
  }
  
  try {
    await agentApi.autoAgentStream(
      params,
      // onMessage callback
      (data) => {
        const content = data.content || data.message || ''
        if (content) {
          assistantMessage.content += content
          assistantMessage.isStreaming = true
          scrollToBottom()
        }
      },
      // onError callback
      (error) => {
        console.error('AutoAgent执行失败:', error)
        assistantMessage.content = '抱歉，AutoAgent执行失败，请稍后重试。'
        assistantMessage.isStreaming = false
      },
      // onComplete callback
      () => {
        assistantMessage.isStreaming = false
        if (!assistantMessage.content.trim()) {
          assistantMessage.content = 'AutoAgent执行完成，但没有返回内容。'
        }
      }
    )
  } catch (error) {
    console.error('AutoAgent调用失败:', error)
    assistantMessage.content = '抱歉，AutoAgent调用失败，请稍后重试。'
    assistantMessage.isStreaming = false
  }
}

const askQuestion = (question) => {
  messageInput.value = question
  sendMessage()
}

const handleKeyDown = (event) => {
  if (event.ctrlKey && event.key === 'Enter') {
    sendMessage()
  }
}

const clearHistory = () => {
  ElMessageBox.confirm(
    '确定要清空当前对话记录吗？',
    '确认清空',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    currentMessages.value = []
    ElMessage.success('对话记录已清空')
  }).catch(() => {
    // 用户取消
  })
}

const clearAllChats = () => {
  ElMessageBox.confirm(
    '确定要清空所有对话记录吗？',
    '确认清空',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    currentMessages.value = []
    chatList.value = []
    currentChatId.value = null
    ElMessage.success('所有对话记录已清空')
  }).catch(() => {
    // 用户取消
  })
}

const exportHistory = () => {
  if (currentMessages.value.length === 0) return

  const content = currentMessages.value.map(msg => {
    const role = msg.role === 'user' ? '用户' : 'AI助手'
    const time = formatTime(msg.timestamp)
    return `[${time}] ${role}: ${msg.content}`
  }).join('\n\n')

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `Agent对话记录_${new Date().toISOString().slice(0, 10)}.txt`
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

  const userMessage = currentMessages.value[messageIndex - 1]
  if (userMessage.role !== 'user') return

  // 移除当前回复
  currentMessages.value.splice(messageIndex, 1)
  
  isLoading.value = true
  
  try {
    const params = {
      agentId: selectedAgentId.value,
      message: userMessage.content,
      ragId: selectedRagId.value || undefined,
      promptTemplateId: selectedPromptTemplate.value || undefined
    }

    const response = await agentApi.chatWithAgent(params)

    const assistantMessage = {
      id: Date.now(),
      role: 'assistant',
      content: response.data,
      timestamp: new Date()
    }

    currentMessages.value.splice(messageIndex, 0, assistantMessage)
  } catch (error) {
    console.error('重新生成失败:', error)
    ElMessage.error('重新生成失败，请重试')
  } finally {
    isLoading.value = false
  }
}

const handleRepoCommand = (command) => {
  if (command === 'local') {
    buildLocalRepo()
  } else if (command === 'git') {
    parseGitRepo()
  }
}

const handleUploadFiles = () => {
  showUploadModal.value = true
  uploadForm.value = {
    name: '',
    tag: '',
    files: []
  }
}

const handleParseGit = () => {
  showGitModal.value = true
  gitForm.value = {
    repoUrl: '',
    branch: 'main',
    tag: ''
  }
}

// 文件上传相关方法
const handleFileChange = (file, fileList) => {
  uploadForm.value.files = fileList.map(f => f.raw)
}

const handleFileRemove = (file, fileList) => {
  uploadForm.value.files = fileList.map(f => f.raw)
}

const submitUpload = async () => {
  if (!uploadForm.value.name || !uploadForm.value.tag || uploadForm.value.files.length === 0) {
    ElMessage.warning('请填写完整信息并选择文件')
    return
  }

  isUploading.value = true
  try {
    const response = await agentApi.uploadRagFile(
      uploadForm.value.name,
      uploadForm.value.tag,
      uploadForm.value.files
    )

    if (response.code === '0000') {
      ElMessage.success('文件上传成功')
      showUploadModal.value = false
      // 刷新知识库列表
      loadKnowledgeBases()
    } else {
      ElMessage.error(response.data || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    isUploading.value = false
  }
}

// Git仓库解析相关方法
const submitGitParse = async () => {
  if (!gitForm.value.repoUrl || !gitForm.value.tag) {
    ElMessage.warning('请填写仓库地址和知识库标签')
    return
  }

  isGitParsing.value = true
  try {
    const response = await agentApi.parseGitRepo(
      gitForm.value.repoUrl,
      gitForm.value.branch
    )

    if (response.code === '0000') {
      ElMessage.success('Git仓库解析成功')
      showGitModal.value = false
      // 刷新知识库列表
      loadKnowledgeBases()
    } else {
      ElMessage.error(response.data || '解析失败')
    }
  } catch (error) {
    console.error('Git仓库解析失败:', error)
    ElMessage.error('解析失败，请重试')
  } finally {
    isGitParsing.value = false
  }
}

const buildLocalRepo = () => {
  ElMessage.info('构建本地仓库功能开发中...')
}

const parseGitRepo = () => {
  ElMessage.info('解析Git仓库功能开发中...')
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

const renderMarkdown = (content) => {
  try {
    // 配置marked选项
    marked.setOptions({
      highlight: function(code, lang) {
        if (lang && window.hljs) {
          try {
            return window.hljs.highlight(code, { language: lang }).value
          } catch (err) {
            console.warn('代码高亮失败:', err)
          }
        }
        return code
      },
      breaks: true,
      gfm: true
    })
    
    const html = marked(content)
    
    // 应用代码高亮
    if (window.hljs) {
      setTimeout(() => {
        const codeBlocks = document.querySelectorAll('pre code')
        codeBlocks.forEach(block => {
          window.hljs.highlightElement(block)
        })
      }, 100)
    }
    
    return html
  } catch (error) {
    console.error('Markdown渲染失败:', error)
    return content.replace(/\n/g, '<br>')
  }
}
</script>

<style scoped>
.agent-chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
  position: relative;
}

/* 页面标题栏 */
.page-header {
  background: #fff;
  padding: 10px 24px 16px;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.0;
}

.page-subtitle {
  font-size: 14px;
  font-weight: 400;
  color: #666;
  margin-left: 8px;
}

/* 顶部配置栏 */
.top-config-bar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  margin: 0px 20px 0 0px;
  padding: 6px 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  min-height: 70px;
}

.config-left .page-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.config-center {
  display: flex;
  align-items: center;
  gap: 24px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-item label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
  white-space: nowrap;
}

.config-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-container {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
  margin: 20px;
  margin-top: 20px;
  min-height: 0;
}

/* 历史对话面板 */
.history-panel {
  width: 280px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0px 20px rgba(0, 0, 0, 0.1);
  min-height: 0;
  max-height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.history-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-item:hover {
  background: #f0f0f0;
  border-color: #d0d0d0;
}

.history-item.active {
  background: #e0e0e0;
  color: #333;
  border-color: #999;
}

.history-content {
  flex: 1;
}

.history-title {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
}

.history-time {
  font-size: 12px;
  opacity: 0.7;
  margin-bottom: 4px;
}

.history-preview {
  font-size: 12px;
  opacity: 0.8;
  line-height: 1.4;
}

.history-actions {
  flex-shrink: 0;
}

.delete-btn {
  color: var(--danger-color);
  padding: 0 8px;
}

.delete-btn:hover {
  background: var(--danger-light);
  color: var(--danger-dark);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  font-weight: 500;
  color: var(--text-primary);
  background: var(--fill-lighter);
  border-radius: 6px;
  margin-bottom: 8px;
}

.agent-list,
.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.config-item:hover {
  background: var(--fill-lighter);
}

.config-item.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.config-item.active .item-desc {
  color: rgba(255, 255, 255, 0.8);
}

.config-item.optional {
  border-style: dashed;
}

.item-icon {
  font-size: 20px;
  color: var(--primary-color);
}

.config-item.active .item-icon {
  color: white;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

.panel-footer {
  padding: 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.w-full {
  width: 100%;
}

/* 对话区域 */
.chat-area {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  min-height: 0;
  max-height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-light);
}

.current-config {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: var(--text-primary);
}

.separator {
  color: var(--text-secondary);
  margin: 0 4px;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  min-height: 0;
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

/* 流式响应指示器 */
.streaming-indicator {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary-color);
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 代码高亮样式 */
.message-text :deep(pre) {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-text :deep(code) {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 3px;
  padding: 2px 4px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
}

.message-text :deep(pre code) {
  background: transparent;
  border: none;
  padding: 0;
  border-radius: 0;
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



/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 30px 20px;
  color: #999;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .top-config-bar {
    flex-direction: column;
    gap: 16px;
    min-height: auto;
    padding: 16px;
  }
  
  .config-center {
    width: 100%;
    justify-content: center;
  }
  
  .config-right {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .chat-container {
    flex-direction: column;
    gap: 12px;
  }
  
  .history-panel {
    width: 100%;
    order: 1;
    max-height: 200px;
  }
  
  .chat-area {
    order: 2;
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


