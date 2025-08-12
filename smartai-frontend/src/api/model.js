import { request } from '@/utils/http'

// 模型管理API
export const modelApi = {
  // 查询模型列表
  queryModelList(params = {}) {
    return request.post('/v1/ai/admin/client/model/queryClientModelList', params)
  },

  // 查询所有模型配置
  queryAllModelConfig() {
    return request.post('/v1/ai/admin/client/model/queryAllModelConfig')
  },

  // 根据ID查询模型
  queryModelById(id) {
    return request.get('/v1/ai/admin/client/model/queryClientModelById', { params: { id } })
  },

  // 新增模型
  addModel(model) {
    return request.post('/v1/ai/admin/client/model/addClientModel', model)
  },

  // 更新模型
  updateModel(model) {
    return request.post('/v1/ai/admin/client/model/updateClientModel', model)
  },

  // 删除模型
  deleteModel(id) {
    return request.get('/v1/ai/admin/client/model/deleteClientModel', { params: { id } })
  }
}