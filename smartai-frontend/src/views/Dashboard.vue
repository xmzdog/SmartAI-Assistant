<template>
  <div class="dashboard page-container">
      <div class="page-header">
        <h1 class="page-title">仪表板</h1>
        <p class="page-description">SmartAI Assistant 系统总览</p>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#409eff"><List /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.totalTasks || 0 }}</div>
            <div class="stat-label">总任务数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#67c23a"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.completedTasks || 0 }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#f56c6c"><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.failedTasks || 0 }}</div>
            <div class="stat-label">失败任务</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#e6a23c"><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatTime(statistics?.averageExecutionTime) }}</div>
            <div class="stat-label">平均耗时</div>
          </div>
        </div>
      </div>

      <!-- 系统状态和最近任务 -->
      <div class="content-grid">
        <!-- 系统状态 -->
        <div class="status-panel card">
          <div class="panel-header">
            <h3>系统状态</h3>
            <el-button :icon="Refresh" @click="refreshStatus" :loading="systemStore.isLoading" text />
          </div>
          
          <div class="status-content">
            <div class="status-item">
              <span class="status-label">Agent状态:</span>
              <el-tag
                :type="getStatusType(agentStatus?.status)"
                effect="light"
              >
                {{ getStatusText(agentStatus?.status) }}
              </el-tag>
            </div>
            
            <div class="status-item">
              <span class="status-label">活跃任务:</span>
              <span class="status-value">{{ agentStatus?.activeTaskCount || 0 }}</span>
            </div>
            
            <div class="status-item">
              <span class="status-label">运行时间:</span>
              <span class="status-value">{{ agentStatus?.uptime || 'N/A' }}</span>
            </div>
            
            <div class="status-item">
              <span class="status-label">最后活动:</span>
              <span class="status-value">{{ formatDateTime(agentStatus?.lastActivity) }}</span>
            </div>
            
            <div class="status-item">
              <span class="status-label">系统负载:</span>
              <el-progress
                :percentage="(statistics?.systemLoad || 0) * 100"
                :color="getLoadColor(statistics?.systemLoad)"
                :show-text="false"
                style="margin-top: 4px;"
              />
              <span class="status-value">{{ ((statistics?.systemLoad || 0) * 100).toFixed(1) }}%</span>
            </div>
          </div>
        </div>

        <!-- 最近任务 -->
        <div class="recent-tasks card">
          <div class="panel-header">
            <h3>最近任务</h3>
            <el-button @click="$router.push('/task')" text>查看全部</el-button>
          </div>
          
          <div class="task-list">
            <div v-if="recentTasks.length === 0" class="empty-state">
              <el-icon size="48" color="#c0c4cc"><DocumentCopy /></el-icon>
              <p>暂无任务记录</p>
            </div>
            
            <div
              v-for="task in recentTasks"
              :key="task.taskId"
              class="task-item"
              @click="viewTaskDetail(task)"
            >
              <div class="task-icon">
                <el-icon 
                  size="20" 
                  :color="getTaskIconColor(task.status)"
                >
                  <component :is="getTaskIcon(task.status)" />
                </el-icon>
              </div>
              
              <div class="task-info">
                <div class="task-title">{{ task.result?.title || '任务 ' + task.taskId }}</div>
                <div class="task-desc">{{ task.result?.summary || '暂无描述' }}</div>
                <div class="task-time">{{ formatDateTime(task.startTime) }}</div>
              </div>
              
              <div class="task-status">
                <el-tag
                  :type="getTaskStatusType(task.status)"
                  effect="light"
                  size="small"
                >
                  {{ getTaskStatusText(task.status) }}
                </el-tag>
                <div v-if="task.status === 'RUNNING' && task.progress" class="task-progress">
                  <el-progress
                    :percentage="task.progress"
                    :show-text="false"
                    :stroke-width="3"
                    status="active"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions card">
        <div class="panel-header">
          <h3>快捷操作</h3>
        </div>
        
        <div class="action-grid">
          <el-button
            type="primary"
            :icon="Cpu"
            @click="$router.push('/manus')"
            class="action-btn"
          >
            ManusAgent
          </el-button>
          
          <el-button
            type="success"
            :icon="Service"
            @click="$router.push('/agent')"
            class="action-btn"
          >
            智能Agent
          </el-button>
          
          <el-button
            type="warning"
            :icon="MagicStick"
            @click="$router.push('/reasoning')"
            class="action-btn"
          >
            深度推理
          </el-button>
          
          <el-button
            type="info"
            :icon="Tools"
            @click="$router.push('/tools')"
            class="action-btn"
          >
            工具管理
          </el-button>
        </div>
      </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSystemStore } from '@/stores/system'
import { useTaskStore } from '@/stores/task'

