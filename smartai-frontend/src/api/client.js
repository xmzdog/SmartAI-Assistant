import { MockApiService } from './base'

// 模拟数据 - AI客户端配置表
const mockClients = [
  {
    id: 1,
    clientName: '提示词优化',
    description: '提示词优化，分为角色、动作、规则、目标等。',
    status: 1,
    createTime: '2025-05-04 19:47:56',
    updateTime: '2025-05-05 10:02:55'
  },
  {
    id: 2,
    clientName: '自动发帖和通知',
    description: '自动生成CSDN文章，发送微信公众号消息通知',
    status: 1,
    createTime: '2025-05-05 10:05:43',
    updateTime: '2025-05-05 10:09:20'
  },
  {
    id: 3,
    clientName: '文件操作服务',
    description: '文件操作服务',
    status: 1,
    createTime: '2025-05-05 13:15:57',
    updateTime: '2025-05-05 13:16:03'
  },
  {
    id: 4,
    clientName: '流式对话客户端',
    description: '流式对话客户端',
    status: 1,
    createTime: '2025-05-07 09:21:04',
    updateTime: '2025-05-07 09:21:04'
  }
]

// 创建模拟API服务
const mockApiService = new MockApiService('/api/v1/clients', mockClients)

// AI客户端相关接口
export const clientApi = {
  // 获取客户端列表
  getClientList: (params) => {
    return mockApiService.getList(params)
  },

  // 根据ID获取客户端详情
  getClientById: (id) => {
    return mockApiService.getById(id)
  },

  // 创建客户端
  createClient: (data) => {
    return mockApiService.create(data)
  },

  // 更新客户端
  updateClient: (id, data) => {
    return mockApiService.update(id, data)
  },

  // 删除客户端
  deleteClient: (id) => {
    return mockApiService.delete(id)
  },

  // 批量删除客户端
  batchDeleteClients: (ids) => {
    return mockApiService.batchDelete(ids)
  },

  // 更新客户端状态
  updateClientStatus: (id, status) => {
    return mockApiService.updateStatus(id, status)
  },

  // 批量更新客户端状态
  batchUpdateClientStatus: (ids, status) => {
    return mockApiService.batchUpdateStatus(ids, status)
  }
}
