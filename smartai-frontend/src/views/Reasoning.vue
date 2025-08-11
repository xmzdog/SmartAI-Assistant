<template>
  <div class="reasoning page-container">
      <div class="page-header">
        <h1 class="page-title">深度推理</h1>
        <p class="page-description">基于Chain-of-Thought的复杂问题分析和推理</p>
      </div>

      <!-- 推理输入区 -->
      <div class="input-section card">
        <div class="section-header">
          <h3>问题输入</h3>
          <div class="strategy-selector">
            <el-select v-model="currentStrategy" placeholder="选择推理策略">
              <el-option label="综合分析" value="COMPREHENSIVE" />
              <el-option label="步骤推理" value="STEP_BY_STEP" />
              <el-option label="快速模式" value="QUICK" />
            </el-select>
          </div>
        </div>
        
        <div class="input-content">
          <el-input
            v-model="problem"
            type="textarea"
            :rows="6"
            placeholder="请输入您需要深度分析的问题或场景，例如：&#10;&#10;如何设计一个高可用的微服务架构？需要考虑哪些关键因素？&#10;&#10;分析我们团队当前面临的技术挑战，并提出解决方案&#10;&#10;评估不同AI模型在我们业务场景中的适用性"
            maxlength="2000"
            show-word-limit
          />
          
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :loading="isReasoning"
              @click="startReasoning"
              :disabled="!problem.trim()"
              :icon="Cpu"
            >
              开始推理分析
            </el-button>
            
            <el-button
              size="large"
              @click="clearInput"
              :disabled="isReasoning"
            >
              清空输入
            </el-button>
          </div>
        </div>
      </div>

      <!-- 推理过程展示 -->
      <div v-if="isReasoning || reasoningResult" class="reasoning-section card">
        <div class="section-header">
          <h3>推理过程</h3>
          <div class="reasoning-meta">
            <el-tag v-if="currentStrategy" type="info" effect="light">
              {{ getStrategyText(currentStrategy) }}
            </el-tag>
            <span v-if="reasoningStartTime" class="time-info">
              开始时间: {{ formatDateTime(reasoningStartTime) }}
            </span>
          </div>
        </div>
        
        <div class="reasoning-content">
          <!-- 推理进行中 -->
          <div v-if="isReasoning" class="reasoning-progress">
            <div class="progress-indicator">
              <el-icon class="rotating"><Loading /></el-icon>
              <span>AI正在深度分析中...</span>
            </div>
            
            <div class="progress-steps">
              <div class="step-item active">
                <el-icon><Check /></el-icon>
                <span>问题理解</span>
              </div>
              <div class="step-item active">
                <el-icon><Loading /></el-icon>
                <span>深度分析</span>
              </div>
              <div class="step-item">
                <el-icon><Clock /></el-icon>
                <span>方案生成</span>
              </div>
              <div class="step-item">
                <el-icon><Clock /></el-icon>
                <span>结果整理</span>
              </div>
            </div>
          </div>
          
          <!-- 推理结果 -->
          <div v-if="reasoningResult" class="reasoning-result">
            <div class="result-header">
              <div class="problem-summary">
                <h4>分析问题</h4>
                <p>{{ problem }}</p>
              </div>
              
              <div class="result-meta">
                <span class="duration">
                  推理耗时: {{ reasoningDuration }}
                </span>
                <el-button :icon="Copy" @click="copyResult" text>
                  复制结果
                </el-button>
              </div>
            </div>
            
            <div class="result-content">
              <div class="markdown-content" v-html="formatResult(reasoningResult)"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 推理历史 -->
      <div class="history-section card">
        <div class="section-header">
          <h3>推理历史</h3>
          <div class="history-actions">
            <el-button :icon="Download" @click="exportHistory" text>
              导出历史
            </el-button>
            <el-button :icon="Delete" @click="clearHistory" text type="danger">
              清空历史
            </el-button>
          </div>
        </div>
        
        <div class="history-list">
          <div v-if="historyList.length === 0" class="empty-state">
            <el-icon size="48" color="#c0c4cc"><DocumentCopy /></el-icon>
            <p>暂无推理历史</p>
          </div>
          
          <div
            v-for="(item, index) in historyList"
            :key="index"
            class="history-item"
            @click="viewHistoryItem(item)"
          >
            <div class="item-header">
              <div class="item-title">{{ truncateText(item.problem, 100) }}</div>
              <div class="item-meta">
                <el-tag :type="getStrategyTagType(item.strategy)" size="small" effect="light">
                  {{ getStrategyText(item.strategy) }}
                </el-tag>
                <span class="item-time">{{ formatDateTime(item.timestamp) }}</span>
              </div>
            </div>
            
            <div class="item-preview">
              {{ truncateText(item.result, 200) }}
            </div>
            
            <div class="item-actions" @click.stop>
              <el-button :icon="View" @click="viewHistoryItem(item)" text size="small">
                查看
              </el-button>
              <el-button :icon="Copy" @click="copyHistoryResult(item)" text size="small">
                复制
              </el-button>
              <el-button :icon="Delete" @click="deleteHistoryItem(index)" text type="danger" size="small">
                删除
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="historyList.length > 0" class="history-pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="historyList.length"
            layout="prev, pager, next"
            small
          />
        </div>
      </div>

      <!-- 推理策略说明 -->
      <div class="strategy-info card">
        <div class="section-header">
          <h3>推理策略说明</h3>
        </div>
        
        <div class="strategy-list">
          <div class="strategy-item">
            <div class="strategy-name">
              <el-icon color="#409eff"><Cpu /></el-icon>
              <span>综合分析 (COMPREHENSIVE)</span>
            </div>
            <div class="strategy-description">
              深度全面分析，考虑多个维度和角度，提供详细的分析过程和结论
            </div>
          </div>
          
          <div class="strategy-item">
            <div class="strategy-name">
              <el-icon color="#67c23a"><Sort /></el-icon>
              <span>步骤推理 (STEP_BY_STEP)</span>
            </div>
            <div class="strategy-description">
              逐步分析，按照逻辑顺序展示推理过程，适合复杂问题的结构化分析
            </div>
          </div>
          
          <div class="strategy-item">
            <div class="strategy-name">
              <el-icon color="#e6a23c"><Lightning /></el-icon>
              <span>快速模式 (QUICK)</span>
            </div>
            <div class="strategy-description">
              快速响应模式，提供核心观点和建议，适合简单问题的快速分析
            </div>
          </div>
        </div>
      </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { manusApi } from '@/api/manus'

