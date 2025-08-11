<template>
  <div class="smart-agent page-container">
      <div class="page-header">
        <h1 class="page-title">智能Agent</h1>
        <p class="page-description">复杂任务规划与执行 - 支持知识库问答、报告生成、会议分析</p>
      </div>

      <!-- 快捷功能卡片 -->
      <div class="feature-cards">
        <div class="feature-card" @click="activeTab = 'knowledge'">
          <div class="card-icon">
            <el-icon size="48" color="#409eff"><Search /></el-icon>
          </div>
          <div class="card-content">
            <h3>知识库问答</h3>
            <p>基于向量数据库的智能问答</p>
          </div>
        </div>

        <div class="feature-card" @click="activeTab = 'report'">
          <div class="card-icon">
            <el-icon size="48" color="#67c23a"><Document /></el-icon>
          </div>
          <div class="card-content">
            <h3>报告生成</h3>
            <p>自动生成PDF格式报告</p>
          </div>
        </div>

        <div class="feature-card" @click="activeTab = 'meeting'">
          <div class="card-icon">
            <el-icon size="48" color="#e6a23c"><VideoCamera /></el-icon>
          </div>
          <div class="card-content">
            <h3>会议分析</h3>
            <p>会议纪要分析和总结</p>
          </div>
        </div>

        <div class="feature-card" @click="activeTab = 'custom'">
          <div class="card-icon">
            <el-icon size="48" color="#f56c6c"><Setting /></el-icon>
          </div>
          <div class="card-content">
            <h3>自定义任务</h3>
            <p>高级任务配置和执行</p>
          </div>
        </div>
      </div>

      <!-- 功能选项卡 -->
      <div class="function-tabs card">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 知识库问答 -->
          <el-tab-pane label="知识库问答" name="knowledge">
            <div class="tab-content">
              <el-form :model="knowledgeForm" label-width="120px">
                <el-form-item label="知识库">
                  <el-select v-model="knowledgeForm.knowledge" placeholder="选择知识库">
                    <el-option label="会议纪要" value="meeting_minutes" />
                    <el-option label="技术文档" value="tech_docs" />
                    <el-option label="产品说明" value="product_docs" />
                    <el-option label="用户手册" value="user_manual" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="问题">
                  <el-input
                    v-model="knowledgeForm.question"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入您的问题，例如：最近会议中讨论了哪些重要议题？"
                  />
                </el-form-item>
                
                <el-form-item label="上下文大小">
                  <el-slider
                    v-model="knowledgeForm.contextSize"
                    :min="1"
                    :max="10"
                    show-input
                    style="width: 300px;"
                  />
                </el-form-item>
                
                <el-form-item label="用户ID">
                  <el-input v-model="knowledgeForm.userId" placeholder="输入用户ID" />
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="executeKnowledgeQA"
                    :loading="isExecuting"
                    :disabled="!knowledgeForm.question || !knowledgeForm.knowledge"
                  >
                    开始问答
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 报告生成 -->
          <el-tab-pane label="报告生成" name="report">
            <div class="tab-content">
              <el-form :model="reportForm" label-width="120px">
                <el-form-item label="报告标题">
                  <el-input v-model="reportForm.title" placeholder="输入报告标题" />
                </el-form-item>
                
                <el-form-item label="报告内容">
                  <el-input
                    v-model="reportForm.content"
                    type="textarea"
                    :rows="8"
                    placeholder="输入报告内容，支持Markdown格式"
                  />
                </el-form-item>
                
                <el-form-item label="输出路径">
                  <el-input v-model="reportForm.outputPath" placeholder="/reports/report.pdf" />
                </el-form-item>
                
                <el-form-item label="用户ID">
                  <el-input v-model="reportForm.userId" placeholder="输入用户ID" />
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="generateReport"
                    :loading="isExecuting"
                    :disabled="!reportForm.title || !reportForm.content"
                  >
                    生成报告
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 会议分析 -->
          <el-tab-pane label="会议分析" name="meeting">
            <div class="tab-content">
              <el-form :model="meetingForm" label-width="120px">
                <el-form-item label="开始日期">
                  <el-date-picker
                    v-model="meetingForm.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                
                <el-form-item label="结束日期">
                  <el-date-picker
                    v-model="meetingForm.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                
                <el-form-item label="用户ID">
                  <el-input v-model="meetingForm.userId" placeholder="输入用户ID" />
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="analyzeMeeting"
                    :loading="isExecuting"
                    :disabled="!meetingForm.startDate || !meetingForm.endDate"
                  >
                    分析会议
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 自定义任务 -->
          <el-tab-pane label="自定义任务" name="custom">
            <div class="tab-content">
              <el-form :model="customForm" label-width="120px">
                <el-form-item label="任务描述">
                  <el-input
                    v-model="customForm.taskDescription"
                    type="textarea"
                    :rows="4"
                    placeholder="详细描述您要执行的任务"
                  />
                </el-form-item>
                
                <el-form-item label="任务类型">
                  <el-select v-model="customForm.taskType" placeholder="选择任务类型">
                    <el-option label="会议分析" value="meeting_analysis" />
                    <el-option label="文档处理" value="document_processing" />
                    <el-option label="报告生成" value="report_generation" />
                    <el-option label="数据分析" value="data_analysis" />
                    <el-option label="其他" value="other" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="优先级">
                  <el-radio-group v-model="customForm.priority">
                    <el-radio :label="1">低</el-radio>
                    <el-radio :label="2">中</el-radio>
                    <el-radio :label="3">高</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="超时时间">
                  <el-input-number
                    v-model="customForm.timeoutMinutes"
                    :min="1"
                    :max="120"
                    placeholder="分钟"
                  />
                </el-form-item>
                
                <el-form-item label="用户ID">
                  <el-input v-model="customForm.userId" placeholder="输入用户ID" />
                </el-form-item>
                
                <el-form-item label="异步执行">
                  <el-switch v-model="customForm.async" />
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="executeCustomTask"
                    :loading="isExecuting"
                    :disabled="!customForm.taskDescription"
                  >
                    执行任务
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 执行结果 -->
      <div v-if="executionResult" class="result-section card">
        <div class="section-header">
          <h3>执行结果</h3>
          <div class="result-actions">
                          <el-button :icon="DocumentCopy" @click="copyResult" text>复制结果</el-button>
            <el-button :icon="Download" @click="downloadResult" text>下载结果</el-button>
          </div>
        </div>
        
        <div class="result-content">
          <div class="result-meta">
            <el-tag v-if="executionResult.taskId" type="info">
              任务ID: {{ executionResult.taskId }}
            </el-tag>
            <el-tag type="success">执行成功</el-tag>
          </div>
          
          <div class="result-text">
            <pre v-if="typeof executionResult === 'string'">{{ executionResult }}</pre>
            <div v-else class="json-result">
              <pre>{{ JSON.stringify(executionResult, null, 2) }}</pre>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误信息 -->
      <div v-if="errorMessage" class="error-section card">
        <el-alert
          title="执行错误"
          :description="errorMessage"
          type="error"
          show-icon
          closable
          @close="errorMessage = ''"
        />
      </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { agentApi } from '@/api/agent'

