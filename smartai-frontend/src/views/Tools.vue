<template>
  <div class="tools page-container">
      <div class="page-header">
        <h1 class="page-title">工具管理</h1>
        <p class="page-description">查看和管理AI智能体可用的工具和插件</p>
      </div>

      <!-- 工具统计 -->
      <div class="tools-stats">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#409eff"><Tools /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ tools.length }}</div>
            <div class="stat-label">总工具数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#67c23a"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ enabledTools.length }}</div>
            <div class="stat-label">已启用</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#e6a23c"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ disabledTools.length }}</div>
            <div class="stat-label">已禁用</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="32" color="#909399"><Grid /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ categories.length }}</div>
            <div class="stat-label">工具分类</div>
          </div>
        </div>
      </div>

      <!-- 工具筛选 -->
      <div class="tools-filter card">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索工具名称或描述"
            style="width: 300px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select v-model="categoryFilter" placeholder="选择分类" style="width: 150px;" clearable>
            <el-option label="全部分类" value="" />
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>

          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px;" clearable>
            <el-option label="全部状态" value="" />
            <el-option label="已启用" value="enabled" />
            <el-option label="已禁用" value="disabled" />
          </el-select>
        </div>

        <div class="filter-right">
          <el-button :icon="Refresh" @click="refreshTools" :loading="isLoading">
            刷新
          </el-button>
          <el-button :icon="Setting" @click="showConfigDialog = true">
            工具配置
          </el-button>
        </div>
      </div>

      <!-- 工具列表 -->
      <div class="tools-grid">
        <div v-if="filteredTools.length === 0" class="empty-state">
          <el-icon size="64" color="#c0c4cc"><Box /></el-icon>
          <p>没有找到匹配的工具</p>
        </div>

        <div
          v-for="tool in filteredTools"
          :key="tool.name"
          class="tool-card"
          :class="{ disabled: !tool.enabled }"
        >
          <div class="tool-header">
            <div class="tool-icon">
              <el-icon size="24" :color="getToolIconColor(tool.category)">
                <component :is="getToolIcon(tool.category)" />
              </el-icon>
            </div>
            <div class="tool-status">
              <el-switch
                v-model="tool.enabled"
                @change="toggleTool(tool)"
                :loading="tool.loading"
              />
            </div>
          </div>

          <div class="tool-content">
            <h3 class="tool-name">{{ tool.name }}</h3>
            <p class="tool-description">{{ tool.description }}</p>
            
            <div class="tool-meta">
              <el-tag :type="getCategoryTagType(tool.category)" size="small" effect="light">
                {{ tool.category }}
              </el-tag>
              
              <span class="tool-params">
                {{ Object.keys(tool.parameters || {}).length }} 个参数
              </span>
            </div>
          </div>

          <div class="tool-actions">
            <el-button :icon="View" @click="viewToolDetail(tool)" text size="small">
              查看详情
            </el-button>
            <el-button :icon="Setting" @click="configTool(tool)" text size="small">
              配置
            </el-button>
                                <el-button :icon="VideoPlay" @click="testTool(tool)" text size="small" type="primary">
              测试
            </el-button>
          </div>
        </div>
      </div>

      <!-- 工具详情弹窗 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="工具详情"
        width="60%"
        :close-on-click-modal="false"
      >
        <div v-if="selectedTool" class="tool-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工具名称">
              {{ selectedTool.name }}
            </el-descriptions-item>
            <el-descriptions-item label="分类">
              <el-tag :type="getCategoryTagType(selectedTool.category)" effect="light">
                {{ selectedTool.category }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="selectedTool.enabled ? 'success' : 'danger'" effect="light">
                {{ selectedTool.enabled ? '已启用' : '已禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="参数数量">
              {{ Object.keys(selectedTool.parameters || {}).length }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="tool-description-section">
            <h4>工具描述</h4>
            <p>{{ selectedTool.description }}</p>
          </div>

          <div v-if="selectedTool.parameters" class="tool-parameters-section">
            <h4>参数列表</h4>
            <el-table :data="parametersList" border>
              <el-table-column prop="name" label="参数名" width="150" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="required" label="必需" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                    {{ row.required ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" />
            </el-table>
          </div>
        </div>

        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="selectedTool"
            type="primary"
            @click="testTool(selectedTool)"
          >
            测试工具
          </el-button>
        </template>
      </el-dialog>

      <!-- 工具配置弹窗 -->
      <el-dialog
        v-model="showConfigDialog"
        title="工具配置"
        width="50%"
        :close-on-click-modal="false"
      >
        <div class="config-content">
          <el-form :model="configForm" label-width="120px">
            <el-form-item label="自动刷新">
              <el-switch v-model="configForm.autoRefresh" />
              <span class="form-help">自动刷新工具状态</span>
            </el-form-item>
            
            <el-form-item label="刷新间隔">
              <el-input-number
                v-model="configForm.refreshInterval"
                :min="5"
                :max="300"
                :disabled="!configForm.autoRefresh"
              />
              <span class="form-help">秒</span>
            </el-form-item>
            
            <el-form-item label="默认分类筛选">
              <el-select v-model="configForm.defaultCategory" placeholder="选择默认分类" clearable>
                <el-option
                  v-for="category in categories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <template #footer>
          <el-button @click="showConfigDialog = false">取消</el-button>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
        </template>
      </el-dialog>

      <!-- 工具测试弹窗 -->
      <el-dialog
        v-model="testDialogVisible"
        title="工具测试"
        width="60%"
        :close-on-click-modal="false"
      >
        <div v-if="testingTool" class="test-content">
          <el-form :model="testForm" label-width="100px">
            <el-form-item
              v-for="(param, key) in testingTool.parameters"
              :key="key"
              :label="key"
              :required="param.required"
            >
              <el-input
                v-model="testForm[key]"
                :placeholder="param.description || `请输入${key}`"
              />
              <div class="param-help">
                类型: {{ param.type || 'string' }} | 
                {{ param.required ? '必需' : '可选' }}
              </div>
            </el-form-item>
          </el-form>

          <div v-if="testResult" class="test-result">
            <h4>测试结果</h4>
            <pre>{{ testResult }}</pre>
          </div>
        </div>

        <template #footer>
          <el-button @click="testDialogVisible = false">关闭</el-button>
          <el-button
            type="primary"
            @click="executeTest"
            :loading="isTesting"
          >
            执行测试
          </el-button>
        </template>
      </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useSystemStore } from '@/stores/system'

import {
  Tools, CircleCheck, Warning, Grid, Search, Refresh, Setting,
  Box, View, VideoPlay, Database, Globe, Document, Grid,
  Camera, Message, Avatar, Service
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const systemStore = useSystemStore()

// 响应式数据
const searchKeyword = ref('')
const categoryFilter = ref('')
const statusFilter = ref('')
const isLoading = ref(false)
const detailDialogVisible = ref(false)
const testDialogVisible = ref(false)
const showConfigDialog = ref(false)
const selectedTool = ref<any>(null)
const testingTool = ref<any>(null)
const testResult = ref('')
const isTesting = ref(false)

// 表单数据
const testForm = reactive<Record<string, string>>({})
const configForm = reactive({
  autoRefresh: true,
  refreshInterval: 30,
  defaultCategory: ''
})

// 计算属性
const tools = computed(() => systemStore.tools)
const enabledTools = computed(() => tools.value.filter(tool => tool.enabled))
const disabledTools = computed(() => tools.value.filter(tool => !tool.enabled))
const categories = computed(() => {
  const cats = [...new Set(tools.value.map(tool => tool.category))]
  return cats.filter(Boolean)
})

const filteredTools = computed(() => {
  let filtered = [...tools.value]

  // 分类筛选
  if (categoryFilter.value) {
    filtered = filtered.filter(tool => tool.category === categoryFilter.value)
  }

  // 状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(tool => {
      return statusFilter.value === 'enabled' ? tool.enabled : !tool.enabled
    })
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(tool =>
      tool.name.toLowerCase().includes(keyword) ||
      tool.description.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

const parametersList = computed(() => {
  if (!selectedTool.value?.parameters) return []
  
  return Object.entries(selectedTool.value.parameters).map(([name, param]: [string, any]) => ({
    name,
    type: param.type || 'string',
    required: param.required || false,
    description: param.description || '-'
  }))
})

// 方法
const refreshTools = async () => {
  isLoading.value = true
  try {
    await systemStore.fetchTools()
    ElMessage.success('工具列表已更新')
  } catch (error) {
    ElMessage.error('获取工具列表失败')
  } finally {
    isLoading.value = false
  }
}

const toggleTool = async (tool: any) => {
  tool.loading = true
  try {
    // 这里应该调用API来启用/禁用工具
    await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟API调用
    ElMessage.success(`工具 ${tool.name} 已${tool.enabled ? '启用' : '禁用'}`)
  } catch (error) {
    tool.enabled = !tool.enabled // 回滚状态
    ElMessage.error('操作失败')
  } finally {
    tool.loading = false
  }
}

const viewToolDetail = (tool: any) => {
  selectedTool.value = tool
  detailDialogVisible.value = true
}

const configTool = (tool: any) => {
  ElMessage.info('工具配置功能开发中...')
}

const testTool = (tool: any) => {
  testingTool.value = tool
  testResult.value = ''
  
  // 重置测试表单
  Object.keys(testForm).forEach(key => delete testForm[key])
  if (tool.parameters) {
    Object.keys(tool.parameters).forEach(key => {
      testForm[key] = ''
    })
  }
  
  testDialogVisible.value = true
}

const executeTest = async () => {
  isTesting.value = true
  try {
    // 模拟工具测试
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    testResult.value = JSON.stringify({
      tool: testingTool.value.name,
      parameters: testForm,
      result: `工具 ${testingTool.value.name} 执行成功`,
      timestamp: new Date().toISOString()
    }, null, 2)
    
    ElMessage.success('工具测试完成')
  } catch (error) {
    testResult.value = `测试失败: ${error}`
    ElMessage.error('工具测试失败')
  } finally {
    isTesting.value = false
  }
}

const saveConfig = () => {
  // 保存配置到本地存储
  localStorage.setItem('tools-config', JSON.stringify(configForm))
  showConfigDialog.value = false
  ElMessage.success('配置已保存')
}

const loadConfig = () => {
  const saved = localStorage.getItem('tools-config')
  if (saved) {
    try {
      Object.assign(configForm, JSON.parse(saved))
    } catch (error) {
      console.error('加载配置失败:', error)
    }
  }
}

const getToolIcon = (category: string) => {
  switch (category.toLowerCase()) {
    case 'database': return Database
    case 'network': return Globe
    case 'document': return Document
    case 'calculation': return Grid
    case 'media': return Camera
    case 'communication': return Message
    case 'ai': return Service
    default: return Tools
  }
}

const getToolIconColor = (category: string) => {
  switch (category.toLowerCase()) {
    case 'database': return '#409eff'
    case 'network': return '#67c23a'
    case 'document': return '#e6a23c'
    case 'calculation': return '#f56c6c'
    case 'media': return '#909399'
    case 'communication': return '#409eff'
    case 'ai': return '#9c27b0'
    default: return '#909399'
  }
}

const getCategoryTagType = (category: string) => {
  switch (category.toLowerCase()) {
    case 'database': return 'primary'
    case 'network': return 'success'
    case 'document': return 'warning'
    case 'calculation': return 'danger'
    case 'ai': return 'primary'
    default: return 'info'
  }
}

// 生命周期
onMounted(() => {
  loadConfig()
  refreshTools()
})
</script>

<style lang="scss" scoped>
.tools {
  padding: 24px;
  
  .tools-stats {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    .stat-card {
      background: var(--bg-white);
      border: 1px solid var(--border-light);
      border-radius: 8px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      
      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: var(--text-primary);
        }
        
        .stat-label {
          font-size: 14px;
          color: var(--text-secondary);
          margin-top: 4px;
        }
      }
    }
  }
  
  .tools-filter {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    margin-bottom: 24px;
    
    .filter-left {
      display: flex;
      gap: 12px;
      align-items: center;
    }
    
    .filter-right {
      display: flex;
      gap: 8px;
    }
  }
  
  .tools-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    
    .tool-card {
      background: var(--bg-white);
      border: 1px solid var(--border-light);
      border-radius: 12px;
      padding: 20px;
      transition: all 0.3s;
      
      &:hover {
        box-shadow: var(--shadow-light);
        transform: translateY(-2px);
      }
      
      &.disabled {
        opacity: 0.6;
      }
      
      .tool-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
      }
      
      .tool-content {
        margin-bottom: 16px;
        
        .tool-name {
          font-size: 16px;
          font-weight: 600;
          color: var(--text-primary);
          margin: 0 0 8px 0;
        }
        
        .tool-description {
          font-size: 14px;
          color: var(--text-secondary);
          line-height: 1.4;
          margin: 0 0 12px 0;
        }
        
        .tool-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .tool-params {
            font-size: 12px;
            color: var(--text-placeholder);
          }
        }
      }
      
      .tool-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .empty-state {
    grid-column: 1 / -1;
    text-align: center;
    padding: 60px 20px;
    color: var(--text-secondary);
    
    p {
      margin-top: 16px;
      font-size: 16px;
    }
  }
  
  .tool-detail {
    .tool-description-section,
    .tool-parameters-section {
      margin-top: 24px;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 12px 0;
      }
      
      p {
        color: var(--text-secondary);
        line-height: 1.5;
      }
    }
  }
  
  .config-content {
    .form-help {
      font-size: 12px;
      color: var(--text-placeholder);
      margin-left: 8px;
    }
  }
  
  .test-content {
    .param-help {
      font-size: 12px;
      color: var(--text-placeholder);
      margin-top: 4px;
    }
    
    .test-result {
      margin-top: 24px;
      
      h4 {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }
      
      pre {
        background: var(--fill-lighter);
        border: 1px solid var(--border-light);
        border-radius: 6px;
        padding: 12px;
        font-size: 12px;
        max-height: 200px;
        overflow-y: auto;
      }
    }
  }
}

@media (max-width: 768px) {
  .tools {
    padding: 16px;
    
    .tools-filter {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
      
      .filter-left {
        flex-direction: column;
      }
    }
    
    .tools-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>