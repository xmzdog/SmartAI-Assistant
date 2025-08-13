import { request } from '@/utils/http'

// 智能体核心API
export const agentApi = {
 // 预热智能体
  preheat(aiAgentId) {
    return request.get('/v1/ai/agent/preheat', { params: { aiAgentId } })
  },

 // 智能体对话
  chatAgent(aiAgentId, message) {
    return request.get('/v1/ai/agent/chat_agent', { params: { aiAgentId, message } })
  },

 // 流式对话
  chatStream(aiAgentId, ragId, message) {
    return request.get('/v1/ai/agent/chat_stream', { params: { aiAgentId, ragId, message } })
  },

 // 智能体对话（新接口）
  chatWithAgent(params) {
    return request.post('/v1/ai/agent/chat', params)
  },

 // 流式智能体对话
  chatWithAgentStream(params) {
    return request.post('/v1/ai/agent/chat_stream', params)
  },

 // AutoAgent执行接口
  autoAgent(params) {
    return request.post('/v1/ai/agent/auto_agent', params)
  },

 // AutoAgent流式执行接口 (使用fetch处理SSE)
  autoAgentStream(params, onMessage, onError, onComplete) {
    console.log('=== 开始AutoAgent流式请求 ===', params)
    return new Promise((resolve, reject) => {
      fetch('/api/v1/ai/agent/auto_agent', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
         // 添加认证头
          'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
        },
        body: JSON.stringify(params)
      })
      .then(response => {
        console.log('=== 收到HTTP响应 ===', response.status, response.headers.get('content-type'))
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        function readStream() {
          return reader.read().then(({ done, value }) => {
            if (done) {
              console.log('SSE流结束')
              onComplete && onComplete()
              resolve()
              return
            }

            buffer += decoder.decode(value, { stream: true })
            console.log('收到SSE原始数据:', buffer)
            
            const lines = buffer.split('\n')
            buffer = lines.pop()// 保留不完整的行

            lines.forEach(line => {
              if (line.trim()) {
                console.log('处理SSE行:', line)
                try {
                 // 处理SSE格式: data: {...}
                  if (line.startsWith('data: ')) {
                    const jsonStr = line.slice(6)// 移除 "data: "
                    if (jsonStr.trim()) {
                      const data = JSON.parse(jsonStr)
                      console.log('解析到SSE数据:', data)
                      onMessage && onMessage(data)
                    }
                  } else {
                   // 处理非JSON格式的消息
                    console.log('处理非JSON消息:', line)
                    onMessage && onMessage({ content: line })
                  }
                } catch (error) {
                  console.error('解析AutoAgent响应失败:', error, '原始行:', line)
                  onMessage && onMessage({ content: line })
                }
              }
            })

            return readStream()
          })
        }

        return readStream()
      })
      .catch(error => {
        console.error('AutoAgent请求失败:', error)
        onError && onError(error)
        reject(error)
      })
    })
  },

 // 获取智能体列表
  getAgentList(params = {}) {
    return request.post('/v1/ai/admin/agent/queryAiAgentList', params)
  },

 // 获取提示词模板列表
  getPromptTemplates() {
    return request.get('/v1/ai/prompt/templates')
  },

 // 上传知识库文件
  uploadRagFile(name, tag, files) {
    const formData = new FormData()
    formData.append('name', name)
    formData.append('tag', tag)
    files.forEach(file => {
      formData.append('files', file)
    })
    return request.post('/v1/ai/agent/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

 // 解析Git仓库
  parseGitRepo(repoUrl, branch = 'main') {
    return request.post('/v1/ai/agent/parse_git', {
      repoUrl,
      branch
    })
  },

 // 构建本地仓库
  buildLocalRepo(files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    return request.post('/v1/ai/agent/build_local', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

 // 获取聊天历史
  getChatHistory(agentId, ragId) {
    return request.get('/v1/ai/agent/chat/history', {
      params: { agentId, ragId }
    })
  },

 // 保存聊天记录
  saveChatRecord(chatData) {
    return request.post('/v1/ai/agent/chat/save', chatData)
  },

 // 删除聊天记录
  deleteChatRecord(chatId) {
    return request.delete(`/v1/ai/agent/chat/${chatId}`)
  }
}

// 智能体管理API
export const agentManageApi = {
 // 查询智能体列表
  queryAgentList(params = {}) {
    return request.post('/v1/ai/admin/agent/queryAiAgentList', params)
  },

 // 根据渠道查询智能体
  queryAgentByChannel(channel) {
    return request.post('/v1/ai/admin/agent/queryAllAgentConfigListByChannel', null, { params: { channel } })
  },

 // 根据ID查询智能体
  queryAgentById(id) {
    return request.get('/v1/ai/admin/agent/queryAiAgentById', { params: { id } })
  },

 // 新增智能体
  addAgent(agent) {
    return request.post('/v1/ai/admin/agent/addAiAgent', agent)
  },

 // 更新智能体
  updateAgent(agent) {
    return request.post('/v1/ai/admin/agent/updateAiAgent', agent)
  },

 // 删除智能体
  deleteAgent(id) {
    return request.get('/v1/ai/admin/agent/deleteAiAgent', { params: { id } })
  }
}

// 客户端管理API
export const clientApi = {
 // 查询客户端列表
  queryClientList(params = {}) {
    return request.post('/v1/ai/admin/client/queryClientList', params)
  },

 // 根据ID查询客户端
  queryClientById(id) {
    return request.get('/v1/ai/admin/client/queryClientById', { params: { id } })
  },

 // 根据智能体ID查询客户端
  queryClientByAgentId(agentId) {
    return request.get('/v1/ai/admin/client/queryClientByAgentId', { params: { agentId } })
  },

 // 新增客户端
  addClient(client) {
    return request.post('/v1/ai/admin/client/addClient', client)
  },

 // 更新客户端
  updateClient(client) {
    return request.post('/v1/ai/admin/client/updateClient', client)
  },

 // 删除客户端
  deleteClient(id) {
    return request.get('/v1/ai/admin/client/deleteClient', { params: { id } })
  }
}