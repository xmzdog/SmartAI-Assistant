import { request } from '@/utils/http'

// 系统提示词API
export const systemPromptApi = {
  // 查询系统提示词列表
  querySystemPromptList(params = {}) {
    return request.post('/v1/ai/admin/client/system/prompt/querySystemPromptList', params)
  },

  // 查询所有系统提示词配置
  queryAllSystemPromptConfig() {
    return request.post('/v1/ai/admin/client/system/prompt/queryAllSystemPromptConfig')
  },

  // 根据ID查询系统提示词
  querySystemPromptById(id) {
    return request.get('/v1/ai/admin/client/system/prompt/querySystemPromptById', { params: { id } })
  },

  // 新增系统提示词
  addSystemPrompt(prompt) {
    return request.post('/v1/ai/admin/client/system/prompt/addSystemPrompt', prompt)
  },

  // 更新系统提示词
  updateSystemPrompt(prompt) {
    return request.post('/v1/ai/admin/client/system/prompt/updateSystemPrompt', prompt)
  },

  // 删除系统提示词
  deleteSystemPrompt(id) {
    return request.get('/v1/ai/admin/client/system/prompt/deleteSystemPrompt', { params: { id } })
  }
}

// 顾问配置API
export const advisorApi = {
  // 查询顾问列表
  queryAdvisorList(params = {}) {
    return request.post('/v1/ai/admin/client/advisor/queryClientAdvisorList', params)
  },

  // 根据ID查询顾问
  queryAdvisorById(id) {
    return request.get('/v1/ai/admin/client/advisor/queryClientAdvisorById', { params: { id } })
  },

  // 新增顾问
  addAdvisor(advisor) {
    return request.post('/v1/ai/admin/client/advisor/addClientAdvisor', advisor)
  },

  // 更新顾问
  updateAdvisor(advisor) {
    return request.post('/v1/ai/admin/client/advisor/updateClientAdvisor', advisor)
  },

  // 删除顾问
  deleteAdvisor(id) {
    return request.get('/v1/ai/admin/client/advisor/deleteClientAdvisor', { params: { id } })
  }
}

// MCP工具API
export const mcpToolApi = {
  // 查询MCP工具列表
  queryMcpList(params = {}) {
    return request.post('/v1/ai/admin/client/tool/mcp/queryMcpList', params)
  },

  // 根据ID查询MCP工具
  queryMcpById(id) {
    return request.get('/v1/ai/admin/client/tool/mcp/queryMcpById', { params: { id } })
  },

  // 新增MCP工具
  addMcp(mcp) {
    return request.post('/v1/ai/admin/client/tool/mcp/addMcp', mcp)
  },

  // 更新MCP工具
  updateMcp(mcp) {
    return request.post('/v1/ai/admin/client/tool/mcp/updateMcp', mcp)
  },

  // 删除MCP工具
  deleteMcp(id) {
    return request.get('/v1/ai/admin/client/tool/mcp/deleteMcp', { params: { id } })
  }
}
