import http from '@/utils/http'

/**
 * API基础服务类
 * 提供通用的CRUD操作方法
 */
export class BaseApiService {
  constructor(baseUrl) {
    this.baseUrl = baseUrl
  }

  /**
   * 获取列表数据
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  async getList(params = {}) {
    const response = await http.get(this.baseUrl, { params })
    return response
  }

  /**
   * 根据ID获取详情
   * @param {number|string} id - 记录ID
   * @returns {Promise}
   */
  async getById(id) {
    const response = await http.get(`${this.baseUrl}/${id}`)
    return response
  }

  /**
   * 创建新记录
   * @param {Object} data - 创建数据
   * @returns {Promise}
   */
  async create(data) {
    const response = await http.post(this.baseUrl, data)
    return response
  }

  /**
   * 更新记录
   * @param {number|string} id - 记录ID
   * @param {Object} data - 更新数据
   * @returns {Promise}
   */
  async update(id, data) {
    const response = await http.put(`${this.baseUrl}/${id}`, data)
    return response
  }

  /**
   * 删除记录
   * @param {number|string} id - 记录ID
   * @returns {Promise}
   */
  async delete(id) {
    const response = await http.delete(`${this.baseUrl}/${id}`)
    return response
  }

  /**
   * 批量删除
   * @param {Array} ids - ID数组
   * @returns {Promise}
   */
  async batchDelete(ids) {
    const response = await http.delete(`${this.baseUrl}/batch`, { data: { ids } })
    return response
  }

  /**
   * 更新状态
   * @param {number|string} id - 记录ID
   * @param {number} status - 状态值
   * @returns {Promise}
   */
  async updateStatus(id, status) {
    const response = await http.patch(`${this.baseUrl}/${id}/status`, { status })
    return response
  }

  /**
   * 批量更新状态
   * @param {Array} ids - ID数组
   * @param {number} status - 状态值
   * @returns {Promise}
   */
  async batchUpdateStatus(ids, status) {
    const response = await http.patch(`${this.baseUrl}/batch/status`, { ids, status })
    return response
  }
}

/**
 * 创建模拟数据响应
 * 用于开发阶段，当后端接口还未完成时
 */
export class MockApiService extends BaseApiService {
  constructor(baseUrl, mockData = []) {
    super(baseUrl)
    this.mockData = mockData
    this.nextId = Math.max(...mockData.map(item => item.id || 0), 0) + 1
  }

  async getList(params = {}) {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 300))
    
    let filteredData = [...this.mockData]
    
    // 模拟搜索
    if (params.search) {
      const searchTerm = params.search.toLowerCase()
      filteredData = filteredData.filter(item => 
        Object.values(item).some(value => 
          String(value).toLowerCase().includes(searchTerm)
        )
      )
    }
    
    // 模拟状态筛选
    if (params.status !== undefined && params.status !== '') {
      filteredData = filteredData.filter(item => item.status === Number(params.status))
    }
    
    // 模拟分页
    const page = params.page || 1
    const size = params.size || 10
    const total = filteredData.length
    const start = (page - 1) * size
    const end = start + size
    const records = filteredData.slice(start, end)
    
    return {
      code: 200,
      message: 'success',
      data: {
        records,
        total,
        page,
        size,
        pages: Math.ceil(total / size)
      }
    }
  }

  async getById(id) {
    await new Promise(resolve => setTimeout(resolve, 200))
    const item = this.mockData.find(item => item.id === Number(id))
    if (!item) {
      throw new Error('记录不存在')
    }
    return {
      code: 200,
      message: 'success',
      data: item
    }
  }

  async create(data) {
    await new Promise(resolve => setTimeout(resolve, 500))
    const newItem = {
      ...data,
      id: this.nextId++,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString()
    }
    this.mockData.push(newItem)
    return {
      code: 200,
      message: '创建成功',
      data: newItem
    }
  }

  async update(id, data) {
    await new Promise(resolve => setTimeout(resolve, 500))
    const index = this.mockData.findIndex(item => item.id === Number(id))
    if (index === -1) {
      throw new Error('记录不存在')
    }
    this.mockData[index] = {
      ...this.mockData[index],
      ...data,
      updateTime: new Date().toISOString()
    }
    return {
      code: 200,
      message: '更新成功',
      data: this.mockData[index]
    }
  }

  async delete(id) {
    await new Promise(resolve => setTimeout(resolve, 300))
    const index = this.mockData.findIndex(item => item.id === Number(id))
    if (index === -1) {
      throw new Error('记录不存在')
    }
    this.mockData.splice(index, 1)
    return {
      code: 200,
      message: '删除成功'
    }
  }

  async batchDelete(ids) {
    await new Promise(resolve => setTimeout(resolve, 500))
    const numIds = ids.map(id => Number(id))
    this.mockData = this.mockData.filter(item => !numIds.includes(item.id))
    return {
      code: 200,
      message: `成功删除 ${ids.length} 条记录`
    }
  }

  async updateStatus(id, status) {
    await new Promise(resolve => setTimeout(resolve, 300))
    const index = this.mockData.findIndex(item => item.id === Number(id))
    if (index === -1) {
      throw new Error('记录不存在')
    }
    this.mockData[index].status = status
    this.mockData[index].updateTime = new Date().toISOString()
    return {
      code: 200,
      message: '状态更新成功',
      data: this.mockData[index]
    }
  }

  async batchUpdateStatus(ids, status) {
    await new Promise(resolve => setTimeout(resolve, 500))
    const numIds = ids.map(id => Number(id))
    let count = 0
    this.mockData.forEach(item => {
      if (numIds.includes(item.id)) {
        item.status = status
        item.updateTime = new Date().toISOString()
        count++
      }
    })
    return {
      code: 200,
      message: `成功更新 ${count} 条记录的状态`
    }
  }
}
