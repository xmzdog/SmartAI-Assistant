<template>
  <div class="agent-detail page-container">
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack" text>
          返回列表
        </el-button>
        <h1 class="page-title">{{ agentInfo?.agentName || '智能体详情' }}</h1>
      </div>
      <div class="header-actions">
        <el-button :icon="Setting" @click="handlePreheat" :loading="preheating">
          预热智能体
        </el-button>
        <el-button type="primary" :icon="Edit" @click="handleEdit">
          编辑
        </el-button>
      </div>
    </div>

    <div class="content-grid">
      <!-- 基本信息 -->
      <div class="info-card">
        <h3>基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>智能体名称：</label>
            <span>{{ agentInfo?.agentName || '-' }}</span>
          </div>
          <div class="info-item">
            <label>渠道类型：</label>
            <el-tag :type="agentInfo?.channel === 'agent' ? 'primary' : 'success'">
              {{ agentInfo?.channel === 'agent' ? '智能体' : '流式对话' }}
            </el-tag>
          </div>
          <div class="info-item">
            <label>状态：</label>
            <el-tag :type="agentInfo?.status === 1 ? 'success' : 'danger'">
              {{ agentInfo?.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
          <div class="info-item">
            <label>创建时间：</label>
            <span>{{ formatDate(agentInfo?.createTime) }}</span>
          </div>
          <div class="info-item">
            <label>更新时间：</label>
            <span>{{ formatDate(agentInfo?.updateTime) }}</span>
          </div>
        </div>
        <div class="info-item description">
          <label>描述：</label>
          <p>{{ agentInfo?.description || '暂无描述' }}</p>
        </div>
      </div>

      <!-- 关联客户端 -->
      <div class="clients-card">
        <div class="card-header">
          <h3>关联客户端</h3>
          <el-button type="primary" size="small" :icon="Plus" @click="handleAddClient">
            添加客户端
          </el-button>
        </div>
        <el-table :data="clientList" :loading="clientLoading" stripe>
          <el-table-column prop="id" label="客户端ID" width="100" />
          <el-table-column prop="clientName" label="客户端名称" min-width="150" />
          <el-table-column prop="sequence" label="执行顺序" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="handleViewClient(row)">
                查看
              </el-button>
              <el-button type="danger" size="small" text @click="handleRemoveClient(row)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 测试对话 -->
      <div class="chat-card">
        <h3>测试对话</h3>
        <div class="chat-container">
          <div class="messages" ref="messagesRef">
            <div
              v-for="(message, index) in messages"
              :key="index"
              :class="['message', message.type]"
            >
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.time) }}</div>
              </div>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              placeholder="请输入消息..."
              @keyup.enter="handleSendMessage"
              :disabled="chatLoading"
            >
              <template #append>
                <el-button 
                  type="primary" 
                  :icon="Position" 
                  @click="handleSendMessage"
                  :loading="chatLoading"
                >
                  发送
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑智能体"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="智能体名称" prop="agentName">
          <el-input v-model="editForm.agentName" placeholder="请输入智能体名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="渠道类型" prop="channel">
          <el-select v-model="editForm.channel" placeholder="请选择渠道类型">
            <el-option label="智能体" value="agent" />
            <el-option label="流式对话" value="chat_stream" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="handleEditSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Setting, Edit, Plus, Position } from '@element-plus/icons-vue'
import { agentManageApi, agentApi, clientApi } from '@/api/agent'

const route = useRoute()
const router = useRouter()

// 响应式数据
const agentInfo = ref(null)
const clientList = ref([])
const clientLoading = ref(false)
const preheating = ref(false)
const chatLoading = ref(false)
const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const messagesRef = ref()

// 聊天相关
const messages = ref([])
const inputMessage = ref('')

// 编辑表单
const editForm = reactive({
  id: null,
  agentName: '',
  description: '',
  channel: '',
  status: 1
})

const editFormRules = {
  agentName: [
    { required: true, message: '请输入智能体名称', trigger: 'blur' }
  ],
  channel: [
    { required: true, message: '请选择渠道类型', trigger: 'change' }
  ]
}

// 计算属性
const agentId = computed(() => route.params.id)

// 方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString('zh-CN')
}

const goBack = () => {
  router.push('/agent')
}

const fetchAgentInfo = async () => {
  if (!agentId.value) return
  
  try {
    const response = await agentManageApi.queryAgentById(agentId.value)
    agentInfo.value = response
  } catch (error) {
    console.error('获取智能体信息失败:', error)
    ElMessage.error('获取智能体信息失败')
  }
}

