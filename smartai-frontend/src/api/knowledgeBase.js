import { request } from '@/utils/http'

// 知识库API
export const knowledgeBaseApi = {
  
  /**
   * 获取知识库列表
   * @returns {Promise} 知识库列表
   */
  getKnowledgeBaseList() {
    return request.get('/v1/rag/query_rag_tag_list')
  },

  /**
   * 上传文件到知识库
   * @param {FormData} formData - 包含ragTag和file的表单数据
   * @returns {Promise} 上传结果
   */
  uploadFiles(formData) {
    return request.post('/v1/rag/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除知识库
   * @param {string} ragTag - 知识库标签
   * @returns {Promise} 删除结果
   */
  deleteKnowledgeBase(ragTag) {
    return request.delete(`/v1/rag/knowledge-base/${ragTag}`)
  },

  /**
   * 获取知识库详情
   * @param {string} ragTag - 知识库标签
   * @returns {Promise} 知识库详情
   */
  getKnowledgeBaseDetail(ragTag) {
    return request.get(`/v1/rag/knowledge-base/${ragTag}`)
  },

  /**
   * 获取知识库文档列表
   * @param {string} ragTag - 知识库标签
   * @returns {Promise} 文档列表
   */
  getDocumentList(ragTag) {
    return request.get(`/v1/rag/knowledge-base/${ragTag}/documents`)
  },

  /**
   * 删除知识库中的文档
   * @param {string} ragTag - 知识库标签
   * @param {string} documentId - 文档ID
   * @returns {Promise} 删除结果
   */
  deleteDocument(ragTag, documentId) {
    return request.delete(`/v1/rag/knowledge-base/${ragTag}/documents/${documentId}`)
  },

  /**
   * 基于知识库进行问答
   * @param {string} ragTag - 知识库标签
   * @param {string} question - 问题
   * @returns {Promise} 回答结果
   */
  askQuestion(ragTag, question) {
    return request.post('/v1/rag/ask', {
      ragTag,
      question
    })
  }
}
