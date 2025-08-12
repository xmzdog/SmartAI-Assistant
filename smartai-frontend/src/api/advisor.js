import { MockApiService } from './base'

// 模拟数据 - 顾问配置表
const mockAdvisors = [
  {
    id: 1,
    advisorName: '记忆',
    advisorType: 'ChatMemory',
    orderNum: 1,
    extParam: '{\n    "maxMessages": 200\n}',
    status: 1,
    createTime: '2025-05-04 08:23:25',
    updateTime: '2025-05-05 09:01:58'
  },
  {
    id: 2,
    advisorName: '知识库',
    advisorType: 'RagAnswer',
    orderNum: 1,
    extParam: '{\n    "topK": "4",\n    "filterExpression": "knowledge == \'知识库名称\'"\n}',
    status: 1,
    createTime: '2025-05-04 08:23:25',
    updateTime: '2025-05-04 08:58:31'
  },
  {
    id: 3,
    advisorName: '日志记录',
    advisorType: 'SimpleLoggerAdvisor',
    orderNum: 2,
    extParam: '{\n    "logLevel": "INFO",\n    "includeTimestamp": true\n}',
    status: 1,
    createTime: '2025-05-04 08:23:25',
    updateTime: '2025-05-04 08:58:31'
  },
  {
    id: 4,
    advisorName: '安全过滤',
    advisorType: 'SecurityFilterAdvisor',
    orderNum: 0,
    extParam: '{\n    "enableContentFilter": true,\n    "sensitiveWords": ["敏感词1", "敏感词2"]\n}',
    status: 1,
    createTime: '2025-05-04 08:23:25',
    updateTime: '2025-05-04 08:58:31'
  }
]

// 创建模拟API服务
const mockApiService = new MockApiService('/api/v1/advisors', mockAdvisors)

// 顾问配置相关接口
export const advisorApi = {
  // 获取顾问列表
  getAdvisorList: (params) => {
    return mockApiService.getList(params)
  },

  // 根据ID获取顾问详情
  getAdvisorById: (id) => {
    return mockApiService.getById(id)
  },

  // 创建顾问
  createAdvisor: (data) => {
    return mockApiService.create(data)
  },

  // 更新顾问
  updateAdvisor: (id, data) => {
    return mockApiService.update(id, data)
  },

  // 删除顾问
  deleteAdvisor: (id) => {
    return mockApiService.delete(id)
  },

  // 批量删除顾问
  batchDeleteAdvisors: (ids) => {
    return mockApiService.batchDelete(ids)
  },

  // 更新顾问状态
  updateAdvisorStatus: (id, status) => {
    return mockApiService.updateStatus(id, status)
  },

  // 批量更新顾问状态
  batchUpdateAdvisorStatus: (ids, status) => {
    return mockApiService.batchUpdateStatus(ids, status)
  },

  // 获取顾问类型列表
  getAdvisorTypes: () => {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: [
        { value: 'ChatMemory', label: '对话记忆', description: '保存对话历史，提供上下文记忆能力' },
        { value: 'RagAnswer', label: '知识库问答', description: '基于知识库进行检索增强生成' },
        { value: 'SimpleLoggerAdvisor', label: '日志记录', description: '记录对话和操作日志' },
        { value: 'SecurityFilterAdvisor', label: '安全过滤', description: '过滤敏感内容和不当信息' },
        { value: 'RetryAdvisor', label: '重试机制', description: '自动重试失败的请求' },
        { value: 'CacheAdvisor', label: '缓存管理', description: '缓存常用查询结果' }
      ]
    })
  }
}
