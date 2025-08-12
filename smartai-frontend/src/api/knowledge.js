import { request } from '@/utils/http'

// 知识库管理API
export const knowledgeApi = {
  // 查询RAG订单列表
  queryRagOrderList(params = {}) {
    return request.post('/v1/ai/admin/rag/queryRagOrderList', params)
  },

  // 查询所有有效的RAG订单
  queryAllValidRagOrder() {
    return request.post('/v1/ai/admin/rag/queryAllValidRagOrder')
  },

  // 根据ID查询RAG订单
  queryRagOrderById(id) {
    return request.get('/v1/ai/admin/rag/queryRagOrderById', { params: { id } })
  },

  // 新增RAG订单
  addRagOrder(ragOrder) {
    return request.post('/v1/ai/admin/rag/addRagOrder', ragOrder)
  },

  // 更新RAG订单
  updateRagOrder(ragOrder) {
    return request.post('/v1/ai/admin/rag/updateRagOrder', ragOrder)
  },

  // 删除RAG订单
  deleteRagOrder(id) {
    return request.get('/v1/ai/admin/rag/deleteRagOrder', { params: { id } })
  }
}
