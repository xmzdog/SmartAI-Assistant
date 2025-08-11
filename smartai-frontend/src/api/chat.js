import { request } from '@/utils/http'

// 对话API
export const chatApi = {
  // 基于知识库的对话
  chatWithKnowledgeBase(data) {
    return request.post('/v1/chat/knowledge-base', data)
  },

  // 获取知识库列表
  getKnowledgeBases() {
    return request.get('/v1/knowledge-bases')
  },

  // 获取知识库详情
  getKnowledgeBaseDetail(id) {
    return request.get(`/v1/knowledge-bases/${id}`)
  },

  // 创建知识库
  createKnowledgeBase(data) {
    return request.post('/v1/knowledge-bases', data)
  },

  // 更新知识库
  updateKnowledgeBase(id, data) {
    return request.put(`/v1/knowledge-bases/${id}`, data)
  },

  // 删除知识库
  deleteKnowledgeBase(id) {
    return request.delete(`/v1/knowledge-bases/${id}`)
  },

  // 上传文档到知识库
  uploadDocument(knowledgeBaseId, formData) {
    return request.post(`/v1/knowledge-bases/${knowledgeBaseId}/documents`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除知识库文档
  deleteDocument(knowledgeBaseId, documentId) {
    return request.delete(`/v1/knowledge-bases/${knowledgeBaseId}/documents/${documentId}`)
  },

  // 获取知识库文档列表
  getDocuments(knowledgeBaseId, params = {}) {
    return request.get(`/v1/knowledge-bases/${knowledgeBaseId}/documents`, { params })
  },

  // 搜索知识库内容
  searchKnowledgeBase(knowledgeBaseId, query, params = {}) {
    return request.post(`/v1/knowledge-bases/${knowledgeBaseId}/search`, {
      query,
      ...params
    })
  },

  // 获取对话历史
  getChatHistory(knowledgeBaseId, params = {}) {
    return request.get(`/v1/chat/history/${knowledgeBaseId}`, { params })
  },

  // 清空对话历史
  clearChatHistory(knowledgeBaseId) {
    return request.delete(`/v1/chat/history/${knowledgeBaseId}`)
  },

  // 导出对话历史
  exportChatHistory(knowledgeBaseId, format = 'txt') {
    return request.get(`/v1/chat/history/${knowledgeBaseId}/export`, {
      params: { format },
      responseType: 'blob'
    })
  },

  // 获取推荐问题
  getSuggestedQuestions(knowledgeBaseId) {
    return request.get(`/v1/knowledge-bases/${knowledgeBaseId}/suggested-questions`)
  },

  // 评价回答质量
  rateAnswer(data) {
    return request.post('/v1/chat/rate', data)
  },

  // 重新生成回答
  regenerateAnswer(data) {
    return request.post('/v1/chat/regenerate', data)
  }
}

// 导出便捷方法
export const {
  chatWithKnowledgeBase,
  getKnowledgeBases,
  getKnowledgeBaseDetail,
  createKnowledgeBase,
  updateKnowledgeBase,
  deleteKnowledgeBase,
  uploadDocument,
  deleteDocument,
  getDocuments,
  searchKnowledgeBase,
  getChatHistory,
  clearChatHistory,
  exportChatHistory,
  getSuggestedQuestions,
  rateAnswer,
  regenerateAnswer
} = chatApi

export default chatApi