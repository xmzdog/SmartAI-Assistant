<template>
  <div class="monitoring-container">
    <div class="page-header">
      <h1>系统监控</h1>
      <p>监控系统运行状态和性能指标</p>
    </div>

    <!-- 系统状态概览 -->
    <div class="status-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-icon" :class="{ 'status-healthy': systemStatus.healthy }">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="status-info">
                <h3>系统状态</h3>
                <p :class="{ 'text-success': systemStatus.healthy, 'text-danger': !systemStatus.healthy }">
                  {{ systemStatus.healthy ? '正常' : '异常' }}
                </p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-icon cpu">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="status-info">
                <h3>CPU使用率</h3>
                <p>{{ systemMetrics.cpu }}%</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-icon memory">
                <el-icon><Odometer /></el-icon>
              </div>
              <div class="status-info">
                <h3>内存使用率</h3>
                <p>{{ systemMetrics.memory }}%</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-icon disk">
                <el-icon><FolderOpened /></el-icon>
              </div>
              <div class="status-info">
                <h3>磁盘使用率</h3>
                <p>{{ systemMetrics.disk }}%</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 性能图表 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>CPU使用率趋势</span>
                <el-button type="text" @click="refreshCharts">刷新</el-button>
              </div>
            </template>
            <div class="chart-container" id="cpuChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>内存使用率趋势</span>
                <el-button type="text" @click="refreshCharts">刷新</el-button>
              </div>
            </template>
            <div class="chart-container" id="memoryChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 系统日志 -->
    <div class="logs-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>系统日志</span>
            <div>
              <el-select v-model="logLevel" placeholder="日志级别" style="margin-right: 10px; width: 120px">
                <el-option label="全部" value="all"></el-option>
                <el-option label="错误" value="error"></el-option>
                <el-option label="警告" value="warning"></el-option>
                <el-option label="信息" value="info"></el-option>
              </el-select>
              <el-button type="primary" @click="refreshLogs">刷新日志</el-button>
            </div>
          </div>
        </template>
        <div class="logs-container">
          <div v-if="logs.length === 0" class="no-data">
            暂无日志数据
          </div>
          <div v-else class="log-list">
            <div 
              v-for="log in filteredLogs" 
              :key="log.id"
              class="log-item"
              :class="`log-${log.level}`"
            >
              <span class="log-time">{{ formatTime(log.timestamp) }}</span>
              <span class="log-level">{{ log.level.toUpperCase() }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 服务状态 -->
    <div class="services-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>服务状态</span>
            <el-button type="primary" @click="refreshServices">刷新状态</el-button>
          </div>
        </template>
        <el-table :data="services" style="width: 100%">
          <el-table-column prop="name" label="服务名称" width="200"></el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'running' ? 'success' : 'danger'">
                {{ scope.row.status === 'running' ? '运行中' : '已停止' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="port" label="端口" width="100"></el-table-column>
          <el-table-column prop="uptime" label="运行时间" width="150"></el-table-column>
          <el-table-column prop="description" label="描述"></el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button 
                v-if="scope.row.status === 'running'"
                type="danger" 
                size="small"
                @click="stopService(scope.row)"
              >
                停止
              </el-button>
              <el-button 
                v-else
                type="success" 
                size="small"
                @click="startService(scope.row)"
              >
                启动
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Cpu, Odometer, FolderOpened } from '@element-plus/icons-vue'
import { useSystemStore } from '@/stores/system'

const systemStore = useSystemStore()

// 系统状态
const systemStatus = ref({
  healthy: true
})

// 系统指标
const systemMetrics = ref({
  cpu: 45,
  memory: 68,
  disk: 32
})

// 日志相关
const logLevel = ref('all')
const logs = ref([
  {
    id: 1,
    level: 'info',
    message: '系统启动成功',
    timestamp: new Date(Date.now() - 60000)
  },
  {
    id: 2,
    level: 'warning',
    message: 'API响应时间较长',
    timestamp: new Date(Date.now() - 30000)
  },
  {
    id: 3,
    level: 'error',
    message: '数据库连接超时',
    timestamp: new Date()
  }
])

// 服务状态
const services = ref([
  {
    id: 1,
    name: 'SmartAI Core',
    status: 'running',
    port: 8080,
    uptime: '2天 3小时',
    description: '核心AI服务'
  },
  {
    id: 2,
    name: 'Database Service',
    status: 'running',
    port: 3306,
    uptime: '2天 3小时',
    description: '数据库服务'
  },
  {
    id: 3,
    name: 'Cache Service',
    status: 'stopped',
    port: 6379,
    uptime: '-',
    description: 'Redis缓存服务'
  }
])

// 计算属性
const filteredLogs = computed(() => {
  if (logLevel.value === 'all') {
    return logs.value
  }
  return logs.value.filter(log => log.level === logLevel.value)
})

// 方法
const refreshCharts = () => {
  ElMessage.success('图表已刷新')
  // 这里可以添加实际的图表刷新逻辑
}

const refreshLogs = () => {
  ElMessage.success('日志已刷新')
  // 这里可以添加实际的日志刷新逻辑
}

const refreshServices = () => {
  ElMessage.success('服务状态已刷新')
  // 这里可以添加实际的服务状态刷新逻辑
}

const startService = async (service) => {
  try {
    await ElMessageBox.confirm(`确定要启动服务 ${service.name} 吗？`, '确认启动', {
      type: 'warning'
    })
    
    // 模拟启动服务
    service.status = 'running'
    service.uptime = '刚刚启动'
    ElMessage.success(`服务 ${service.name} 启动成功`)
  } catch {
    // 用户取消
  }
}

const stopService = async (service) => {
  try {
    await ElMessageBox.confirm(`确定要停止服务 ${service.name} 吗？`, '确认停止', {
      type: 'warning'
    })
    
    // 模拟停止服务
    service.status = 'stopped'
    service.uptime = '-'
    ElMessage.success(`服务 ${service.name} 已停止`)
  } catch {
    // 用户取消
  }
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString()
}

// 模拟实时数据更新
let metricsInterval
const updateMetrics = () => {
  systemMetrics.value.cpu = Math.floor(Math.random() * 100)
  systemMetrics.value.memory = Math.floor(Math.random() * 100)
  systemMetrics.value.disk = Math.floor(Math.random() * 100)
}

onMounted(() => {
  // 启动实时数据更新
  metricsInterval = setInterval(updateMetrics, 5000)
})

onUnmounted(() => {
  if (metricsInterval) {
    clearInterval(metricsInterval)
  }
})
</script>

<style scoped>
.monitoring-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.page-header p {
  margin: 5px 0 0 0;
  color: #909399;
}

.status-overview {
  margin-bottom: 20px;
}

.status-card {
  height: 120px;
}

.status-item {
  display: flex;
  align-items: center;
  height: 100%;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
  background: #909399;
}

.status-icon.status-healthy {
  background: #67c23a;
}

.status-icon.cpu {
  background: #409eff;
}

.status-icon.memory {
  background: #e6a23c;
}

.status-icon.disk {
  background: #f56c6c;
}

.status-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.status-info p {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
}

.logs-section {
  margin-bottom: 20px;
}

.logs-container {
  max-height: 400px;
  overflow-y: auto;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 40px;
}

.log-list {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.log-time {
  width: 150px;
  color: #909399;
  flex-shrink: 0;
}

.log-level {
  width: 80px;
  font-weight: bold;
  flex-shrink: 0;
}

.log-message {
  flex: 1;
}

.log-info .log-level {
  color: #409eff;
}

.log-warning .log-level {
  color: #e6a23c;
}

.log-error .log-level {
  color: #f56c6c;
}

.services-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
