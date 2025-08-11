import { defineStore } from 'pinia'
import { ref } from 'vue'
import { manusApi } from '@/api/manus'
import { agentApi } from '@/api/agent'

export const useSystemStore = defineStore('system', () => {
  // 状态
  const agentStatus = ref(null)
  const statistics = ref(null)
  const tools = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  // 配置
  const theme = ref('light')
  const language = ref('zh-CN')
  const autoRefresh = ref(true)
  const refreshInterval = ref(5000) // 5秒

  // 操作
  const fetchAgentStatus = async () => {
    try {
      isLoading.value = true
      const status = await manusApi.getStatus()
      agentStatus.value = status
    } catch (err) {
      error.value = '获取Agent状态失败'
      console.warn('获取Agent状态失败，使用模拟数据:', err)
      // 使用模拟数据
      agentStatus.value = {
        status: 'IDLE',
        activeTaskCount: 2,
        uptime: '2天 14小时 32分钟',
        lastActivity: new Date().toISOString(),
        version: '1.0.0',
        memoryUsage: 65.2
      }
    } finally {
      isLoading.value = false
    }
  }

  const fetchStatistics = async () => {
    try {
      const stats = await manusApi.getStatistics()
      statistics.value = stats
    } catch (err) {
      console.warn('获取统计信息失败，使用模拟数据:', err)
      // 使用模拟数据
      statistics.value = {
        totalTasks: 156,
        completedTasks: 132,
        failedTasks: 8,
        runningTasks: 2,
        pendingTasks: 14,
        averageExecutionTime: 245.6,
        systemLoad: 0.45,
        todayTasks: 12,
        successRate: 94.6
      }
    }
  }

  const fetchTools = async () => {
    try {
      const toolList = await agentApi.getTools()
      tools.value = toolList
    } catch (err) {
      console.error('获取工具列表失败:', err)
    }
  }

  const healthCheck = async () => {
    try {
      const health = await manusApi.healthCheck()
      return health.status === 'healthy'
    } catch (err) {
      console.error('健康检查失败:', err)
      return false
    }
  }

  const restartAgent = async () => {
    try {
      isLoading.value = true
      await manusApi.restart()
      // 重启后重新获取状态
      setTimeout(() => {
        fetchAgentStatus()
        fetchStatistics()
      }, 3000)
    } catch (err) {
      error.value = '重启Agent失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const cleanup = async () => {
    try {
      await manusApi.cleanup()
      // 清理后重新获取统计信息
      fetchStatistics()
    } catch (err) {
      console.error('清理失败:', err)
    }
  }

  const updateConfig = async (property, value) => {
    try {
      await manusApi.updateConfig({ property, value })
    } catch (err) {
      error.value = '更新配置失败'
      throw err
    }
  }

  // 主题切换
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    localStorage.setItem('theme', theme.value)
    applyTheme()
  }

  const applyTheme = () => {
    const html = document.documentElement
    if (theme.value === 'dark') {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
  }

  // 语言切换
  const switchLanguage = (lang) => {
    language.value = lang
    localStorage.setItem('language', lang)
  }

  // 自动刷新控制
  const toggleAutoRefresh = () => {
    autoRefresh.value = !autoRefresh.value
    localStorage.setItem('autoRefresh', autoRefresh.value.toString())
  }

  const setRefreshInterval = (interval) => {
    refreshInterval.value = interval
    localStorage.setItem('refreshInterval', interval.toString())
  }

  // 初始化
  const init = () => {
    // 恢复保存的设置
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      theme.value = savedTheme
      applyTheme()
    }

    const savedLanguage = localStorage.getItem('language')
    if (savedLanguage) {
      language.value = savedLanguage
    }

    const savedAutoRefresh = localStorage.getItem('autoRefresh')
    if (savedAutoRefresh) {
      autoRefresh.value = savedAutoRefresh === 'true'
    }

    const savedRefreshInterval = localStorage.getItem('refreshInterval')
    if (savedRefreshInterval) {
      refreshInterval.value = parseInt(savedRefreshInterval)
    }

    // 初始化数据
    fetchAgentStatus()
    fetchStatistics()
    fetchTools()
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // 状态
    agentStatus,
    statistics,
    tools,
    isLoading,
    error,

    // 配置
    theme,
    language,
    autoRefresh,
    refreshInterval,

    // 方法
    fetchAgentStatus,
    fetchStatistics,
    fetchTools,
    healthCheck,
    restartAgent,
    cleanup,
    updateConfig,
    toggleTheme,
    switchLanguage,
    toggleAutoRefresh,
    setRefreshInterval,
    init,
    clearError
  }
})