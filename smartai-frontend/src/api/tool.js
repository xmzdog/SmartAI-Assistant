import { MockApiService } from './base'

// 模拟数据 - MCP客户端配置表
const mockTools = [
  {
    id: 1,
    mcpName: 'CSDN自动发帖',
    transportType: 'sse',
    transportConfig: '{\n\t"baseUri":"http://mcp-server-csdn-app:8101"\n}',
    requestTimeout: 180,
    status: 1,
    createTime: '2025-05-02 18:43:28',
    updateTime: '2025-05-05 12:07:52'
  },
  {
    id: 2,
    mcpName: '微信公众号消息通知',
    transportType: 'sse',
    transportConfig: '{\n\t"baseUri":"http://mcp-server-weixin-app:8102"\n}',
    requestTimeout: 180,
    status: 1,
    createTime: '2025-05-02 18:43:28',
    updateTime: '2025-05-05 12:07:57'
  },
  {
    id: 3,
    mcpName: 'filesystem',
    transportType: 'stdio',
    transportConfig: '{\n    "filesystem": {\n        "command": "npx",\n        "args": [\n            "-y",\n            "@modelcontextprotocol/server-filesystem",\n            "/Users/fuzhengwei/Desktop",\n            "/Users/fuzhengwei/Desktop"\n        ]\n    }\n}',
    requestTimeout: 180,
    status: 1,
    createTime: '2025-05-05 13:14:42',
    updateTime: '2025-05-05 13:27:46'
  },
  {
    id: 4,
    mcpName: 'g-search',
    transportType: 'stdio',
    transportConfig: '{\n    "g-search": {\n        "command": "npx",\n        "args": [\n            "-y",\n            "g-search-mcp"\n        ]\n    }\n}',
    requestTimeout: 180,
    status: 1,
    createTime: '2025-05-05 13:14:42',
    updateTime: '2025-05-10 08:23:58'
  }
]

// 创建模拟API服务
const mockApiService = new MockApiService('/api/v1/tools', mockTools)

// MCP工具相关接口
export const toolApi = {
  // 获取工具列表
  getToolList: (params) => {
    return mockApiService.getList(params)
  },

  // 根据ID获取工具详情
  getToolById: (id) => {
    return mockApiService.getById(id)
  },

  // 创建工具
  createTool: (data) => {
    return mockApiService.create(data)
  },

  // 更新工具
  updateTool: (id, data) => {
    return mockApiService.update(id, data)
  },

  // 删除工具
  deleteTool: (id) => {
    return mockApiService.delete(id)
  },

  // 批量删除工具
  batchDeleteTools: (ids) => {
    return mockApiService.batchDelete(ids)
  },

  // 更新工具状态
  updateToolStatus: (id, status) => {
    return mockApiService.updateStatus(id, status)
  },

  // 批量更新工具状态
  batchUpdateToolStatus: (ids, status) => {
    return mockApiService.batchUpdateStatus(ids, status)
  },

  // 测试工具连接
  testToolConnection: async (id) => {
    // 模拟测试连接
    await new Promise(resolve => setTimeout(resolve, 800))
    return {
      code: 200,
      message: '工具连接测试成功',
      data: {
        status: 'success',
        capabilities: ['read', 'write', 'execute'],
        timestamp: new Date().toISOString()
      }
    }
  }
}
