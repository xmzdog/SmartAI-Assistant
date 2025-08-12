import { request } from '@/utils/http'

// Manus API - 任务管理相关接口
export const manusApi = {
  // 执行任务
  executeTask: (taskData) => {
    return request.post('/api/manus/tasks', taskData)
  },

  // 获取任务状态
  getTaskStatus: (taskId) => {
    return request.get(`/api/manus/tasks/${taskId}/status`)
  },

  // 获取任务结果
  getTaskResult: (taskId) => {
    return request.get(`/api/manus/tasks/${taskId}/result`)
  },

  // 获取任务详情
  getTaskDetail: (taskId) => {
    return request.get(`/api/manus/tasks/${taskId}`)
  },

  // 取消任务
  cancelTask: (taskId) => {
    return request.post(`/api/manus/tasks/${taskId}/cancel`)
  },

  // 获取任务历史
  getTaskHistory: (params = {}) => {
    return request.get('/api/manus/tasks', { params })
  },

  // 获取任务列表
  getTasks: (params = {}) => {
    return request.get('/api/manus/tasks', { params })
  },

  // 删除任务
  deleteTask: (taskId) => {
    return request.delete(`/api/manus/tasks/${taskId}`)
  },

  // 重试任务
  retryTask: (taskId) => {
    return request.post(`/api/manus/tasks/${taskId}/retry`)
  },

  // 获取任务日志
  getTaskLogs: (taskId) => {
    return request.get(`/api/manus/tasks/${taskId}/logs`)
  },

  // 批量操作任务
  batchTasks: (operation, taskIds) => {
    return request.post('/api/manus/tasks/batch', {
      operation,
      taskIds
    })
  },

  // 获取任务统计信息
  getTaskStats: (params = {}) => {
    return request.get('/api/manus/tasks/stats', { params })
  },

  // 获取任务类型列表
  getTaskTypes: () => {
    return request.get('/api/manus/task-types')
  },

  // 创建任务模板
  createTaskTemplate: (templateData) => {
    return request.post('/api/manus/task-templates', templateData)
  },

  // 获取任务模板列表
  getTaskTemplates: (params = {}) => {
    return request.get('/api/manus/task-templates', { params })
  },

  // 使用模板创建任务
  createTaskFromTemplate: (templateId, taskData) => {
    return request.post(`/api/manus/task-templates/${templateId}/create`, taskData)
  },

  // 获取任务队列状态
  getQueueStatus: () => {
    return request.get('/api/manus/queue/status')
  },

  // 暂停任务队列
  pauseQueue: () => {
    return request.post('/api/manus/queue/pause')
  },

  // 恢复任务队列
  resumeQueue: () => {
    return request.post('/api/manus/queue/resume')
  },

  // 清空任务队列
  clearQueue: () => {
    return request.post('/api/manus/queue/clear')
  }
}
