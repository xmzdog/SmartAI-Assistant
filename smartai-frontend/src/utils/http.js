import axios from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置NProgress
NProgress.configure({ showSpinner: false })

// 创建axios实例
const http = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    NProgress.start()
    
    // 添加token等认证信息
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  (response) => {
    NProgress.done()
    
    const { data } = response
    
    // 检查业务状态码
    if (data.code === '0000') {
      return response
    } else {
      ElMessage.error(data.info || '请求失败')
      return Promise.reject(new Error(data.info || '请求失败'))
    }
  },
  (error) => {
    NProgress.done()
    
    let message = '网络错误'
    
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 400:
          message = data.info || '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          // 清除token并跳转到登录页
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = data.info || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http

// 封装常用的请求方法
export const request = {
  get(url, config = {}) {
    return http.get(url, config).then(res => res.data)
  },
  
  post(url, data, config = {}) {
    return http.post(url, data, config).then(res => res.data)
  },
  
  put(url, data, config = {}) {
    return http.put(url, data, config).then(res => res.data)
  },
  
  delete(url, config = {}) {
    return http.delete(url, config).then(res => res.data)
  }
}