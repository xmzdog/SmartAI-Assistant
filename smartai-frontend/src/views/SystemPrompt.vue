<template>
  <div class="system-prompt">
    <div class="page-header">
      <h1 class="page-title">系统提示管理</h1>
      <p class="page-description">管理AI客户端的系统提示配置，优化AI响应效果</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增提示
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
        <el-table-column prop="promptName" label="提示名称" width="200" />
        <el-table-column prop="promptType" label="提示类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getPromptTypeTagType(row.promptType)">
              {{ getPromptTypeName(row.promptType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clientId" label="所属客户端" width="150">
          <template #default="{ row }">
            <el-tag type="info">{{ row.clientId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editPrompt(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="previewPrompt(row)">
              预览
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="deletePrompt(row)">
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
      :title="editingPrompt ? '编辑系统提示' : '新增系统提示'"
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
            <el-form-item label="提示名称" prop="promptName">
              <el-input
                v-model="formData.promptName"
                placeholder="请输入提示名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提示类型" prop="promptType">
              <el-select v-model="formData.promptType" placeholder="请选择提示类型">
                <el-option label="系统角色" value="system_role" />
                <el-option label="任务指令" value="task_instruction" />
                <el-option label="格式约束" value="format_constraint" />
                <el-option label="安全规则" value="safety_rule" />
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
            <el-form-item label="优先级" prop="priority">
              <el-input-number
                v-model="formData.priority"
                :min="1"
                :max="100"
                placeholder="1-100"
                style="width: 100%"
              />
              <div class="form-tip">
                <el-text type="info" size="small">
                  数值越大优先级越高
                </el-text>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="请输入提示描述"
          />
        </el-form-item>

        <el-form-item label="系统提示内容" prop="promptContent">
          <el-input
            v-model="formData.promptContent"
            type="textarea"
            :rows="12"
            placeholder="请输入系统提示内容"
            show-word-limit
            maxlength="10000"
          />
          <div class="form-tip">
            <el-text type="info" size="small">
              支持变量替换，使用 {变量名} 格式，如 {user_name}、{current_time} 等
            </el-text>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用条件">
              <el-input
                v-model="formData.enableCondition"
                placeholder="可选，如特定场景或用户类型"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="info" @click="previewCurrentPrompt">预览</el-button>
          <el-button type="primary" @click="savePrompt" :loading="saving">
            {{ editingPrompt ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="showPreviewDialog"
      title="系统提示预览"
      width="800px"
      destroy-on-close
    >
      <div class="preview-container">
        <el-card class="preview-card">
          <template #header>
            <div class="preview-header">
              <span>{{ previewPrompt?.promptName }}</span>
              <el-tag :type="getPromptTypeTagType(previewPrompt?.promptType)">
                {{ getPromptTypeName(previewPrompt?.promptType) }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border class="preview-info">
            <el-descriptions-item label="所属客户端">
              {{ previewPrompt?.clientId }}
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityTagType(previewPrompt?.priority)">
                {{ previewPrompt?.priority }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ previewPrompt?.description || '无描述' }}
            </el-descriptions-item>
            <el-descriptions-item label="启用条件" :span="2">
              {{ previewPrompt?.enableCondition || '无特殊条件' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="preview-content">
            <h4>提示内容：</h4>
            <div class="prompt-content">
              {{ previewPrompt?.promptContent }}
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
import { Plus, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showPreviewDialog = ref(false)
const editingPrompt = ref(null)
const previewPrompt = ref(null)
const tableData = ref([])
const clientList = ref([])
const selectedClientId = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref()

// 表单数据
const formData = reactive({
  promptName: '',
  promptType: 'system_role',
  clientId: '',
  description: '',
  promptContent: '',
  priority: 10,
  enableCondition: '',
  status: 1
})

// 表单验证规则
const formRules = {
  promptName: [
    { required: true, message: '请输入提示名称', trigger: 'blur' }
  ],
  promptType: [
    { required: true, message: '请选择提示类型', trigger: 'change' }
  ],
  clientId: [
    { required: true, message: '请选择客户端', trigger: 'change' }
  ],
  promptContent: [
    { required: true, message: '请输入系统提示内容', trigger: 'blur' },
    { min: 10, message: '提示内容至少10个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' }
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
        promptName: '专业助手角色',
        promptType: 'system_role',
        clientId: 'openai_client_001',
        description: '设定AI为专业的技术助手角色',
        promptContent: '你是一个专业的AI技术助手，具有丰富的编程和技术知识。你的回答应该准确、详细且易于理解。请始终保持专业和友好的态度，并在适当的时候提供代码示例或实际解决方案。',
        priority: 90,
        enableCondition: '',
        status: 1,
        createTime: '2024-01-15 10:30:00',
        updateTime: '2024-01-20 15:45:00'
      },
      {
        id: 2,
        promptName: '代码格式约束',
        promptType: 'format_constraint',
        clientId: 'openai_client_001',
        description: '约束代码输出格式',
        promptContent: '当提供代码示例时，请遵循以下格式：\n1. 使用适当的代码块标记\n2. 添加必要的注释\n3. 确保代码的可读性和最佳实践\n4. 如果代码较长，请分段解释',
        priority: 80,
        enableCondition: '编程相关问题',
        status: 1,
        createTime: '2024-01-15 11:00:00',
        updateTime: '2024-01-18 09:30:00'
      },
      {
        id: 3,
        promptName: '安全响应规则',
        promptType: 'safety_rule',
        clientId: 'claude_client_001',
        description: '确保AI响应的安全性和合规性',
        promptContent: '请始终遵循以下安全原则：\n1. 不提供可能造成伤害的信息\n2. 不协助进行非法活动\n3. 保护用户隐私和敏感信息\n4. 拒绝生成有害、歧视性或不当内容\n5. 如遇到不确定的请求，请寻求澄清',
        priority: 100,
        enableCondition: '',
        status: 1,
        createTime: '2024-01-16 14:20:00',
        updateTime: '2024-01-19 16:10:00'
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

const editPrompt = (row) => {
  editingPrompt.value = row
  Object.assign(formData, row)
  showAddDialog.value = true
}

const previewPrompt = (row) => {
  previewPrompt.value = row
  showPreviewDialog.value = true
}

const previewCurrentPrompt = () => {
  previewPrompt.value = { ...formData }
  showPreviewDialog.value = true
}

const savePrompt = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingPrompt.value) {
      // TODO: 调用更新API
      ElMessage.success('系统提示更新成功')
    } else {
      // TODO: 调用创建API
      ElMessage.success('系统提示创建成功')
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
    await ElMessageBox.confirm(`确定要${action}该系统提示吗？`, '操作确认')
    
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

const deletePrompt = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除系统提示 "${row.promptName}" 吗？此操作不可恢复。`,
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
  editingPrompt.value = null
  Object.assign(formData, {
    promptName: '',
    promptType: 'system_role',
    clientId: '',
    description: '',
    promptContent: '',
    priority: 10,
    enableCondition: '',
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

const getPromptTypeTagType = (type) => {
  const typeMap = {
    system_role: 'primary',
    task_instruction: 'success',
    format_constraint: 'warning',
    safety_rule: 'danger',
    other: 'info'
  }
  return typeMap[type] || 'default'
}

const getPromptTypeName = (type) => {
  const typeMap = {
    system_role: '系统角色',
    task_instruction: '任务指令',
    format_constraint: '格式约束',
    safety_rule: '安全规则',
    other: '其他'
  }
  return typeMap[type] || type
}

const getPriorityTagType = (priority) => {
  if (priority >= 80) return 'danger'
  if (priority >= 50) return 'warning'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style lang="scss" scoped>
.system-prompt {
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
  
  .preview-container {
    .preview-card {
      .preview-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .preview-info {
        margin-bottom: 20px;
      }
      
      .preview-content {
        h4 {
          margin: 0 0 12px 0;
          color: var(--el-text-color-primary);
        }
        
        .prompt-content {
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
          max-height: 400px;
          overflow-y: auto;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .system-prompt {
    padding: 16px;
    
    .action-bar {
      flex-direction: column;
      align-items: stretch;
      
      .el-select {
        width: 100% !important;
      }
    }
  }
}
</style>
