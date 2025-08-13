/**
 * 大内容处理工具
 * 处理超大消息内容的显示和交互
 */

/**
 * 内容大小阈值（64KB）
 */
export const CONTENT_SIZE_THRESHOLD = 64 * 1024

/**
 * 检查是否为大内容
 * @param {string} content - 内容
 * @returns {boolean} 是否为大内容
 */
export function isLargeContent(content) {
  if (!content) return false
  return new Blob([content]).size > CONTENT_SIZE_THRESHOLD
}

/**
 * 格式化内容大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小
 */
export function formatContentSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

/**
 * 截取内容预览
 * @param {string} content - 原始内容
 * @param {number} maxLength - 最大长度，默认500字符
 * @returns {string} 截取后的内容
 */
export function truncateContent(content, maxLength = 500) {
  if (!content || content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

/**
 * 检测内容类型
 * @param {string} content - 内容
 * @returns {string} 内容类型
 */
export function detectContentType(content) {
  if (!content) return 'text'
  
  // JSON
  try {
    JSON.parse(content)
    return 'json'
  } catch (e) {
    // 不是JSON
  }
  
  // Markdown
  if (content.includes('```') || content.includes('#') || content.includes('*')) {
    return 'markdown'
  }
  
  // Code
  if (content.includes('function') || content.includes('class') || 
      content.includes('import') || content.includes('def ')) {
    return 'code'
  }
  
  // XML/HTML
  if (content.includes('<') && content.includes('>')) {
    return 'xml'
  }
  
  return 'text'
}

/**
 * 创建大内容警告消息
 * @param {number} size - 内容大小
 * @returns {object} 警告消息对象
 */
export function createLargeContentWarning(size) {
  return {
    type: 'warning',
    title: '大内容消息',
    message: `此消息内容较大（${formatContentSize(size)}），可能影响页面性能。`,
    actions: [
      { text: '显示预览', value: 'preview' },
      { text: '显示全部', value: 'full' },
      { text: '下载文件', value: 'download' }
    ]
  }
}

/**
 * 创建内容下载
 * @param {string} content - 内容
 * @param {string} filename - 文件名
 * @param {string} type - MIME类型
 */
export function downloadContent(content, filename = 'message.txt', type = 'text/plain') {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 压缩内容显示（仅用于显示优化）
 * @param {string} content - 原始内容
 * @returns {object} 压缩结果
 */
export function compressContentForDisplay(content) {
  if (!content) return { content: '', compressed: false }
  
  // 移除多余的空白字符
  let compressed = content.replace(/\s+/g, ' ').trim()
  
  // 如果仍然很大，进一步截取
  if (compressed.length > 10000) {
    compressed = compressed.substring(0, 10000) + '\n\n[内容已截取，完整内容请点击"显示全部"]'
  }
  
  return {
    content: compressed,
    compressed: compressed !== content,
    originalSize: new Blob([content]).size,
    compressedSize: new Blob([compressed]).size
  }
}

/**
 * 分页显示大内容
 * @param {string} content - 内容
 * @param {number} pageSize - 每页大小，默认5000字符
 * @returns {array} 分页结果
 */
export function paginateContent(content, pageSize = 5000) {
  if (!content) return []
  
  const pages = []
  let currentIndex = 0
  
  while (currentIndex < content.length) {
    const pageContent = content.substring(currentIndex, currentIndex + pageSize)
    pages.push({
      index: pages.length + 1,
      content: pageContent,
      startIndex: currentIndex,
      endIndex: Math.min(currentIndex + pageSize, content.length)
    })
    currentIndex += pageSize
  }
  
  return pages
}

/**
 * 内容统计信息
 * @param {string} content - 内容
 * @returns {object} 统计信息
 */
export function getContentStats(content) {
  if (!content) return { characters: 0, words: 0, lines: 0, size: 0 }
  
  return {
    characters: content.length,
    words: content.split(/\s+/).filter(word => word.length > 0).length,
    lines: content.split('\n').length,
    size: new Blob([content]).size,
    type: detectContentType(content)
  }
}
