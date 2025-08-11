<template>
  <div class="monitoring page-container">
      <div class="page-header">
        <h1 class="page-title">系统监控</h1>
        <p class="page-description">实时监控AI智能体系统的运行状态和性能指标</p>
      </div>

      <!-- 系统状态卡片 -->
      <div class="status-cards">
        <div class="status-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon size="20" color="#409eff"><Monitor /></el-icon>
              <span>Agent状态</span>
            </div>
            <el-tag
              :type="getStatusType(agentStatus?.status)"
              effect="light"
            >
              {{ getStatusText(agentStatus?.status) }}
            </el-tag>
          </div>
          <div class="card-content">
            <div class="status-item">
              <span class="label">活跃任务:</span>
              <span class="value">{{ agentStatus?.activeTaskCount || 0 }}</span>
            </div>
            <div class="status-item">
              <span class="label">运行时间:</span>
              <span class="value">{{ agentStatus?.uptime || 'N/A' }}</span>
            </div>
            <div class="status-item">
              <span class="label">最后活动:</span>
              <span class="value">{{ formatDateTime(agentStatus?.lastActivity) }}</span>
            </div>
          </div>
        </div>

        <div class="status-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon size="20" color="#67c23a"><TrendCharts /></el-icon>
              <span>系统负载</span>
            </div>
            <span class="load-value">{{ ((statistics?.systemLoad || 0) * 100).toFixed(1) }}%</span>
          </div>
          <div class="card-content">
            <el-progress
              :percentage="(statistics?.systemLoad || 0) * 100"
              :color="getLoadColor(statistics?.systemLoad)"
              :stroke-width="12"
            />
            <div class="load-description">
              {{ getLoadDescription(statistics?.systemLoad) }}
            </div>
          </div>
        </div>

        <div class="status-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon size="20" color="#e6a23c"><User /></el-icon>
              <span>活跃用户</span>
            </div>
            <span class="user-count">{{ statistics?.activeUsers || 0 }}</span>
          </div>
          <div class="card-content">
            <div class="user-chart">
              <!-- 这里可以添加用户活跃度图表 -->
              <div class="chart-placeholder">
                <el-icon size="32" color="#c0c4cc"><DataAnalysis /></el-icon>
                <span>用户活跃度图表</span>
              </div>
            </div>
          </div>
        </div>

        <div class="status-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon size="20" color="#f56c6c"><Clock /></el-icon>
              <span>平均响应</span>
            </div>
            <span class="response-time">{{ formatTime(statistics?.averageExecutionTime) }}</span>
          </div>
          <div class="card-content">
            <div class="response-chart">
              <!-- 这里可以添加响应时间图表 -->
              <div class="chart-placeholder">
                <el-icon size="32" color="#c0c4cc"><DataLine /></el-icon>
                <span>响应时间趋势</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 实时数据图表 -->
      <div class="charts-section">
        <div class="chart-container card">
          <div class="chart-header">
            <h3>任务执行趋势</h3>
            <div class="chart-controls">
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
                <el-radio-button label="7d">7天</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="chart-content">
            <div class="chart-placeholder large">
              <el-icon size="48" color="#c0c4cc"><TrendCharts /></el-icon>
              <span>任务执行趋势图表</span>
              <p>显示不同时间段的任务执行统计</p>
            </div>
          </div>
        </div>

        <div class="chart-container card">
          <div class="chart-header">
            <h3>成功率统计</h3>
          </div>
          <div class="chart-content">
            <div class="success-stats">
              <div class="stat-item">
                <div class="stat-circle success">
                  <span>{{ getSuccessRate() }}%</span>
                </div>
                <div class="stat-label">总成功率</div>
              </div>
              
              <div class="stat-details">
                <div class="detail-item">
                  <span class="detail-label">总任务数:</span>
                  <span class="detail-value">{{ statistics?.totalTasks || 0 }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">成功任务:</span>
                  <span class="detail-value success">{{ statistics?.completedTasks || 0 }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">失败任务:</span>
                  <span class="detail-value failed">{{ statistics?.failedTasks || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统日志 -->
      <div class="logs-section card">
        <div class="section-header">
          <h3>系统日志</h3>
          <div class="log-controls">
            <el-select v-model="logLevel" placeholder="日志级别" style="width: 120px;">
              <el-option label="全部" value="" />
              <el-option label="错误" value="error" />
              <el-option label="警告" value="warning" />
              <el-option label="信息" value="info" />
              <el-option label="调试" value="debug" />
            </el-select>
            
            <el-button :icon="Refresh" @click="refreshLogs" :loading="isLoadingLogs" text>
              刷新
            </el-button>
            
            <el-button :icon="Download" @click="downloadLogs" text>
              导出日志
            </el-button>
          </div>
        </div>
        
        <div class="logs-content">
          <div v-if="logs.length === 0" class="empty-logs">
            <el-icon size="32" color="#c0c4cc"><Document /></el-icon>
            <span>暂无日志记录</span>
          </div>
          
          <div v-else class="log-list">
            <div
              v-for="(log, index) in filteredLogs"
              :key="index"
              class="log-item"
              :class="log.level"
            >
              <div class="log-time">{{ formatDateTime(log.timestamp) }}</div>
              <div class="log-level">
                <el-tag :type="getLogLevelType(log.level)" size="small">
                  {{ log.level.toUpperCase() }}
                </el-tag>
              </div>
              <div class="log-message">{{ log.message }}</div>
            </div>
          </div>
          
          <div class="logs-pagination">
            <el-pagination
              v-model:current-page="logCurrentPage"
              :page-size="logPageSize"
              :total="filteredLogs.length"
              layout="prev, pager, next"
              small
            />
          </div>
        </div>
      </div>

      <!-- 性能指标 -->
      <div class="metrics-section card">
        <div class="section-header">
          <h3>性能指标</h3>
          <el-button :icon="Refresh" @click="refreshMetrics" :loading="isLoadingMetrics" text>
            刷新指标
          </el-button>
        </div>
        
        <div class="metrics-grid">
          <div class="metric-item">
            <div class="metric-label">内存使用</div>
            <div class="metric-value">
              <el-progress
                type="circle"
                :percentage="75"
                :width="60"
                color="#409eff"
              />
              <span class="metric-text">75%</span>
            </div>
          </div>
          
          <div class="metric-item">
            <div class="metric-label">CPU使用</div>
            <div class="metric-value">
              <el-progress
                type="circle"
                :percentage="45"
                :width="60"
                color="#67c23a"
              />
              <span class="metric-text">45%</span>
            </div>
          </div>
          
          <div class="metric-item">
            <div class="metric-label">磁盘使用</div>
            <div class="metric-value">
              <el-progress
                type="circle"
                :percentage="60"
                :width="60"
                color="#e6a23c"
              />
              <span class="metric-text">60%</span>
            </div>
          </div>
          
          <div class="metric-item">
            <div class="metric-label">网络I/O</div>
            <div class="metric-value">
              <el-progress
                type="circle"
                :percentage="30"
                :width="60"
                color="#909399"
              />
              <span class="metric-text">30%</span>
            </div>
          </div>
        </div>
      </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useSystemStore } from '@/stores/system'

import {
  Monitor, TrendCharts, User, Clock, DataAnalysis, DataLine,
  Refresh, Download, Document
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const systemStore = useSystemStore()

// 响应式数据
const timeRange = ref('24h')
const logLevel = ref('')
const logCurrentPage = ref(1)
const logPageSize = ref(20)
const isLoadingLogs = ref(false)
const isLoadingMetrics = ref(false)
const refreshTimer = ref<NodeJS.Timeout | null>(null)

// 模拟日志数据
const logs = ref([
  {
    timestamp: new Date().toISOString(),
    level: 'info',
    message: 'ManusAgent 任务执行成功 - 任务ID: task_12345'
  },
  {
    timestamp: new Date(Date.now() - 60000).toISOString(),
    level: 'warning',
    message: '系统负载较高，建议优化资源配置'
  },
  {
    timestamp: new Date(Date.now() - 120000).toISOString(),
    level: 'error',
    message: '任务执行失败 - 连接超时'
  },
  {
    timestamp: new Date(Date.now() - 180000).toISOString(),
    level: 'info',
    message: '新用户连接 - 用户ID: user_789'
  }
])

// 计算属性
const agentStatus = computed(() => systemStore.agentStatus)
const statistics = computed(() => systemStore.statistics)

const filteredLogs = computed(() => {
  let filtered = [...logs.value]
  
  if (logLevel.value) {
    filtered = filtered.filter(log => log.level === logLevel.value)
  }
  
  return filtered.slice(
    (logCurrentPage.value - 1) * logPageSize.value,
    logCurrentPage.value * logPageSize.value
  )
})

// 方法
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

const getLoadColor = (load?: number) => {
  if (!load) return '#67c23a'
  if (load < 0.5) return '#67c23a'
  if (load < 0.8) return '#e6a23c'
  return '#f56c6c'
}

const getLoadDescription = (load?: number) => {
  if (!load) return '系统空闲'
  if (load < 0.3) return '系统空闲'
  if (load < 0.6) return '负载正常'
  if (load < 0.8) return '负载较高'
  return '负载过高'
}

const getSuccessRate = () => {
  const total = statistics.value?.totalTasks || 0
  const completed = statistics.value?.completedTasks || 0
  
  if (total === 0) return 0
  return Math.round((completed / total) * 100)
}

const getLogLevelType = (level: string) => {
  switch (level) {
    case 'error': return 'danger'
    case 'warning': return 'warning'
    case 'info': return 'primary'
    case 'debug': return 'info'
    default: return 'info'
  }
}

const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return 'N/A'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const formatTime = (seconds?: number) => {
  if (!seconds) return 'N/A'
  if (seconds < 1) return `${(seconds * 1000).toFixed(0)}ms`
  if (seconds < 60) return `${seconds.toFixed(1)}s`
  if (seconds < 3600) return `${(seconds / 60).toFixed(1)}m`
  return `${(seconds / 3600).toFixed(1)}h`
}

const refreshLogs = async () => {
  isLoadingLogs.value = true
  try {
    // 模拟刷新日志
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 添加新的日志条目
    logs.value.unshift({
      timestamp: new Date().toISOString(),
      level: 'info',
      message: '日志已刷新'
    })
    
    ElMessage.success('日志已更新')
  } catch (error) {
    ElMessage.error('刷新日志失败')
  } finally {
    isLoadingLogs.value = false
  }
}

const downloadLogs = () => {
  const logText = logs.value
    .map(log => `[${formatDateTime(log.timestamp)}] ${log.level.toUpperCase()}: ${log.message}`)
    .join('\n')
  
  const blob = new Blob([logText], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `system-logs-${dayjs().format('YYYY-MM-DD')}.log`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  ElMessage.success('日志已导出')
}

const refreshMetrics = async () => {
  isLoadingMetrics.value = true
  try {
    await Promise.all([
      systemStore.fetchAgentStatus(),
      systemStore.fetchStatistics()
    ])
    ElMessage.success('指标已更新')
  } catch (error) {
    ElMessage.error('更新指标失败')
  } finally {
    isLoadingMetrics.value = false
  }
}

const startAutoRefresh = () => {
  refreshTimer.value = setInterval(() => {
    if (systemStore.autoRefresh) {
      refreshMetrics()
    }
  }, systemStore.refreshInterval * 1000)
}

const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

// 生命周期
onMounted(() => {
  refreshMetrics()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.monitoring {
  padding: 24px;
  
  .status-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    .status-card {
      background: var(--bg-white);
      border: 1px solid var(--border-light);
      border-radius: 12px;
      padding: 20px;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        
        .card-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 500;
          color: var(--text-primary);
        }
        
        .load-value,
        .user-count,
        .response-time {
          font-size: 18px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }
      
      .card-content {
        .status-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          
          .label {
            color: var(--text-secondary);
            font-size: 14px;
          }
          
          .value {
            color: var(--text-primary);
            font-weight: 500;
          }
        }
        
        .load-description {
          text-align: center;
          margin-top: 8px;
          font-size: 12px;
          color: var(--text-placeholder);
        }
        
        .chart-placeholder {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 80px;
          color: var(--text-placeholder);
          font-size: 12px;
          
          span {
            margin-top: 8px;
          }
        }
      }
    }
  }
  
  .charts-section {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
    margin-bottom: 24px;
    
    .chart-container {
      padding: 24px;
      
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        h3 {
          font-size: 16px;
          font-weight: 600;
          color: var(--text-primary);
          margin: 0;
        }
      }
      
      .chart-content {
        .chart-placeholder {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 200px;
          color: var(--text-placeholder);
          text-align: center;
          
          &.large {
            height: 300px;
          }
          
          span {
            margin-top: 12px;
            font-size: 16px;
          }
          
          p {
            margin-top: 8px;
            font-size: 12px;
          }
        }
        
        .success-stats {
          display: flex;
          align-items: center;
          gap: 24px;
          
          .stat-item {
            text-align: center;
            
            .stat-circle {
              width: 80px;
              height: 80px;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              margin-bottom: 8px;
              font-size: 18px;
              font-weight: 600;
              color: white;
              
              &.success {
                background: var(--success-color);
              }
            }
            
            .stat-label {
              font-size: 14px;
              color: var(--text-secondary);
            }
          }
          
          .stat-details {
            flex: 1;
            
            .detail-item {
              display: flex;
              justify-content: space-between;
              margin-bottom: 8px;
              
              .detail-label {
                color: var(--text-secondary);
              }
              
              .detail-value {
                font-weight: 500;
                
                &.success {
                  color: var(--success-color);
                }
                
                &.failed {
                  color: var(--danger-color);
                }
              }
            }
          }
        }
      }
    }
  }
  
  .logs-section {
    padding: 24px;
    margin-bottom: 24px;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0;
      }
      
      .log-controls {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }
    
    .logs-content {
      .empty-logs {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 120px;
        color: var(--text-placeholder);
        
        span {
          margin-top: 8px;
          font-size: 14px;
        }
      }
      
      .log-list {
        max-height: 300px;
        overflow-y: auto;
        
        .log-item {
          display: grid;
          grid-template-columns: 160px 80px 1fr;
          gap: 12px;
          padding: 8px 0;
          border-bottom: 1px solid var(--border-light);
          font-size: 13px;
          
          &:last-child {
            border-bottom: none;
          }
          
          .log-time {
            color: var(--text-placeholder);
            font-family: monospace;
          }
          
          .log-message {
            color: var(--text-regular);
          }
        }
      }
      
      .logs-pagination {
        display: flex;
        justify-content: center;
        margin-top: 16px;
      }
    }
  }
  
  .metrics-section {
    padding: 24px;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0;
      }
    }
    
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 24px;
      
      .metric-item {
        text-align: center;
        
        .metric-label {
          font-size: 14px;
          color: var(--text-secondary);
          margin-bottom: 12px;
        }
        
        .metric-value {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;
          
          .metric-text {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-primary);
          }
        }
      }
    }
  }
}

@media (max-width: 1024px) {
  .monitoring {
    .charts-section {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .monitoring {
    padding: 16px;
    
    .status-cards {
      grid-template-columns: 1fr;
    }
    
    .section-header {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;
    }
    
    .log-item {
      grid-template-columns: 1fr;
      gap: 4px;
    }
    
    .metrics-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>