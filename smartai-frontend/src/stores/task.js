import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { manusApi } from '@/api/manus'
import { agentApi } from '@/api/agent'

export const useTaskStore = defineStore('task', () => {
  // 状态
  const tasks = ref(new Map())
  const currentTask = ref(null)
  const isLoading = ref(false)
  const error = ref(null)

  // 计算属性
  const taskList = computed(() => Array.from(tasks.value.values()))
  const runningTasks = computed(() => 
    taskList.value.filter(task => task.status === 'RUNNING')
  )
  const completedTasks = computed(() => 
    taskList.value.filter(task => task.status === 'COMPLETED')
  )
  const failedTasks = computed(() => 
    taskList.value.filter(task => task.status === 'FAILED')
  )

  // 操作
  const executeManusTask = async (taskData) => {
    try {
      isLoading.value = true
      error.value = null
      
      const result = await manusApi.executeTask(taskData)
      
      if (result.taskId) {
        tasks.value.set(result.taskId, result)
        currentTask.value = result
        
        // 如果是异步任务，开始轮询状态
        if (taskData.async) {
          pollTaskStatus(result.taskId)
        }
      }
      
      return result
    } catch (err) {
      error.value = err.message || '任务执行失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const executeAgentTask = async (taskData) => {
    try {
      isLoading.value = true
      error.value = null
      
      const result = await agentApi.executeTask(taskData)
      
      if (result.taskId) {
        tasks.value.set(result.taskId, result)
        currentTask.value = result
        
        // 如果是异步任务，开始轮询状态
        if (taskData.async) {
          pollTaskStatus(result.taskId)
        }
      }
      
      return result
    } catch (err) {
      error.value = err.message || '任务执行失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const pollTaskStatus = async (taskId) => {
    const maxAttempts = 300 // 5分钟
    let attempts = 0
    
    const poll = async () => {
      try {
        const status = await manusApi.getTaskStatus(taskId)
        console.log(`轮询任务状态 ${taskId}:`, status)
        
        // 更新任务状态
        const existingTask = tasks.value.get(taskId)
        if (existingTask) {
          existingTask.status = status.status
          existingTask.progress = status.progress
          
          // 更新其他状态信息
          if (status.startTime) existingTask.startTime = status.startTime
          if (status.endTime) existingTask.endTime = status.endTime
          
          tasks.value.set(taskId, existingTask)
        }
        
        // 如果任务完成或失败，停止轮询
        if (status.status === 'COMPLETED' || status.status === 'FAILED' || status.status === 'CANCELLED') {
          console.log(`任务 ${taskId} 已完成，状态: ${status.status}`)
          // 获取最终结果
          try {
            const resultResponse = await manusApi.getTaskResult(taskId)
            console.log(`获取任务 ${taskId} 结果:`, resultResponse)
            
            // 处理结果数据
            if (existingTask) {
              if (resultResponse && resultResponse.data) {
                existingTask.result = resultResponse.data
              }
              tasks.value.set(taskId, existingTask)
            } else {
              // 如果没有现有任务，创建新的任务对象
              const newTask = {
                taskId,
                status: status.status,
                progress: status.progress,
                result: resultResponse?.data,
                startTime: status.startTime,
                endTime: status.endTime
              }
              tasks.value.set(taskId, newTask)
            }
          } catch (resultErr) {
            console.error('获取任务结果失败:', resultErr)
            // 如果获取结果失败，尝试获取任务详情
            try {
              const detailResponse = await manusApi.getTaskDetail(taskId)
              if (detailResponse && detailResponse.data) {
                if (existingTask) {
                  Object.assign(existingTask, detailResponse.data)
                  tasks.value.set(taskId, existingTask)
                } else {
                  tasks.value.set(taskId, detailResponse.data)
                }
              }
            } catch (detailErr) {
              console.error('获取任务详情失败:', detailErr)
            }
          }
          
          console.log(`任务 ${taskId} 轮询结束`)
          return // 停止轮询
        }
        
        // 继续轮询
        attempts++
        if (attempts < maxAttempts) {
          setTimeout(poll, 1000) // 每秒轮询一次
        } else {
          console.log(`任务 ${taskId} 轮询超时，停止轮询`)
        }
      } catch (err) {
        console.error('轮询任务状态失败:', err)
        attempts++
        if (attempts < maxAttempts) {
          setTimeout(poll, 2000) // 出错时延长轮询间隔
        } else {
          console.log(`任务 ${taskId} 轮询出错次数过多，停止轮询`)
        }
      }
    }
    
    poll()
  }

  const getTaskById = (taskId) => {
    return tasks.value.get(taskId)
  }

  const updateTaskStatus = async (taskId) => {
    try {
      const status = await manusApi.getTaskStatus(taskId)
      const existingTask = tasks.value.get(taskId)
      
      if (existingTask) {
        existingTask.status = status.status
        existingTask.progress = status.progress
        
        // 如果任务已完成，获取完整结果
        if (status.status === 'COMPLETED') {
          try {
            const resultResponse = await manusApi.getTaskResult(taskId)
            // 处理结果数据 - getTaskResult返回的是字符串
            if (resultResponse && resultResponse.data) {
              existingTask.result = resultResponse.data
            }
          } catch (resultErr) {
            console.error('获取任务结果失败:', resultErr)
            // 如果getTaskResult失败，尝试getTaskDetail
            try {
              const detailResponse = await manusApi.getTaskDetail(taskId)
              if (detailResponse && detailResponse.data) {
                Object.assign(existingTask, detailResponse.data)
              }
            } catch (detailErr) {
              console.error('获取任务详情失败:', detailErr)
            }
          }
        }
        
        tasks.value.set(taskId, existingTask)
      }
      
      return status
    } catch (err) {
      console.error('更新任务状态失败:', err)
    }
  }

  const cancelTask = async (taskId) => {
    try {
      await manusApi.cancelTask(taskId)
      
      const existingTask = tasks.value.get(taskId)
      if (existingTask) {
        existingTask.status = 'CANCELLED'
        tasks.value.set(taskId, existingTask)
      }
    } catch (err) {
      error.value = '取消任务失败'
      throw err
    }
  }

  const clearTasks = () => {
    tasks.value.clear()
    currentTask.value = null
    error.value = null
  }

  const clearError = () => {
    error.value = null
  }

  // 更新任务数据
  const updateTask = (taskId, taskData) => {
    const existingTask = tasks.value.get(taskId)
    if (existingTask) {
      const updatedTask = { ...existingTask, ...taskData }
      tasks.value.set(taskId, updatedTask)
    }
  }

  // 添加任务到store
  const addTask = (task) => {
    if (task && task.taskId) {
      tasks.value.set(task.taskId, task)
    }
  }

  // 获取任务历史
  const fetchTaskHistory = async (limit = 10) => {
    try {
      isLoading.value = true
      const result = await manusApi.getTaskHistory({ limit, sortBy: 'startTime', order: 'desc' })
      
      if (result && result.tasks && Array.isArray(result.tasks)) {
        // 清空现有任务并添加新的任务
        tasks.value.clear()
        result.tasks.forEach(task => {
          tasks.value.set(task.taskId, task)
        })
      }
    } catch (err) {
      // 如果后端API不可用，使用模拟数据
      console.warn('获取任务历史失败，使用模拟数据:', err)
      loadMockTasks()
    } finally {
      isLoading.value = false
    }
  }

  // 加载模拟任务数据
  const loadMockTasks = () => {
    const mockTasks = [
      {
        taskId: 'task-001',
        status: 'COMPLETED',
        startTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(Date.now() - 1.5 * 60 * 60 * 1000).toISOString(),
        result: {
          title: '知识库问答任务',
          summary: '成功完成产品相关问题的问答'
        },
        progress: 100
      },
      {
        taskId: 'task-002',
        status: 'RUNNING',
        startTime: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
        result: {
          title: '会议分析任务',
          summary: '正在分析最近的会议内容'
        },
        progress: 65
      },
      {
        taskId: 'task-003',
        status: 'FAILED',
        startTime: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(Date.now() - 3.8 * 60 * 60 * 1000).toISOString(),
        result: {
          title: '报告生成任务',
          summary: '生成PDF报告失败，模板文件不存在',
          error: '模板文件路径错误'
        },
        progress: 45
      },
      {
        taskId: 'task-004',
        status: 'COMPLETED',
        startTime: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(Date.now() - 5.5 * 60 * 60 * 1000).toISOString(),
        result: {
          title: '深度推理任务',
          summary: '完成业务策略分析推理'
        },
        progress: 100
      },
      {
        taskId: 'task-005',
        status: 'CANCELLED',
        startTime: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(Date.now() - 7.8 * 60 * 60 * 1000).toISOString(),
        result: {
          title: '数据分析任务',
          summary: '用户取消了任务执行'
        },
        progress: 25
      }
    ]
    
    tasks.value.clear()
    mockTasks.forEach(task => {
      tasks.value.set(task.taskId, task)
    })
  }

  return {
    // 状态
    tasks,
    currentTask,
    isLoading,
    error,
    
    // 计算属性
    taskList,
    runningTasks,
    completedTasks,
    failedTasks,
    
    // 方法
    executeManusTask,
    executeAgentTask,
    getTaskById,
    updateTask,
    addTask,
    updateTaskStatus,
    cancelTask,
    clearTasks,
    clearError,
    pollTaskStatus,
    fetchTaskHistory,
    loadMockTasks
  }
})