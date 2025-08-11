<template>
  <div class="manus-agent page-container">
      <div class="page-header">
        <h1 class="page-title">ManusAgent</h1>
        <p class="page-description">通用多功能智能体 - 支持复杂任务规划和执行</p>
      </div>

      <!-- 快捷操作区 -->
      <div class="quick-section card">
        <div class="section-header">
          <h3>快速执行</h3>
          <div class="strategy-selector">
            <el-select v-model="currentStrategy" placeholder="选择策略" style="width: 150px;">
              <el-option label="综合分析" value="COMPREHENSIVE" />
              <el-option label="步骤推理" value="STEP_BY_STEP" />
              <el-option label="快速模式" value="QUICK" />
            </el-select>
          </div>
        </div>
        
        <div class="input-section">
          <el-input
            v-model="quickTask"
            type="textarea"
            :rows="3"
            placeholder="请描述您要执行的任务，例如：分析最近的会议记录并生成总结报告"
            maxlength="500"
            show-word-limit
          />
          
          <div class="action-buttons">
            <el-button
              type="primary"
              :loading="isExecuting"
              @click="executeQuickTask"
              :disabled="!quickTask.trim()"
            >
              立即执行
            </el-button>
            
            <el-button
              :loading="isExecuting"
              @click="executeTaskAsync"
              :disabled="!quickTask.trim()"
            >
              异步执行
            </el-button>
          </div>
        </div>
      </div>

      <!-- 高级配置 -->
      <div class="advanced-section card">
        <el-collapse v-model="expandedPanels">
          <el-collapse-item title="高级配置" name="advanced">
            <el-form :model="advancedForm" label-width="120px" class="advanced-form">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="任务类型">
                    <el-select v-model="advancedForm.taskType" placeholder="选择任务类型">
                      <el-option label="会议分析" value="meeting_analysis" />
                      <el-option label="知识问答" value="knowledge_qa" />
                      <el-option label="报告生成" value="report_generation" />
                      <el-option label="数据处理" value="data_processing" />
                      <el-option label="其他" value="other" />
                    </el-select>
                  </el-form-item>
                </el-col>
                
                <el-col :span="12">
                  <el-form-item label="优先级">
                    <el-select v-model="advancedForm.priority" placeholder="选择优先级">
                      <el-option label="低" :value="1" />
                      <el-option label="中" :value="2" />
                      <el-option label="高" :value="3" />
                    </el-select>
                  </el-form-item>
                </el-col>
                
                <el-col :span="12">
                  <el-form-item label="超时时间">
                    <el-input-number
                      v-model="advancedForm.timeoutMinutes"
                      :min="1"
                      :max="120"
                      placeholder="分钟"
                    />
                  </el-form-item>
                </el-col>
                
                <el-col :span="12">
                  <el-form-item label="用户ID">
                    <el-input v-model="advancedForm.userId" placeholder="输入用户ID" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="任务描述">
                <el-input
                  v-model="advancedForm.taskDescription"
                  type="textarea"
                  :rows="4"
                  placeholder="详细描述任务需求..."
                />
              </el-form-item>
              
              <el-form-item label="额外参数">
                <div class="parameters-editor">
                  <div
                    v-for="(param, index) in advancedForm.parameters"
                    :key="index"
                    class="parameter-item"
                  >
                    <el-input
                      v-model="param.key"
                      placeholder="参数名"
                      style="width: 150px;"
                    />
                    <el-input
                      v-model="param.value"
                      placeholder="参数值"
                      style="width: 200px; margin-left: 8px;"
                    />
                    <el-button
                      :icon="Delete"
                      @click="removeParameter(index)"
                      text
                      type="danger"
                    />
                  </div>
                  
                  <el-button
                    :icon="Plus"
                    @click="addParameter"
                    text
                    type="primary"
                  >
                    添加参数
                  </el-button>
                </div>
              </el-form-item>
              
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="isExecuting"
                  @click="executeAdvancedTask"
                  :disabled="!advancedForm.taskDescription.trim()"
                >
                  执行高级任务
                </el-button>
                
                <el-button @click="resetAdvancedForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 执行结果 -->
      <div v-if="currentResult" class="result-section card">
        <div class="section-header">
          <h3>执行结果</h3>
          <div class="result-actions">
            <el-button
              v-if="currentResult.taskId"
              :icon="Refresh"
              @click="refreshTaskStatus"
              :loading="isRefreshing"
              text
            >
              刷新状态
            </el-button>
            
            <el-button :icon="DocumentCopy" @click="copyResult" text>复制结果</el-button>
          </div>
        </div>
        
        <div class="result-content">
          <div class="result-header">
            <div class="task-info">
              <span class="task-id">任务ID: {{ currentResult.taskId }}</span>
              <el-tag
                :type="getStatusType(currentResult.status)"
                effect="light"
              >
                {{ getStatusText(currentResult.status) }}
              </el-tag>
            </div>
            
            <div v-if="currentResult.progress !== undefined" class="progress-info">
              <el-progress
                :percentage="currentResult.progress"
                :status="currentResult.status === 'FAILED' ? 'exception' : undefined"
              />
            </div>
          </div>
          
          <div class="result-body">
            <!-- 调试信息 -->
            <div v-if="currentResult.status === 'COMPLETED' && !currentResult.result" class="debug-info">
              <el-alert
                title="调试信息"
                type="warning"
                show-icon
                :closable="false"
              >
                <template #default>
                  <p>任务已完成但未获取到结果数据</p>
                  <p>任务ID: {{ currentResult.taskId }}</p>
                  <p>状态: {{ currentResult.status }}</p>
                  <el-button size="small" @click="debugTaskResult">获取结果详情</el-button>
                </template>
              </el-alert>
            </div>
            
            <div v-if="currentResult.result" class="result-text">
              <pre v-if="typeof currentResult.result === 'string'">{{ currentResult.result }}</pre>
              <el-descriptions v-else :column="2" border>
                <el-descriptions-item
                  v-for="(value, key) in currentResult.result"
                  :key="key"
                  :label="key"
                >
                  <div v-if="typeof value === 'object'" class="json-content">
                    <pre>{{ JSON.stringify(value, null, 2) }}</pre>
                  </div>
                  <span v-else>{{ value }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
            
            <div v-if="currentResult.error" class="error-message">
              <el-alert
                title="执行错误"
                :description="currentResult.error"
                type="error"
                show-icon
                :closable="false"
              />
            </div>
          </div>
          
          <div class="result-footer">
            <div class="time-info">
              <span v-if="currentResult.startTime">
                开始时间: {{ formatDateTime(currentResult.startTime) }}
              </span>
              <span v-if="currentResult.endTime">
                结束时间: {{ formatDateTime(currentResult.endTime) }}
              </span>
              <span v-if="currentResult.startTime && currentResult.endTime">
                耗时: {{ calculateDuration(currentResult.startTime, currentResult.endTime) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 任务历史 -->
      <div class="history-section card">
        <div class="section-header">
          <h3>任务历史</h3>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="refreshTaskHistory" :loading="isRefreshing" text>
              刷新
            </el-button>
            <el-button :icon="Delete" @click="clearHistory" text type="danger">
              清空历史
            </el-button>
          </div>
        </div>
        
        <div class="history-list">
          <div v-if="taskStore.taskList.length === 0" class="empty-state">
            <el-icon size="48" color="#c0c4cc"><DocumentCopy /></el-icon>
            <p>暂无任务历史</p>
          </div>
          
          <div
            v-for="task in taskStore.taskList"
            :key="task.taskId"
            class="history-item"
            @click="viewTask(task)"
          >
            <div class="task-info">
              <div class="task-title">{{ task.result?.title || task.taskId }}</div>
              <div class="task-description">{{ task.result?.summary || '无描述' }}</div>
              <div class="task-time">{{ formatDateTime(task.startTime) }}</div>
            </div>
            
            <div class="task-status">
              <el-tag
                :type="getStatusType(task.status)"
                effect="light"
                size="small"
              >
                {{ getStatusText(task.status) }}
              </el-tag>
              <div v-if="task.progress !== undefined" class="task-progress">
                {{ Math.round(task.progress) }}%
              </div>
            </div>
            
            <div class="task-actions">
              <el-button
                :icon="View"
                @click.stop="openTaskDetail(task)"
                text
                type="primary"
                size="small"
              >
                详情
              </el-button>
              
              <el-button
                v-if="task.status === 'RUNNING'"
                :icon="VideoPause"
                @click.stop="cancelTask(task.taskId)"
                text
                type="danger"
                size="small"
              >
                取消
              </el-button>
              
              <el-button
                v-if="task.status === 'FAILED'"
                :icon="RefreshRight"
                @click.stop="retryTask(task)"
                text
                type="warning"
                size="small"
              >
                重试
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 任务详情弹窗 -->
    <el-dialog
      v-model="taskDetailVisible"
      :title="`任务详情 - ${selectedTask?.taskId || ''}`"
      width="80%"
      :before-close="closeTaskDetail"
    >
      <div v-if="selectedTask" class="task-detail">
        <!-- 基本信息 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-tag :type="getStatusType(selectedTask.status)" effect="light">
                {{ getStatusText(selectedTask.status) }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务ID">
              <code>{{ selectedTask.taskId }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(selectedTask.status)" effect="light" size="small">
                {{ getStatusText(selectedTask.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
              {{ formatDateTime(selectedTask.startTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="结束时间">
              {{ selectedTask.endTime ? formatDateTime(selectedTask.endTime) : 'N/A' }}
            </el-descriptions-item>
            <el-descriptions-item label="执行时长">
              {{ selectedTask.startTime && selectedTask.endTime ? 
                 calculateDuration(selectedTask.startTime, selectedTask.endTime) : 'N/A' }}
            </el-descriptions-item>
            <el-descriptions-item label="进度" v-if="selectedTask.progress !== undefined">
              <el-progress :percentage="Math.round(selectedTask.progress)" :stroke-width="6" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 执行结果 -->
        <el-card class="detail-card" shadow="never" v-if="selectedTask.result">
          <template #header>
            <div class="card-header">
              <span>执行结果</span>
              <el-button :icon="DocumentCopy" @click="copyTaskResult" text size="small">
                复制结果
              </el-button>
            </div>
          </template>
          
          <div class="result-content">
            <div v-if="typeof selectedTask.result === 'string'" class="text-result">
              <pre>{{ selectedTask.result }}</pre>
            </div>
            <div v-else class="object-result">
              <el-descriptions :column="1" border>
                <el-descriptions-item
                  v-for="(value, key) in selectedTask.result"
                  :key="key"
                  :label="key"
                >
                  <div v-if="typeof value === 'object'" class="json-content">
                    <pre>{{ JSON.stringify(value, null, 2) }}</pre>
                  </div>
                  <span v-else>{{ value }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>

        <!-- 错误信息 -->
        <el-card class="detail-card" shadow="never" v-if="selectedTask.error">
          <template #header>
            <span class="error-header">错误信息</span>
          </template>
          
          <el-alert
            :title="selectedTask.error"
            type="error"
            show-icon
            :closable="false"
          />
        </el-card>

        <!-- 任务参数 -->
        <el-card class="detail-card" shadow="never" v-if="selectedTask.parameters">
          <template #header>
            <span>任务参数</span>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item
              v-for="(value, key) in selectedTask.parameters"
              :key="key"
              :label="key"
            >
              {{ typeof value === 'object' ? JSON.stringify(value) : value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeTaskDetail">关闭</el-button>
          <el-button
            v-if="selectedTask?.status === 'RUNNING'"
            type="danger"
            @click="cancelTaskFromDetail"
          >
            取消任务
          </el-button>
          <el-button
            v-if="selectedTask?.status === 'FAILED'"
            type="warning"
            @click="retryTaskFromDetail"
          >
            重试任务
          </el-button>
          <el-button
            v-if="selectedTask?.status === 'RUNNING'"
            @click="refreshTaskDetailStatus"
            :loading="isRefreshingDetail"
          >
            刷新状态
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useTaskStore } from '@/stores/task'
import { manusApi } from '@/api/manus'

import {
  Delete, Plus, DocumentCopy, Refresh, VideoPause, View, RefreshRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const taskStore = useTaskStore()

// 响应式数据
const quickTask = ref('')
const currentStrategy = ref('COMPREHENSIVE')
const expandedPanels = ref<string[]>([])
const isExecuting = ref(false)
const isRefreshing = ref(false)
const currentResult = ref<any>(null)

// 任务详情弹窗相关
const taskDetailVisible = ref(false)
const selectedTask = ref<any>(null)
const isRefreshingDetail = ref(false)

// 定时器相关
let refreshTimer: NodeJS.Timeout | null = null

// 高级表单
const advancedForm = reactive({
  taskDescription: '',
  taskType: '',
  priority: 2,
  timeoutMinutes: 30,
  userId: 'admin',
  parameters: [] as Array<{ key: string; value: string }>
})

// 方法
const executeQuickTask = async () => {
  try {
    isExecuting.value = true
    const result = await taskStore.executeManusTask({
      task: quickTask.value,
      strategy: currentStrategy.value,
      async: false
    })
    
    currentResult.value = result
    ElMessage.success('任务执行完成')
  } catch (error: any) {
    ElMessage.error(error.message || '任务执行失败')
  } finally {
    isExecuting.value = false
  }
}

const executeTaskAsync = async () => {
  try {
    isExecuting.value = true
    const result = await taskStore.executeManusTask({
      task: quickTask.value,
      strategy: currentStrategy.value,
      async: true
    })
    
    currentResult.value = result
    ElMessage.success('异步任务已开始执行')
  } catch (error: any) {
    ElMessage.error(error.message || '任务执行失败')
  } finally {
    isExecuting.value = false
  }
}

const executeAdvancedTask = async () => {
  try {
    isExecuting.value = true
    
    const parameters: Record<string, any> = {}
    advancedForm.parameters.forEach(param => {
      if (param.key && param.value) {
        parameters[param.key] = param.value
      }
    })
    
    const result = await taskStore.executeAgentTask({
      taskDescription: advancedForm.taskDescription,
      taskType: advancedForm.taskType,
      priority: advancedForm.priority,
      timeoutMinutes: advancedForm.timeoutMinutes,
      userId: advancedForm.userId,
      parameters,
      async: true
    })
    
    currentResult.value = result
    ElMessage.success('高级任务已开始执行')
  } catch (error: any) {
    ElMessage.error(error.message || '任务执行失败')
  } finally {
    isExecuting.value = false
  }
}

const refreshTaskStatus = async () => {
  if (!currentResult.value?.taskId) return
  
  try {
    isRefreshing.value = true
    await taskStore.updateTaskStatus(currentResult.value.taskId)
    const updatedTask = taskStore.getTaskById(currentResult.value.taskId)
    if (updatedTask) {
      currentResult.value = updatedTask
      // 如果任务已完成，显示成功消息
      if (updatedTask.status === 'COMPLETED') {
        ElMessage.success('任务执行完成，结果已刷新')
      } else if (updatedTask.status === 'FAILED') {
        ElMessage.error('任务执行失败')
      }
    }
  } catch (error) {
    ElMessage.error('刷新状态失败')
  } finally {
    isRefreshing.value = false
  }
}

const copyResult = () => {
  const text = typeof currentResult.value.result === 'string' 
    ? currentResult.value.result 
    : JSON.stringify(currentResult.value.result, null, 2)
  
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('结果已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const debugTaskResult = async () => {
  if (!currentResult.value?.taskId) return
  
  try {
    console.log('调试任务结果:', currentResult.value)
    
    // 先尝试获取任务详情
    try {
      const detailResponse = await manusApi.getTaskDetail(currentResult.value.taskId)
      console.log('任务详情API返回:', detailResponse)
      
      if (detailResponse && detailResponse.data) {
        currentResult.value = { ...currentResult.value, ...detailResponse.data }
        ElMessage.success('任务详情已获取')
        return
      }
    } catch (detailError) {
      console.log('获取任务详情失败，尝试获取结果:', detailError)
    }
    
    // 如果获取详情失败，再尝试获取结果
    const resultResponse = await manusApi.getTaskResult(currentResult.value.taskId)
    console.log('任务结果API返回:', resultResponse)
    
    if (resultResponse && resultResponse.data) {
      currentResult.value = { 
        ...currentResult.value, 
        result: resultResponse.data 
      }
      ElMessage.success('任务结果已获取')
    } else {
      ElMessage.warning('未获取到任务结果')
    }
  } catch (error) {
    console.error('调试获取结果失败:', error)
    ElMessage.error('获取结果失败: ' + (error.message || '未知错误'))
  }
}

const addParameter = () => {
  advancedForm.parameters.push({ key: '', value: '' })
}

const removeParameter = (index: number) => {
  advancedForm.parameters.splice(index, 1)
}

const resetAdvancedForm = () => {
  Object.assign(advancedForm, {
    taskDescription: '',
    taskType: '',
    priority: 2,
    timeoutMinutes: 30,
    userId: 'admin',
    parameters: []
  })
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有任务历史吗？', '确认操作', {
      type: 'warning'
    })
    
    taskStore.clearTasks()
    currentResult.value = null
    ElMessage.success('任务历史已清空')
  } catch {
    // 用户取消
  }
}

const viewTask = (task: any) => {
  currentResult.value = task
}

const cancelTask = async (taskId: string) => {
  try {
    await taskStore.cancelTask(taskId)
    ElMessage.success('任务已取消')
  } catch (error) {
    ElMessage.error('取消任务失败')
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'COMPLETED': return 'success'
    case 'RUNNING': return 'warning'
    case 'FAILED': return 'danger'
    case 'CANCELLED': return 'info'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '等待中'
    case 'RUNNING': return '运行中'
    case 'COMPLETED': return '已完成'
    case 'FAILED': return '失败'
    case 'CANCELLED': return '已取消'
    default: return '未知'
  }
}

const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return 'N/A'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const calculateDuration = (start: string, end: string) => {
  const duration = dayjs(end).diff(dayjs(start), 'second')
  if (duration < 60) return `${duration}秒`
  if (duration < 3600) return `${Math.floor(duration / 60)}分${duration % 60}秒`
  return `${Math.floor(duration / 3600)}小时${Math.floor((duration % 3600) / 60)}分`
}

// 任务详情相关方法
const openTaskDetail = (task: any) => {
  selectedTask.value = task
  taskDetailVisible.value = true
}

const closeTaskDetail = () => {
  taskDetailVisible.value = false
  selectedTask.value = null
}

const copyTaskResult = () => {
  if (!selectedTask.value?.result) return
  
  const text = typeof selectedTask.value.result === 'string' 
    ? selectedTask.value.result 
    : JSON.stringify(selectedTask.value.result, null, 2)
  
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('结果已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const cancelTaskFromDetail = async () => {
  if (!selectedTask.value) return
  
  try {
    await taskStore.cancelTask(selectedTask.value.taskId)
    ElMessage.success('任务已取消')
    // 更新选中的任务状态
    selectedTask.value.status = 'CANCELLED'
  } catch (error) {
    ElMessage.error('取消任务失败')
  }
}

const retryTask = async (task: any) => {
  try {
    // 使用原任务的描述重新执行
    const taskDescription = task.result?.summary || task.taskId
    
    ElMessage.info('正在重新执行任务...')
    
    const response = await manusApi.executeTask({
      taskDescription,
      taskType: 'other',
      priority: 2,
      timeoutMinutes: 30,
      userId: 'admin',
      parameters: {}
    })
    
    if (response.success) {
      taskStore.addTask(response.data)
      ElMessage.success('任务已重新提交')
    } else {
      ElMessage.error(response.message || '重试任务失败')
    }
  } catch (error) {
    ElMessage.error('重试任务失败')
  }
}

const retryTaskFromDetail = () => {
  if (selectedTask.value) {
    retryTask(selectedTask.value)
    closeTaskDetail()
  }
}

const refreshTaskDetailStatus = async () => {
  if (!selectedTask.value) return
  
  isRefreshingDetail.value = true
  try {
    const response = await manusApi.getTaskStatus(selectedTask.value.taskId)
    if (response.success) {
      selectedTask.value = { ...selectedTask.value, ...response.data }
      // 同时更新store中的任务
      taskStore.updateTask(selectedTask.value.taskId, response.data)
      ElMessage.success('状态已刷新')
    } else {
      ElMessage.error('刷新状态失败')
    }
  } catch (error) {
    ElMessage.error('刷新状态失败')
  } finally {
    isRefreshingDetail.value = false
  }
}

// 手动刷新任务历史
const refreshTaskHistory = async () => {
  isRefreshing.value = true
  try {
    await taskStore.fetchTaskHistory(20)
    ElMessage.success('任务历史已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    isRefreshing.value = false
  }
}

// 自动刷新运行中的任务
const startAutoRefresh = () => {
  // 每30秒刷新一次任务状态
  refreshTimer = setInterval(async () => {
    const runningTaskList = taskStore.runningTasks
    if (runningTaskList.length > 0) {
      // 批量更新运行中任务的状态
      for (const task of runningTaskList) {
        try {
          const response = await manusApi.getTaskStatus(task.taskId)
          if (response.success) {
            taskStore.updateTask(task.taskId, response.data)
            // 如果是当前选中的任务，也更新详情弹窗
            if (selectedTask.value?.taskId === task.taskId) {
              selectedTask.value = { ...selectedTask.value, ...response.data }
            }
          }
        } catch (error) {
          console.warn(`更新任务 ${task.taskId} 状态失败:`, error)
        }
      }
    }
  }, 30000) // 30秒
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 生命周期钩子
onMounted(() => {
  // 加载任务历史
  taskStore.fetchTaskHistory(20)
  // 启动自动刷新
  startAutoRefresh()
})

onUnmounted(() => {
  // 清理定时器
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.manus-agent {
  padding: 24px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .quick-section {
    padding: 24px;
    margin-bottom: 24px;
    
    .input-section {
      .action-buttons {
        display: flex;
        gap: 12px;
        margin-top: 16px;
      }
    }
  }
  
  .advanced-section {
    margin-bottom: 24px;
    
    .advanced-form {
      padding: 0 24px 24px;
      
      .parameters-editor {
        .parameter-item {
          display: flex;
          align-items: center;
          margin-bottom: 8px;
        }
      }
    }
  }
  
  .result-section {
    padding: 24px;
    margin-bottom: 24px;
    
    .result-content {
      .result-header {
        margin-bottom: 16px;
        
        .task-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .task-id {
            font-family: monospace;
            font-size: 14px;
            color: var(--text-secondary);
          }
        }
      }
      
      .result-body {
        margin-bottom: 16px;
        
        .result-text pre {
          background: var(--fill-lighter);
          padding: 16px;
          border-radius: 6px;
          white-space: pre-wrap;
          word-break: break-word;
          max-height: 400px;
          overflow-y: auto;
        }
      }
      
      .result-footer {
        .time-info {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: var(--text-placeholder);
          
          span {
            padding: 4px 8px;
            background: var(--fill-lighter);
            border-radius: 4px;
          }
        }
      }
    }
  }
  
  .history-section {
    padding: 24px;
    
    .history-list {
      .history-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
        cursor: pointer;
        transition: background-color 0.3s;
        
        &:hover {
          background-color: var(--fill-lighter);
          margin: 0 -12px;
          padding-left: 12px;
          padding-right: 12px;
          border-radius: 6px;
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .task-info {
          flex: 1;
          
          .task-title {
            font-weight: 500;
            font-size: 14px;
            color: var(--text-primary);
            margin-bottom: 4px;
          }
          
          .task-description {
            font-size: 12px;
            color: var(--text-secondary);
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 300px;
          }
          
          .task-time {
            font-size: 12px;
            color: var(--text-placeholder);
          }
        }
        
        .task-status {
          margin-right: 12px;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 4px;
          
          .task-progress {
            font-size: 12px;
            color: var(--text-secondary);
          }
        }
        
        .task-actions {
          flex-shrink: 0;
          display: flex;
          gap: 8px;
        }
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: var(--text-secondary);
    
    p {
      margin-top: 12px;
      font-size: 14px;
    }
  }

  // 任务详情弹窗样式
  .task-detail {
    .detail-card {
      margin-bottom: 20px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
      }
      
      .error-header {
        color: var(--color-danger);
        font-weight: 600;
      }
    }
    
    .result-content {
      .text-result pre {
        background: var(--fill-lighter);
        padding: 16px;
        border-radius: 6px;
        white-space: pre-wrap;
        word-break: break-word;
        max-height: 400px;
        overflow-y: auto;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        font-size: 13px;
        line-height: 1.4;
      }
      
      .object-result {
        .json-content pre {
          background: var(--fill-lighter);
          padding: 12px;
          border-radius: 4px;
          font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
          font-size: 12px;
          line-height: 1.4;
          max-height: 200px;
          overflow-y: auto;
        }
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .manus-agent {
    padding: 16px;
    
    .section-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
    
    .action-buttons {
      flex-direction: column;
    }
    
    .history-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }
}
</style>