import { MockApiService } from './base'

// 模拟数据 - 系统提示词配置表
const mockPrompts = [
  {
    id: 1,
    promptName: '提示词优化',
    promptContent: '你是一个专业的AI提示词优化专家。请帮我优化以下prompt，并按照以下格式返回：\n\n# Role: [角色名称]\n\n## Profile\n\n- language: [语言]\n- description: [详细的角色描述]\n- background: [角色背景]\n- personality: [性格特征]\n- expertise: [专业领域]\n- target_audience: [目标用户群]\n\n## Skills\n\n1. [核心技能类别]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n2. [辅助技能类别]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n   - [具体技能]: [简要说明]\n\n## Rules\n\n1. [基本原则]：\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n2. [行为准则]：\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n   - [具体规则]: [详细说明]\n3. [限制条件]：\n   - [具体限制]: [详细说明]\n   - [具体限制]: [详细说明]\n   - [具体限制]: [详细说明]\n   - [具体限制]: [详细说明]\n\n## Workflows\n\n- 目标: [明确目标]\n- 步骤 1: [详细说明]\n- 步骤 2: [详细说明]\n- 步骤 3: [详细说明]\n- 预期结果: [说明]\n\n## Initialization\n\n作为[角色名称]，你必须遵守上述Rules，按照Workflows执行任务。\n请基于以上模板，优化并扩展以下prompt，确保内容专业、完整且结构清晰，注意不要携带任何引导词或解释，不要使用代码块包围。',
    description: '提示词优化，拆分执行动作',
    status: 1,
    createTime: '2025-05-04 21:14:24',
    updateTime: '2025-05-05 10:04:25'
  },
  {
    id: 2,
    promptName: '发帖和消息通知介绍',
    promptContent: '你是一个 AI Agent 智能体，可以根据用户输入信息生成文章，并发送到 CSDN 平台以及完成微信公众号消息通知，今天是 {current_date}。\n\n你擅长使用Planning模式，帮助用户生成质量更高的文章。\n\n你的规划应该包括以下几个方面：\n1. 分析用户输入的内容，生成技术文章。\n2. 提取，文章标题（需要含带技术点）、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）将以上内容发布文章到CSDN\n3. 获取发送到 CSDN 文章的 URL 地址。\n4. 微信公众号消息通知，平台：CSDN、主题：为文章标题、描述：为文章简述、跳转地址：为发布文章到CSDN获取 URL地址 CSDN文章链接 https 开头的地址。',
    description: '提示词优化，拆分执行动作',
    status: 1,
    createTime: '2025-05-04 21:14:24',
    updateTime: '2025-05-05 10:10:42'
  },
  {
    id: 3,
    promptName: 'CSDN发布文章',
    promptContent: '我需要你帮我生成一篇文章，要求如下；\n                                \n                1. 场景为互联网大厂java求职者面试\n                2. 面试管提问 Java 核心知识、JUC、JVM、多线程、线程池、HashMap、ArrayList、Spring、SpringBoot、MyBatis、Dubbo、RabbitMQ、xxl-job、Redis、MySQL、Linux、Docker、设计模式、DDD等不限于此的各项技术问题。\n                3. 按照故事场景，以严肃的面试官和搞笑的水货程序员谢飞机进行提问，谢飞机对简单问题可以回答，回答好了面试官还会夸赞。复杂问题胡乱回答，回答的不清晰。\n                4. 每次进行3轮提问，每轮可以有3-5个问题。这些问题要有技术业务场景上的衔接性，循序渐进引导提问。最后是面试官让程序员回家等通知类似的话术。\n                5. 提问后把问题的答案，写到文章最后，最后的答案要详细讲述出技术点，让小白可以学习下来。\n                                \n                根据以上内容，不要阐述其他信息，请直接提供；文章标题、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）\n                                \n                将以上内容发布文章到CSDN。',
    description: 'CSDN发布文章',
    status: 1,
    createTime: '2025-05-07 12:05:36',
    updateTime: '2025-05-07 12:05:36'
  },
  {
    id: 4,
    promptName: '文章操作测试',
    promptContent: '在 /Users/fuzhengwei/Desktop 创建文件 file01.txt',
    description: '文件操作测试',
    status: 1,
    createTime: '2025-05-07 12:06:03',
    updateTime: '2025-05-07 12:06:08'
  }
]

// 创建模拟API服务
const mockApiService = new MockApiService('/api/v1/prompts', mockPrompts)

// 系统提示相关接口
export const promptApi = {
  // 获取提示列表
  getPromptList: (params) => {
    return mockApiService.getList(params)
  },

  // 根据ID获取提示详情
  getPromptById: (id) => {
    return mockApiService.getById(id)
  },

  // 创建提示
  createPrompt: (data) => {
    return mockApiService.create(data)
  },

  // 更新提示
  updatePrompt: (id, data) => {
    return mockApiService.update(id, data)
  },

  // 删除提示
  deletePrompt: (id) => {
    return mockApiService.delete(id)
  },

  // 批量删除提示
  batchDeletePrompts: (ids) => {
    return mockApiService.batchDelete(ids)
  },

  // 更新提示状态
  updatePromptStatus: (id, status) => {
    return mockApiService.updateStatus(id, status)
  },

  // 批量更新提示状态
  batchUpdatePromptStatus: (ids, status) => {
    return mockApiService.batchUpdateStatus(ids, status)
  },

  // 测试提示词
  testPrompt: async (data) => {
    // 模拟测试提示词
    await new Promise(resolve => setTimeout(resolve, 1200))
    return {
      code: 200,
      message: '提示词测试成功',
      data: {
        result: '这是一个测试响应，展示提示词的效果...',
        tokens: Math.floor(Math.random() * 1000) + 100,
        timestamp: new Date().toISOString()
      }
    }
  }
}
