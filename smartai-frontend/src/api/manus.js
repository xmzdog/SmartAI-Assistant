import { request } from '@/utils/http'

// ManusAgent API
export const manusApi = {
  // 执行任务 (POST)
  executeTask(data) {
    return request.post('/v1/manus/execute', data)
  },

  // 快速执行任务 (GET)
  quickExecute(task) {
    return request.get('/v1/manus/execute', { task })
  },

  // 深度推理
  reasoning(data) {
    return request.post('/v1/manus/reasoning', data)
  },

  // 快速推理 (GET)
  quickReasoning(problem, strategy) {
    return request.get('/v1/manus/reasoning', { problem, strategy })
  },

  // 查询任务状态
  getTaskStatus(taskId) {
    return request.get(`/v1/manus/task/${taskId}/status`)
  },

  // 获取任务结果
  getTaskResult(taskId) {
    return request.get(`/v1/manus/task/${taskId}/result`)
  },

  // 取消任务
  cancelTask(taskId) {
    return request.delete(`/v1/manus/task/${taskId}`)
  },

  // 获取Agent状态
  getStatus() {
    return request.get('/v1/manus/status')
  },

  // 获取运行统计
  getStatistics() {
    return request.get('/v1/manus/statistics')
  },

  // 健康检查
  healthCheck() {
    return request.get('/v1/manus/health')
  },

  // 获取推理策略列表
  getReasoningStrategies() {
    return request.get('/v1/manus/reasoning/strategies')
  },

  // 获取工具选择策略
  getToolStrategies() {
    return request.get('/v1/manus/tools/strategies')
  },

  // 配置管理
  updateConfig(data) {
    return request.post('/v1/manus/config', data)
  },

  // 重启Agent
  restart() {
    return request.post('/v1/manus/restart')
  },

  // 清理过期结果
  cleanup() {
    return request.delete('/v1/manus/cleanup')
  },

  // 获取任务历史列表
  getTaskHistory(params = {}) {
    return request.get('/v1/manus/tasks', { params })
  },

  // 获取任务详情
  getTaskDetail(taskId) {
    return request.get(`/v1/manus/task/${taskId}`)
  }
}