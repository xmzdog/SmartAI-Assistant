<template>
  <div class="advisor-management">
    <div class="page-header">
      <h1 class="page-title">顾问配置管理</h1>
      <p class="page-description">管理AI客户端的顾问配置，定制专业领域的AI助手</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增顾问
      </el-button>
      <el-button @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
      <el-select
        v-model="selectedClientId"
        placeholder="筛选客户端"
        clearable
        style="width: 200px; margin-left: 12px;"
        @change="handleClientChange"
      >
        <el-option
          v-for="client in clientList"
          :key="client.clientId"
          :label="client.clientName"
          :value="client.clientId"
        />
      </el-select>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="advisorName" label="顾问名称" width="200" />
        <el-table-column prop="advisorType" label="顾问类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAdvisorTypeTagType(row.advisorType)">
              {{ getAdvisorTypeName(row.advisorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clientId" label="所属客户端" width="150">
          <template #default="{ row }">
            <el-tag type="info">{{ row.clientId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expertise" label="专业领域" show-overflow-tooltip />
        <el-table-column prop="language" label="语言" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.language }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editAdvisor(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="viewConfig(row)">
              详情
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="deleteAdvisor(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingAdvisor ? '编辑顾问配置' : '新增顾问配置'"
      width="900px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="顾问名称" prop="advisorName">
              <el-input
                v-model="formData.advisorName"
                placeholder="请输入顾问名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="顾问类型" prop="advisorType">
              <el-select v-model="formData.advisorType" placeholder="请选择顾问类型">
                <el-option label="技术专家" value="technical" />
                <el-option label="业务顾问" value="business" />
                <el-option label="教育导师" value="education" />
                <el-option label="创意助手" value="creative" />
                <el-option label="生活助手" value="lifestyle" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属客户端" prop="clientId">
              <el-select v-model="formData.clientId" placeholder="请选择客户端">
                <el-option
                  v-for="client in clientList"
                  :key="client.clientId"
                  :label="client.clientName"
                  :value="client.clientId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="语言" prop="language">
              <el-select v-model="formData.language" placeholder="请选择语言">
                <el-option label="中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
                <el-option label="日本語" value="ja-JP" />
                <el-option label="한국어" value="ko-KR" />
                <el-option label="Français" value="fr-FR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="专业领域" prop="expertise">
          <el-input
            v-model="formData.expertise"
            placeholder="请输入专业领域，如：软件开发、数据分析、市场营销等"
          />
        </el-form-item>

        <el-form-item label="顾问描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入顾问的详细描述"
          />
        </el-form-item>

        <el-form-item label="系统指令" prop="systemPrompt">
          <el-input
            v-model="formData.systemPrompt"
            type="textarea"
            :rows="8"
            placeholder="请输入系统指令，定义顾问的行为和回答风格"
            show-word-limit
            maxlength="5000"
          />
          <div class="form-tip">
            <el-text type="info" size="small">
              系统指令将指导AI如何扮演这个顾问角色，包括专业知识、回答风格等
            </el-text>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="温度参数" prop="temperature">
              <el-input-number
                v-model="formData.temperature"
                :min="0"
                :max="2"
                :step="0.1"
                placeholder="0-2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大Token" prop="maxTokens">
              <el-input-number
                v-model="formData.maxTokens"
                :min="100"
                :max="8192"
                placeholder="最大输出长度"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="formData.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="知识库配置">
          <el-input
            v-model="formData.knowledgeBase"
            placeholder="可选，关联的知识库配置"
          />
        </el-form-item>

        <el-form-item label="工具权限">
          <el-checkbox-group v-model="formData.allowedTools">
            <el-checkbox label="file_operations">文件操作</el-checkbox>
            <el-checkbox label="web_search">网络搜索</el-checkbox>
            <el-checkbox label="code_execution">代码执行</el-checkbox>
            <el-checkbox label="data_analysis">数据分析</el-checkbox>
            <el-checkbox label="image_generation">图像生成</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="info" @click="testAdvisor" :loading="testing">
            测试配置
          </el-button>
          <el-button type="primary" @click="saveAdvisor" :loading="saving">
            {{ editingAdvisor ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情查看对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="顾问配置详情"
      width="900px"
      destroy-on-close
    >
      <div class="detail-container">
        <el-card class="detail-card">
          <template #header>
            <div class="detail-header">
              <span>{{ currentAdvisor?.advisorName }}</span>
              <el-tag :type="getAdvisorTypeTagType(currentAdvisor?.advisorType)">
                {{ getAdvisorTypeName(currentAdvisor?.advisorType) }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border class="detail-info">
            <el-descriptions-item label="所属客户端">
              {{ currentAdvisor?.clientId }}
            </el-descriptions-item>
            <el-descriptions-item label="语言">
              {{ currentAdvisor?.language }}
            </el-descriptions-item>
            <el-descriptions-item label="专业领域" :span="2">
              {{ currentAdvisor?.expertise }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ currentAdvisor?.description }}
            </el-descriptions-item>
            <el-descriptions-item label="温度参数">
              {{ currentAdvisor?.temperature }}
            </el-descriptions-item>
            <el-descriptions-item label="最大Token">
              {{ currentAdvisor?.maxTokens }}
            </el-descriptions-item>
            <el-descriptions-item label="知识库配置" :span="2">
              {{ currentAdvisor?.knowledgeBase || '无' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="detail-section">
            <h4>系统指令：</h4>
            <div class="system-prompt">
              {{ currentAdvisor?.systemPrompt }}
            </div>
          </div>

          <div class="detail-section" v-if="currentAdvisor?.allowedTools?.length">
            <h4>工具权限：</h4>
            <div class="tools-list">
              <el-tag
                v-for="tool in currentAdvisor.allowedTools"
                :key="tool"
                type="success"
                size="small"
                style="margin-right: 8px; margin-bottom: 8px;"
              >
                {{ getToolName(tool) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { advisorApi } from '@/api/advisor'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const editingAdvisor = ref(null)
const currentAdvisor = ref(null)
const tableData = ref([])
const clientList = ref([])
const selectedClientId = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref()

// 表单数据
const formData = reactive({
  advisorName: '',
  advisorType: 'technical',
  clientId: '',
  expertise: '',
  language: 'zh-CN',
  description: '',
  systemPrompt: '',
  temperature: 0.7,
  maxTokens: 2048,
  knowledgeBase: '',
  allowedTools: [],
  status: 1
})

// 表单验证规则
const formRules = {
  advisorName: [
    { required: true, message: '请输入顾问名称', trigger: 'blur' }
  ],
  advisorType: [
    { required: true, message: '请选择顾问类型', trigger: 'change' }
  ],
  clientId: [
    { required: true, message: '请选择客户端', trigger: 'change' }
  ],
  expertise: [
    { required: true, message: '请输入专业领域', trigger: 'blur' }
  ],
  language: [
    { required: true, message: '请选择语言', trigger: 'change' }
  ],
  systemPrompt: [
    { required: true, message: '请输入系统指令', trigger: 'blur' },
    { min: 20, message: '系统指令至少20个字符', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadClientList()
  loadData()
})

// 方法
const loadClientList = async () => {
  try {
    // TODO: 调用后端API获取客户端列表
    // 模拟数据
    clientList.value = [
      { clientId: 'openai_client_001', clientName: 'OpenAI GPT客户端' },
      { clientId: 'claude_client_001', clientName: 'Claude API客户端' }
    ]
  } catch (error) {
    console.error('加载客户端列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取数据
    // 模拟数据
    const mockData = [
      {
        id: 1,
        advisorName: '软件架构师',
        advisorType: 'technical',
        clientId: 'openai_client_001',
        expertise: '软件架构设计、系统设计、技术选型',
        language: 'zh-CN',
        description: '专业的软件架构师，擅长大型系统设计和技术架构规划',
        systemPrompt: '你是一位经验丰富的软件架构师，具有15年以上的系统设计经验。你擅长分析业务需求，设计可扩展、高可用的系统架构。请用专业但易懂的语言回答问题，并提供具体的技术建议和最佳实践。',
        temperature: 0.6,
        maxTokens: 3000,
        knowledgeBase: 'tech_architecture_kb',
        allowedTools: ['file_operations', 'code_execution', 'data_analysis'],
        status: 1,
        createTime: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        advisorName: '市场营销专家',
        advisorType: 'business',
        clientId: 'openai_client_001',
        expertise: '数字营销、品牌策略、市场分析',
        language: 'zh-CN',
        description: '资深市场营销专家，专注于数字化营销策略和品牌建设',
        systemPrompt: '你是一位资深的市场营销专家，具有丰富的数字营销和品牌策略经验。你能够分析市场趋势，制定有效的营销策略，并提供实用的营销建议。请用专业而生动的语言回答问题。',
        temperature: 0.8,
        maxTokens: 2500,
        knowledgeBase: 'marketing_kb',
        allowedTools: ['web_search', 'data_analysis'],
        status: 1,
        createTime: '2024-01-15 11:00:00'
      },
      {
        id: 3,
        advisorName: '创意写作导师',
        advisorType: 'creative',
        clientId: 'claude_client_001',
        expertise: '创意写作、文学创作、内容策划',
        language: 'zh-CN',
        description: '专业的创意写作导师，帮助提升写作技巧和创作灵感',
        systemPrompt: '你是一位富有创意的写作导师，具有深厚的文学功底和丰富的创作经验。你能够激发创作灵感，提供写作技巧指导，帮助改善文章结构和表达方式。请用富有启发性的语言回答问题。',
        temperature: 0.9,
        maxTokens: 2000,
        knowledgeBase: 'writing_kb',
        allowedTools: ['web_search', 'image_generation'],
        status: 1,
        createTime: '2024-01-16 14:20:00'
      }
    ]
    
    // 根据客户端筛选
    let filteredData = mockData
    if (selectedClientId.value) {
      filteredData = mockData.filter(item => item.clientId === selectedClientId.value)
    }
    
    tableData.value = filteredData
    total.value = filteredData.length
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const handleClientChange = () => {
  loadData()
}

const editAdvisor = (row) => {
  editingAdvisor.value = row
  Object.assign(formData, row)
  showAddDialog.value = true
}

const viewConfig = (row) => {
  currentAdvisor.value = row
  showDetailDialog.value = true
}

const testAdvisor = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  testing.value = true
  try {
    // TODO: 调用测试API
    await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟测试
    ElMessage.success('顾问配置测试通过')
  } catch (error) {
    console.error('测试失败:', error)
    ElMessage.error('测试失败，请检查配置')
  } finally {
    testing.value = false
  }
}

const saveAdvisor = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingAdvisor.value) {
      // TODO: 调用更新API
      ElMessage.success('顾问配置更新成功')
    } else {
      // TODO: 调用创建API
      ElMessage.success('顾问配置创建成功')
    }
    
    showAddDialog.value = false
    resetForm()
    loadData()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}该顾问配置吗？`, '操作确认')
    
    // TODO: 调用状态切换API
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error('状态切换失败')
    }
  }
}

const deleteAdvisor = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除顾问配置 "${row.advisorName}" 吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    
    // TODO: 调用删除API
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  editingAdvisor.value = null
  Object.assign(formData, {
    advisorName: '',
    advisorType: 'technical',
    clientId: '',
    expertise: '',
    language: 'zh-CN',
    description: '',
    systemPrompt: '',
    temperature: 0.7,
    maxTokens: 2048,
    knowledgeBase: '',
    allowedTools: [],
    status: 1
  })
  formRef.value?.resetFields()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadData()
}

const getAdvisorTypeTagType = (type) => {
  const typeMap = {
    technical: 'primary',
    business: 'success',
    education: 'warning',
    creative: 'info',
    lifestyle: 'default',
    other: 'default'
  }
  return typeMap[type] || 'default'
}

const getAdvisorTypeName = (type) => {
  const typeMap = {
    technical: '技术专家',
    business: '业务顾问',
    education: '教育导师',
    creative: '创意助手',
    lifestyle: '生活助手',
    other: '其他'
  }
  return typeMap[type] || type
}

const getToolName = (tool) => {
  const toolMap = {
    file_operations: '文件操作',
    web_search: '网络搜索',
    code_execution: '代码执行',
    data_analysis: '数据分析',
    image_generation: '图像生成'
  }
  return toolMap[tool] || tool
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style lang="scss" scoped>
.advisor-management {
  padding: 24px;
  
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin: 0 0 8px 0;
    }
    
    .page-description {
      color: var(--el-text-color-regular);
      margin: 0;
    }
  }
  
  .action-bar {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .table-card {
    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: center;
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
  
  .form-tip {
    margin-top: 4px;
  }
  
  .detail-container {
    .detail-card {
      .detail-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .detail-info {
        margin-bottom: 20px;
      }
      
      .detail-section {
        margin-bottom: 20px;
        
        h4 {
          margin: 0 0 12px 0;
          color: var(--el-text-color-primary);
        }
        
        .system-prompt {
          background: var(--el-bg-color-page);
          border: 1px solid var(--el-border-color);
          border-radius: 4px;
          padding: 16px;
          white-space: pre-wrap;
          word-break: break-word;
          font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          font-size: 14px;
          line-height: 1.6;
          color: var(--el-text-color-primary);
          max-height: 300px;
          overflow-y: auto;
        }
        
        .tools-list {
          display: flex;
          flex-wrap: wrap;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .advisor-management {
    padding: 16px;
    
    .action-bar {
      flex-direction: column;
      align-items: stretch;
      
      .el-select {
        width: 100% !important;
      }
    }
    
    .detail-container {
      .detail-card {
        .detail-header {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;
        }
      }
    }
  }
}
</style>
