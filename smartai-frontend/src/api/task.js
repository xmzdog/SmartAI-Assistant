import { request } from '@/utils/http'

// 任务调度API
export const taskApi = {
  // 查询任务调度列表
  queryTaskScheduleList(params = {}) {
    return request.post('/v1/ai/admin/agent/task/queryTaskScheduleList', params)
  },

  // 根据ID查询任务调度
  queryTaskScheduleById(id) {
    return request.get('/v1/ai/admin/agent/task/queryTaskScheduleById', { params: { id } })
  },

  // 新增任务调度
  addTaskSchedule(task) {
    return request.post('/v1/ai/admin/agent/task/addTaskSchedule', task)
  },

  // 更新任务调度
  updateTaskSchedule(task) {
    return request.post('/v1/ai/admin/agent/task/updateTaskSchedule', task)
  },

  // 删除任务调度
  deleteTaskSchedule(id) {
    return request.get('/v1/ai/admin/agent/task/deleteTaskSchedule', { params: { id } })
  }
}