const fetchClientList = async () => {
  if (!agentId.value) return
  
  clientLoading.value = true
  try {
    const response = await clientApi.queryClientByAgentId(agentId.value)
    clientList.value = response || []
  } catch (error) {
    console.error('获取客户端列表失败:', error)
    ElMessage.error('获取客户端列表失败')
  } finally {
    clientLoading.value = false
  }
}

const handlePreheat = async () => {
  if (!agentId.value) return
  
  preheating.value = true
  try {
    await agentApi.preheat(agentId.value)
    ElMessage.success('智能体预热成功')
  } catch (error) {
    console.error('预热失败:', error)
    ElMessage.error('预热失败，请重试')
  } finally {
    preheating.value = false
  }
}

const handleEdit = () => {
  if (!agentInfo.value) return
  
  Object.assign(editForm, agentInfo.value)
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  editSubmitting.value = true
  try {
    await agentManageApi.updateAgent(editForm)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    fetchAgentInfo()
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    editSubmitting.value = false
  }
}

const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || !agentId.value) return
  
  const userMessage = {
    type: 'user',
    content: inputMessage.value,
    time: new Date()
  }
  messages.value.push(userMessage)
  
  const messageContent = inputMessage.value
  inputMessage.value = ''
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
  
  chatLoading.value = true
  try {
    const response = await agentApi.chatAgent(agentId.value, messageContent)
    const assistantMessage = {
      type: 'assistant',
      content: response.data || '抱歉，我无法理解您的问题。',
      time: new Date()
    }
    messages.value.push(assistantMessage)
    
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    const errorMessage = {
      type: 'assistant',
      content: '抱歉，发生了错误，请稍后重试。',
      time: new Date()
    }
    messages.value.push(errorMessage)
    
    nextTick(() => {
      scrollToBottom()
    })
  } finally {
    chatLoading.value = false
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const handleAddClient = () => {
  // TODO: 实现添加客户端功能
  ElMessage.info('功能开发中...')
}

const handleViewClient = (row) => {
  // TODO: 实现查看客户端功能
  ElMessage.info('功能开发中...')
}

const handleRemoveClient = (row) => {
  // TODO: 实现移除客户端功能
  ElMessage.info('功能开发中...')
}

// 生命周期
onMounted(() => {
  fetchAgentInfo()
  fetchClientList()
})
</script>

<style lang="scss" scoped>
.agent-detail {
  .content-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-top: 16px;

    .info-card,
    .clients-card,
    .chat-card {
      background: var(--bg-white);
      padding: 20px;
      border-radius: 8px;
      box-shadow: var(--shadow-light);

      h3 {
        margin: 0 0 16px 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }

    .info-card {
      .info-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;
        margin-bottom: 16px;
      }

      .info-item {
        display: flex;
        align-items: center;
        gap: 8px;

        &.description {
          grid-column: 1 / -1;
          flex-direction: column;
          align-items: flex-start;
          
          p {
            margin: 4px 0 0 0;
            color: var(--text-secondary);
            line-height: 1.5;
          }
        }

        label {
          font-weight: 500;
          color: var(--text-secondary);
          min-width: 80px;
        }

        span {
          color: var(--text-primary);
        }
      }
    }

    .clients-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        h3 {
          margin: 0;
        }
      }
    }

    .chat-card {
      grid-column: 1 / -1;

      .chat-container {
        height: 400px;
        display: flex;
        flex-direction: column;
        border: 1px solid var(--border-light);
        border-radius: 8px;
        overflow: hidden;
      }

      .messages {
        flex: 1;
        overflow-y: auto;
        padding: 16px;
        background: var(--bg-page);

        .message {
          margin-bottom: 16px;
          display: flex;

          &.user {
            justify-content: flex-end;

            .message-content {
              background: var(--primary-color);
              color: white;
              max-width: 70%;
            }
          }

          &.assistant {
            justify-content: flex-start;

            .message-content {
              background: var(--bg-white);
              color: var(--text-primary);
              border: 1px solid var(--border-light);
              max-width: 70%;
            }
          }

          .message-content {
            padding: 12px 16px;
            border-radius: 12px;

            .message-text {
              line-height: 1.5;
              margin-bottom: 4px;
            }

            .message-time {
              font-size: 12px;
              opacity: 0.7;
            }
          }
        }
      }

      .chat-input {
        padding: 16px;
        background: var(--bg-white);
        border-top: 1px solid var(--border-light);
      }
    }
  }
}

@media (max-width: 768px) {
  .agent-detail .content-grid {
    grid-template-columns: 1fr;
    
    .chat-card {
      grid-column: 1;
    }
  }
}
</style>
