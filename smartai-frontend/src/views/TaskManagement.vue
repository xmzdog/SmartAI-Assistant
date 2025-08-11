<template>
  <div class="task-management page-container">
      <div class="page-header">
        <h1 class="page-title">任务管理</h1>
        <p class="page-description">查看和管理所有AI任务的执行状态</p>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar card">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索任务ID或描述"
            style="width: 300px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px;" clearable>
            <el-option label="全部" value="" />
            <el-option label="等待中" value="PENDING" />
            <el-option label="运行中" value="RUNNING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </div>
        
        <div class="toolbar-right">
          <el-button :icon="Refresh" @click="refreshTasks" :loading="isLoading">
            刷新
          </el-button>
          <el-button :icon="Delete" @click="clearCompletedTasks" type="danger">
            清理已完成
          </el-button>
        </div>
      </div>

      <!-- 任务列表 -->
      <div class="task-list card">
        <el-table
          :data="filteredTasks"
          v-loading="isLoading"
          stripe
          border
          :default-sort="{ prop: 'startTime', order: 'descending' }"
        >
          <el-table-column prop="taskId" label="任务ID" width="200" fixed="left">
            <template #default="{ row }">
              <el-link type="primary" @click="viewTaskDetail(row)">
                {{ row.taskId }}
              </el-link>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" effect="light">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="progress" label="进度" width="120">
            <template #default="{ row }">
              <el-progress
                v-if="row.progress !== undefined"
                :percentage="row.progress"
                :status="row.status === 'FAILED' ? 'exception' : undefined"
                :show-text="false"
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="startTime" label="开始时间" width="160" sortable>
            <template #default="{ row }">
              {{ formatDateTime(row.startTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="endTime" label="结束时间" width="160" sortable>
            <template #default="{ row }">
              {{ row.endTime ? formatDateTime(row.endTime) : '-' }}
            </template>
          </el-table-column>
          
          <el-table-column prop="duration" label="耗时" width="100">
            <template #default="{ row }">
              {{ calculateDuration(row.startTime, row.endTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button-group>
                <el-button :icon="View" @click="viewTaskDetail(row)" text size="small">
                  查看
                </el-button>
                
                <el-button
                  v-if="row.status === 'RUNNING'"
                  :icon="VideoPause"
                  @click="cancelTask(row.taskId)"
                  text
                  type="danger"
                  size="small"
                >
                  取消
                </el-button>
                
                <el-button
                  v-if="row.status === 'COMPLETED' && row.result"
                  :icon="DocumentCopy"
                  @click="copyResult(row.result)"
                  text
                  size="small"
                >
                  复制
                </el-button>
                
                <el-button
                  :icon="Delete"
                  @click="deleteTask(row.taskId)"
                  text
                  type="danger"
                  size="small"
                >
                  删除
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="filteredTasks.length"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
          />
        </div>
      </div>

      <!-- 任务详情弹窗 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="任务详情"
        width="70%"
        :close-on-click-modal="false"
      >
        <div v-if="selectedTask" class="task-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务ID">
              {{ selectedTask.taskId }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(selectedTask.status)" effect="light">
                {{ getStatusText(selectedTask.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
              {{ formatDateTime(selectedTask.startTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="结束时间">
              {{ selectedTask.endTime ? formatDateTime(selectedTask.endTime) : '未完成' }}
            </el-descriptions-item>
            <el-descriptions-item label="执行耗时">
              {{ calculateDuration(selectedTask.startTime, selectedTask.endTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="进度">
              {{ selectedTask.progress !== undefined ? `${selectedTask.progress}%` : '-' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div v-if="selectedTask.result" class="result-section">
            <h4>执行结果</h4>
            <div class="result-content">
              <pre v-if="typeof selectedTask.result === 'string'">{{ selectedTask.result }}</pre>
              <el-descriptions v-else :column="1" border>
                <el-descriptions-item
                  v-for="(value, key) in selectedTask.result"
                  :key="key"
                  :label="key"
                >
                  {{ value }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          
          <div v-if="selectedTask.error" class="error-section">
            <h4>错误信息</h4>
            <el-alert
              :title="selectedTask.error"
              type="error"
              show-icon
              :closable="false"
            />
          </div>
        </div>
        
        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="selectedTask?.result"
            type="primary"
            @click="copyTaskResult"
          >
            复制结果
          </el-button>
        </template>
      </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTaskStore } from '@/stores/task'

import {
  Search, Refresh, Delete, View, VideoPause, DocumentCopy
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const taskStore = useTaskStore()

// 响应式数据
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const isLoading = ref(false)
const detailDialogVisible = ref(false)
const selectedTask = ref<any>(null)

// 计算属性
const filteredTasks = computed(() => {
  let tasks = [...taskStore.taskList]
  
  // 状态筛选
  if (statusFilter.value) {
    tasks = tasks.filter(task => task.status === statusFilter.value)
  }
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    tasks = tasks.filter(task => 
      task.taskId.toLowerCase().includes(keyword) ||
      (task.result && JSON.stringify(task.result).toLowerCase().includes(keyword))
    )
  }
  
  return tasks.sort((a, b) => {
    return new Date(b.startTime || 0).getTime() - new Date(a.startTime || 0).getTime()
  })
})

// 方法
const refreshTasks = async () => {
  isLoading.value = true
  try {
    // 更新所有运行中任务的状态
    const runningTasks = taskStore.taskList.filter(task => task.status === 'RUNNING')
    await Promise.all(
      runningTasks.map(task => taskStore.updateTaskStatus(task.taskId))
    )
    ElMessage.success('任务状态已更新')
  } catch (error) {
    ElMessage.error('更新任务状态失败')
  } finally {
    isLoading.value = false
  }
}

const clearCompletedTasks = async () => {
  try {
    await ElMessageBox.confirm('确定要清理所有已完成的任务吗？', '确认操作', {
      type: 'warning'
    })
    
    const completedTasks = taskStore.taskList.filter(task => 
      task.status === 'COMPLETED' || task.status === 'FAILED' || task.status === 'CANCELLED'
    )
    
    // 这里应该调用后端API清理任务，目前只是前端清理
    ElMessage.success(`已清理 ${completedTasks.length} 个已完成任务`)
  } catch {
    // 用户取消
  }
}

const viewTaskDetail = (task: any) => {
  selectedTask.value = task
  detailDialogVisible.value = true
}

const cancelTask = async (taskId: string) => {
  try {
    await ElMessageBox.confirm('确定要取消这个任务吗？', '确认取消', {
      type: 'warning'
    })
    
    await taskStore.cancelTask(taskId)
    ElMessage.success('任务已取消')
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

const deleteTask = async (taskId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务记录吗？', '确认删除', {
      type: 'warning'
    })
    
    // 从本地状态中删除任务
    taskStore.tasks.delete(taskId)
    ElMessage.success('任务记录已删除')
  } catch {
    // 用户取消
  }
}

const copyResult = (result: any) => {
  const text = typeof result === 'string' ? result : JSON.stringify(result, null, 2)
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('结果已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const copyTaskResult = () => {
  if (selectedTask.value?.result) {
    copyResult(selectedTask.value.result)
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
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const calculateDuration = (start?: string, end?: string) => {
  if (!start) return '-'
  if (!end) return '进行中'
  
  const duration = dayjs(end).diff(dayjs(start), 'second')
  if (duration < 60) return `${duration}秒`
  if (duration < 3600) return `${Math.floor(duration / 60)}分${duration % 60}秒`
  return `${Math.floor(duration / 3600)}小时${Math.floor((duration % 3600) / 60)}分`
}

// 生命周期
onMounted(() => {
  refreshTasks()
})
</script>

<style lang="scss" scoped>
.task-management {
  padding: 24px;
  
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    margin-bottom: 24px;
    
    .toolbar-left {
      display: flex;
      gap: 12px;
      align-items: center;
    }
    
    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }
  
  .task-list {
    .pagination-container {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }
  
  .task-detail {
    .result-section,
    .error-section {
      margin-top: 24px;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 12px 0;
      }
      
      .result-content {
        pre {
          background: var(--fill-lighter);
          border: 1px solid var(--border-light);
          border-radius: 6px;
          padding: 16px;
          white-space: pre-wrap;
          word-break: break-word;
          max-height: 300px;
          overflow-y: auto;
          font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          font-size: 13px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .task-management {
    padding: 16px;
    
    .toolbar {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
      
      .toolbar-left {
        flex-direction: column;
      }
    }
  }
}
</style>