import {
  List, CircleCheck, CircleClose, Timer, Refresh, DocumentCopy,
  Cpu, Service, MagicStick, Tools, Loading, WarningFilled, SuccessFilled, InfoFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const systemStore = useSystemStore()
const taskStore = useTaskStore()

// 计算属性
const agentStatus = computed(() => systemStore.agentStatus)
const statistics = computed(() => systemStore.statistics)
const recentTasks = computed(() => taskStore.taskList.slice(0, 5))

// 方法
const refreshStatus = async () => {
  try {
    await Promise.all([
      systemStore.fetchAgentStatus(),
      systemStore.fetchStatistics(),
      taskStore.fetchTaskHistory(5)
    ])
    ElMessage.success('状态已更新')
  } catch (error) {
    console.error('更新状态失败:', error)
    // 不显示错误消息，因为我们有模拟数据兼底
  }
}

const getStatusType = (status?: string) => {
  switch (status) {
    case 'IDLE': return 'success'
    case 'BUSY': return 'warning'
    case 'ERROR': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'IDLE': return '空闲'
    case 'BUSY': return '忙碌'
    case 'ERROR': return '错误'
    default: return '未知'
  }
}

const getTaskStatusType = (status: string) => {
  switch (status) {
    case 'COMPLETED': return 'success'
    case 'RUNNING': return 'warning'
    case 'FAILED': return 'danger'
    case 'CANCELLED': return 'info'
    default: return 'info'
  }
}

const getTaskStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '等待中'
    case 'RUNNING': return '运行中'
    case 'COMPLETED': return '已完成'
    case 'FAILED': return '失败'
    case 'CANCELLED': return '已取消'
    default: return '未知'
  }
}

const getLoadColor = (load?: number) => {
  if (!load) return '#67c23a'
  if (load < 0.5) return '#67c23a'
  if (load < 0.8) return '#e6a23c'
  return '#f56c6c'
}

const formatTime = (seconds?: number) => {
  if (!seconds) return 'N/A'
  if (seconds < 60) return `${seconds.toFixed(1)}s`
  if (seconds < 3600) return `${(seconds / 60).toFixed(1)}m`
  return `${(seconds / 3600).toFixed(1)}h`
}

const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return 'N/A'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const viewTaskDetail = (task: any) => {
  router.push(`/task?taskId=${task.taskId}`)
}

const getTaskIcon = (status: string) => {
  switch (status) {
    case 'RUNNING': return Loading
    case 'COMPLETED': return SuccessFilled
    case 'FAILED': return WarningFilled
    case 'CANCELLED': return InfoFilled
    default: return DocumentCopy
  }
}

const getTaskIconColor = (status: string) => {
  switch (status) {
    case 'RUNNING': return '#409eff'
    case 'COMPLETED': return '#67c23a'
    case 'FAILED': return '#f56c6c'
    case 'CANCELLED': return '#909399'
    default: return '#909399'
  }
}

// 生命周期
onMounted(() => {
  refreshStatus()
  // 初始化任务数据
  taskStore.fetchTaskHistory(5)
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 24px;
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 24px;
    margin-bottom: 24px;
    
    .stat-card {
      background: var(--bg-white);
      border: 1px solid var(--border-light);
      border-radius: 12px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.3s;
      
      &:hover {
        box-shadow: var(--shadow-light);
        transform: translateY(-2px);
      }
      
      .stat-icon {
        flex-shrink: 0;
      }
      
      .stat-content {
        .stat-value {
          font-size: 32px;
          font-weight: 600;
          color: var(--text-primary);
          line-height: 1;
        }
        
        .stat-label {
          font-size: 14px;
          color: var(--text-secondary);
          margin-top: 4px;
        }
      }
    }
  }
  
  .content-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
    margin-bottom: 24px;
    
    @media (max-width: 1024px) {
      grid-template-columns: 1fr;
    }
  }
  
  .panel-header {
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
  }
  
  .status-panel {
    padding: 24px;
    
    .status-content {
      .status-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .status-label {
          font-size: 14px;
          color: var(--text-secondary);
        }
        
        .status-value {
          font-size: 14px;
          color: var(--text-primary);
          font-weight: 500;
        }
      }
    }
  }
  
  .recent-tasks {
    padding: 24px;
    
    .task-list {
      .task-item {
        display: flex;
        align-items: center;
        padding: 16px 0;
        border-bottom: 1px solid var(--border-light);
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background-color: var(--fill-lighter);
          margin: 0 -16px;
          padding-left: 16px;
          padding-right: 16px;
          border-radius: 8px;
          transform: translateX(4px);
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .task-icon {
          margin-right: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        
        .task-info {
          flex: 1;
          min-width: 0;
          
          .task-title {
            font-size: 14px;
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .task-desc {
            font-size: 12px;
            color: var(--text-regular);
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .task-time {
            font-size: 11px;
            color: var(--text-placeholder);
          }
        }
        
        .task-status {
          text-align: right;
          min-width: 80px;
          
          .task-progress {
            margin-top: 6px;
            width: 60px;
          }
        }
      }
    }
  }
  
  .quick-actions {
    padding: 24px;
    
    .action-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      
      .action-btn {
        height: 48px;
        font-size: 16px;
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
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
    
    .stats-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }
    
    .content-grid {
      gap: 16px;
    }
    
    .action-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>