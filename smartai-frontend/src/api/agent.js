import { request } from '@/utils/http'

// Agent API
export const agentApi = {
  // 执行复杂任务
  executeTask(data) {
    return request.post('/v1/agent/execute', data)
  },

  // 查询任务状态
  getTaskStatus(taskId) {
    return request.get(`/v1/agent/status/${taskId}`)
  },

  // 停止任务
  stopTask(taskId) {
    return request.delete(`/v1/agent/stop/${taskId}`)
  },

  // 获取可用工具列表
  getTools() {
    return request.get('/v1/agent/tools')
  },

  // 快速知识库问答
  quickKnowledgeQA(data) {
    return request.post('/v1/agent/quick/knowledge-qa', data)
  },

  // 快速报告生成
  quickGenerateReport(data) {
    return request.post('/v1/agent/quick/generate-report', data)
  },

  // 快速会议分析
  quickMeetingAnalysis(data) {
    return request.post('/v1/agent/quick/meeting-analysis', data)
  },

  // 获取任务历史列表
  getTaskHistory(params = {}) {
    return request.get('/v1/agent/tasks', { params })
  },

  // 获取任务详情
  getTaskDetail(taskId) {
    return request.get(`/v1/agent/task/${taskId}`)
  }
}