import {
  Search, Document, VideoCamera, Setting, DocumentCopy, Download
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const activeTab = ref('knowledge')
const isExecuting = ref(false)
const executionResult = ref<any>(null)
const errorMessage = ref('')

// 表单数据
const knowledgeForm = reactive({
  question: '',
  knowledge: 'meeting_minutes',
  contextSize: 5,
  userId: 'admin'
})

const reportForm = reactive({
  title: '',
  content: '',
  outputPath: '/reports/report.pdf',
  userId: 'admin'
})

const meetingForm = reactive({
  startDate: '',
  endDate: '',
  userId: 'admin'
})

const customForm = reactive({
  taskDescription: '',
  taskType: '',
  priority: 2,
  timeoutMinutes: 30,
  userId: 'admin',
  async: true
})

// 方法
const executeKnowledgeQA = async () => {
  try {
    isExecuting.value = true
    errorMessage.value = ''
    
    const result = await agentApi.quickKnowledgeQA(knowledgeForm)
    executionResult.value = result
    
    ElMessage.success('知识库问答完成')
  } catch (error: any) {
    errorMessage.value = error.message || '知识库问答失败'
    ElMessage.error('知识库问答失败')
  } finally {
    isExecuting.value = false
  }
}

const generateReport = async () => {
  try {
    isExecuting.value = true
    errorMessage.value = ''
    
    const result = await agentApi.quickGenerateReport(reportForm)
    executionResult.value = result
    
    ElMessage.success('报告生成完成')
  } catch (error: any) {
    errorMessage.value = error.message || '报告生成失败'
    ElMessage.error('报告生成失败')
  } finally {
    isExecuting.value = false
  }
}

const analyzeMeeting = async () => {
  try {
    isExecuting.value = true
    errorMessage.value = ''
    
    const result = await agentApi.quickMeetingAnalysis(meetingForm)
    executionResult.value = result
    
    ElMessage.success('会议分析完成')
  } catch (error: any) {
    errorMessage.value = error.message || '会议分析失败'
    ElMessage.error('会议分析失败')
  } finally {
    isExecuting.value = false
  }
}

const executeCustomTask = async () => {
  try {
    isExecuting.value = true
    errorMessage.value = ''
    
    const result = await agentApi.executeTask(customForm)
    executionResult.value = result
    
    ElMessage.success('自定义任务已开始执行')
  } catch (error: any) {
    errorMessage.value = error.message || '任务执行失败'
    ElMessage.error('任务执行失败')
  } finally {
    isExecuting.value = false
  }
}

const copyResult = () => {
  const text = typeof executionResult.value === 'string' 
    ? executionResult.value 
    : JSON.stringify(executionResult.value, null, 2)
  
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('结果已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const downloadResult = () => {
  const text = typeof executionResult.value === 'string' 
    ? executionResult.value 
    : JSON.stringify(executionResult.value, null, 2)
  
  const blob = new Blob([text], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `agent-result-${Date.now()}.txt`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  ElMessage.success('结果已下载')
}
</script>

<style lang="scss" scoped>
.smart-agent {
  padding: 24px;
  
  .feature-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    .feature-card {
      background: var(--bg-white);
      border: 1px solid var(--border-light);
      border-radius: 12px;
      padding: 24px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        border-color: var(--primary-color);
        box-shadow: var(--shadow-light);
        transform: translateY(-2px);
      }
      
      .card-icon {
        text-align: center;
        margin-bottom: 16px;
      }
      
      .card-content {
        text-align: center;
        
        h3 {
          font-size: 18px;
          font-weight: 600;
          color: var(--text-primary);
          margin: 0 0 8px 0;
        }
        
        p {
          font-size: 14px;
          color: var(--text-secondary);
          margin: 0;
        }
      }
    }
  }
  
  .function-tabs {
    margin-bottom: 24px;
    
    .tab-content {
      padding: 24px;
    }
  }
  
  .result-section {
    padding: 24px;
    margin-bottom: 24px;
    
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
      
      .result-actions {
        display: flex;
        gap: 8px;
      }
    }
    
    .result-content {
      .result-meta {
        display: flex;
        gap: 8px;
        margin-bottom: 16px;
      }
      
      .result-text {
        pre {
          background: var(--fill-lighter);
          border: 1px solid var(--border-light);
          border-radius: 6px;
          padding: 16px;
          white-space: pre-wrap;
          word-break: break-word;
          max-height: 400px;
          overflow-y: auto;
          font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          font-size: 13px;
          line-height: 1.4;
        }
        
        .json-result {
          pre {
            background: #2d2d30;
            color: #d4d4d4;
            border-color: #3e3e42;
          }
        }
      }
    }
  }
  
  .error-section {
    padding: 24px;
  }
}

:deep(.el-tabs--border-card) {
  border: 1px solid var(--border-light);
  
  .el-tabs__header {
    background: var(--fill-lighter);
    border-bottom: 1px solid var(--border-light);
  }
  
  .el-tabs__item {
    color: var(--text-secondary);
    
    &.is-active {
      color: var(--primary-color);
      background: var(--bg-white);
    }
  }
  
  .el-tabs__content {
    background: var(--bg-white);
  }
}

@media (max-width: 768px) {
  .smart-agent {
    padding: 16px;
    
    .feature-cards {
      grid-template-columns: 1fr;
      gap: 16px;
    }
    
    .tab-content {
      padding: 16px;
    }
    
    .section-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
  }
}
</style>