import {
  Cpu, Copy, Download, Delete, View, Check, Loading, Clock,
  DocumentCopy, Sort, Lightning
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { marked } from 'marked'

// 响应式数据
const problem = ref('')
const currentStrategy = ref('COMPREHENSIVE')
const isReasoning = ref(false)
const reasoningResult = ref('')
const reasoningStartTime = ref<string | null>(null)
const reasoningEndTime = ref<string | null>(null)
const historyList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

// 计算属性
const reasoningDuration = computed(() => {
  if (!reasoningStartTime.value || !reasoningEndTime.value) return 'N/A'
  const duration = dayjs(reasoningEndTime.value).diff(dayjs(reasoningStartTime.value), 'second')
  return `${duration}秒`
})

// 方法
const startReasoning = async () => {
  try {
    isReasoning.value = true
    reasoningStartTime.value = new Date().toISOString()
    reasoningEndTime.value = null
    reasoningResult.value = ''
    
    const result = await manusApi.reasoning({
      problem: problem.value,
      strategy: currentStrategy.value
    })
    
    reasoningResult.value = result
    reasoningEndTime.value = new Date().toISOString()
    
    // 保存到历史
    saveToHistory()
    
    ElMessage.success('推理分析完成')
  } catch (error: any) {
    ElMessage.error(error.message || '推理分析失败')
  } finally {
    isReasoning.value = false
  }
}

const clearInput = () => {
  problem.value = ''
  reasoningResult.value = ''
  reasoningStartTime.value = null
  reasoningEndTime.value = null
}

const saveToHistory = () => {
  const historyItem = {
    problem: problem.value,
    strategy: currentStrategy.value,
    result: reasoningResult.value,
    timestamp: new Date().toISOString(),
    duration: reasoningDuration.value
  }
  
  historyList.value.unshift(historyItem)
  
  // 保存到本地存储
  localStorage.setItem('reasoning-history', JSON.stringify(historyList.value))
}

const loadHistory = () => {
  const saved = localStorage.getItem('reasoning-history')
  if (saved) {
    try {
      historyList.value = JSON.parse(saved)
    } catch (error) {
      console.error('加载推理历史失败:', error)
    }
  }
}

const viewHistoryItem = (item: any) => {
  problem.value = item.problem
  currentStrategy.value = item.strategy
  reasoningResult.value = item.result
  reasoningStartTime.value = item.timestamp
  reasoningEndTime.value = item.timestamp
}

const copyResult = () => {
  navigator.clipboard.writeText(reasoningResult.value).then(() => {
    ElMessage.success('结果已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const copyHistoryResult = (item: any) => {
  navigator.clipboard.writeText(item.result).then(() => {
    ElMessage.success('结果已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const deleteHistoryItem = async (index: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条推理记录吗？', '确认删除', {
      type: 'warning'
    })
    
    historyList.value.splice(index, 1)
    localStorage.setItem('reasoning-history', JSON.stringify(historyList.value))
    ElMessage.success('记录已删除')
  } catch {
    // 用户取消
  }
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有推理历史吗？', '确认操作', {
      type: 'warning'
    })
    
    historyList.value = []
    localStorage.removeItem('reasoning-history')
    ElMessage.success('推理历史已清空')
  } catch {
    // 用户取消
  }
}

const exportHistory = () => {
  if (historyList.value.length === 0) {
    ElMessage.warning('没有可导出的历史记录')
    return
  }
  
  const exportData = {
    exportTime: new Date().toISOString(),
    totalCount: historyList.value.length,
    history: historyList.value
  }
  
  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `reasoning-history-${dayjs().format('YYYY-MM-DD')}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  ElMessage.success('历史记录已导出')
}

const getStrategyText = (strategy: string) => {
  switch (strategy) {
    case 'COMPREHENSIVE': return '综合分析'
    case 'STEP_BY_STEP': return '步骤推理'
    case 'QUICK': return '快速模式'
    default: return strategy
  }
}

const getStrategyTagType = (strategy: string) => {
  switch (strategy) {
    case 'COMPREHENSIVE': return 'primary'
    case 'STEP_BY_STEP': return 'success'
    case 'QUICK': return 'warning'
    default: return 'info'
  }
}

const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatResult = (result: string) => {
  try {
    return marked(result)
  } catch (error) {
    return result.replace(/\n/g, '<br>')
  }
}

// 生命周期
onMounted(() => {
  loadHistory()
})
</script>

<style lang="scss" scoped>
.reasoning {
  padding: 24px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
    
    .reasoning-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .time-info {
        font-size: 12px;
        color: var(--text-placeholder);
      }
    }
  }
  
  .input-section {
    padding: 24px;
    margin-bottom: 24px;
    
    .input-content {
      .action-buttons {
        display: flex;
        gap: 12px;
        margin-top: 16px;
        justify-content: center;
      }
    }
  }
  
  .reasoning-section {
    padding: 24px;
    margin-bottom: 24px;
    
    .reasoning-progress {
      text-align: center;
      
      .progress-indicator {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
        margin-bottom: 32px;
        font-size: 16px;
        color: var(--primary-color);
        
        .rotating {
          animation: rotate 2s linear infinite;
        }
      }
      
      .progress-steps {
        display: flex;
        justify-content: center;
        gap: 24px;
        
        .step-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;
          padding: 16px;
          border-radius: 8px;
          transition: all 0.3s;
          
          &.active {
            background: var(--primary-color);
            background-opacity: 0.1;
            color: var(--primary-color);
          }
          
          span {
            font-size: 12px;
          }
        }
      }
    }
    
    .reasoning-result {
      .result-header {
        margin-bottom: 24px;
        
        .problem-summary {
          margin-bottom: 16px;
          
          h4 {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-primary);
            margin: 0 0 8px 0;
          }
          
          p {
            color: var(--text-secondary);
            background: var(--fill-lighter);
            padding: 12px;
            border-radius: 6px;
            margin: 0;
            border-left: 4px solid var(--primary-color);
          }
        }
        
        .result-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .duration {
            font-size: 12px;
            color: var(--text-placeholder);
          }
        }
      }
      
      .result-content {
        .markdown-content {
          background: var(--bg-white);
          border: 1px solid var(--border-light);
          border-radius: 8px;
          padding: 20px;
          line-height: 1.6;
          
          :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
            color: var(--text-primary);
            margin-top: 0;
            margin-bottom: 16px;
          }
          
          :deep(p) {
            margin-bottom: 16px;
            color: var(--text-regular);
          }
          
          :deep(ul), :deep(ol) {
            margin-bottom: 16px;
            padding-left: 20px;
            
            li {
              margin-bottom: 8px;
              color: var(--text-regular);
            }
          }
          
          :deep(code) {
            background: var(--fill-lighter);
            padding: 2px 6px;
            border-radius: 4px;
            font-family: monospace;
          }
          
          :deep(pre) {
            background: var(--fill-lighter);
            padding: 16px;
            border-radius: 6px;
            overflow-x: auto;
            
            code {
              background: none;
              padding: 0;
            }
          }
        }
      }
    }
  }
  
  .history-section {
    padding: 24px;
    margin-bottom: 24px;
    
    .history-actions {
      display: flex;
      gap: 8px;
    }
    
    .history-list {
      .history-item {
        border: 1px solid var(--border-light);
        border-radius: 8px;
        padding: 16px;
        margin-bottom: 12px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          border-color: var(--primary-color);
          box-shadow: var(--shadow-base);
        }
        
        .item-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 8px;
          
          .item-title {
            font-weight: 500;
            color: var(--text-primary);
            flex: 1;
            margin-right: 12px;
          }
          
          .item-meta {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-shrink: 0;
            
            .item-time {
              font-size: 12px;
              color: var(--text-placeholder);
            }
          }
        }
        
        .item-preview {
          color: var(--text-secondary);
          font-size: 14px;
          line-height: 1.4;
          margin-bottom: 12px;
        }
        
        .item-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
    
    .history-pagination {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }
  
  .strategy-info {
    padding: 24px;
    
    .strategy-list {
      .strategy-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 16px;
        padding: 16px;
        background: var(--fill-lighter);
        border-radius: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .strategy-name {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 500;
          color: var(--text-primary);
          min-width: 180px;
        }
        
        .strategy-description {
          color: var(--text-secondary);
          line-height: 1.5;
        }
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: var(--text-secondary);
    
    p {
      margin-top: 12px;
      font-size: 14px;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .reasoning {
    padding: 16px;
    
    .action-buttons {
      flex-direction: column;
    }
    
    .progress-steps {
      flex-direction: column;
      gap: 12px;
    }
    
    .item-header {
      flex-direction: column;
      gap: 8px;
    }
    
    .strategy-item {
      flex-direction: column;
      
      .strategy-name {
        min-width: auto;
      }
    }
  }
}
</style>