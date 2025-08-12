<template>
  <div class="tool-management">
    <div class="page-header">
      <h1 class="page-title">MCP工具管理</h1>
      <p class="page-description">管理AI客户端的MCP工具配置</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增工具
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
        <el-table-column prop="toolName" label="工具名称" width="180" />
        <el-table-column prop="toolType" label="工具类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getToolTypeTagType(row.toolType)">
              {{ getToolTypeName(row.toolType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clientId" label="所属客户端" width="150">
          <template #default="{ row }">
            <el-tag type="info">{{ row.clientId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="author" label="作者" width="120" />
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
            <el-button type="primary" size="small" @click="editTool(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="viewConfig(row)">
              配置
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="deleteTool(row)">
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
      :title="editingTool ? '编辑工具' : '新增工具'"
      width="700px"
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
            <el-form-item label="工具名称" prop="toolName">
              <el-input
                v-model="formData.toolName"
                placeholder="请输入工具名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工具类型" prop="toolType">
              <el-select v-model="formData.toolType" placeholder="请选择工具类型">
                <el-option label="文件操作" value="file" />
                <el-option label="网络请求" value="http" />
                <el-option label="数据库" value="database" />
                <el-option label="系统命令" value="system" />
                <el-option label="代码执行" value="code" />
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
            <el-form-item label="版本" prop="version">
              <el-input v-model="formData.version" placeholder="请输入版本号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="formData.author" placeholder="请输入作者" />
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

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入工具描述"
          />
        </el-form-item>

        <el-form-item label="工具配置" prop="config">
          <el-input
            v-model="formData.config"
            type="textarea"
            :rows="6"
            placeholder="请输入JSON格式的工具配置"
          />
          <div class="form-tip">
            <el-text type="info" size="small">
              请输入有效的JSON格式配置信息
            </el-text>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveTool" :loading="saving">
            {{ editingTool ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 配置查看对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      title="工具配置详情"
      width="800px"
      destroy-on-close
    >
      <div class="config-viewer">
        <div class="config-header">
          <h3>{{ currentTool?.toolName }} 配置信息</h3>
          <el-button type="primary" size="small" @click="editConfig">
            编辑配置
          </el-button>
        </div>
        
        <el-card class="config-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工具名称">
              {{ currentTool?.toolName }}
            </el-descriptions-item>
            <el-descriptions-item label="工具类型">
              <el-tag :type="getToolTypeTagType(currentTool?.toolType)">
                {{ getToolTypeName(currentTool?.toolType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="版本">
              {{ currentTool?.version }}
            </el-descriptions-item>
            <el-descriptions-item label="作者">
              {{ currentTool?.author }}
            </el-descriptions-item>
            <el-descriptions-item label="状态" :span="2">
              <el-tag :type="currentTool?.status === 1 ? 'success' : 'danger'">
                {{ currentTool?.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="config-card">
          <template #header>
            <span>配置详情</span>
          </template>
          <pre class="config-json">{{ formatConfig(currentTool?.config) }}</pre>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toolApi } from '@/api/tool'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showConfigDialog = ref(false)
const editingTool = ref(null)
const currentTool = ref(null)
const tableData = ref([])
const clientList = ref([])
const selectedClientId = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref()

// 表单数据
const formData = reactive({
  toolName: '',
  toolType: 'file',
  clientId: '',
  description: '',
  version: '1.0.0',
  author: '',
  config: '',
  status: 1
})

// 表单验证规则
const formRules = {
  toolName: [
    { required: true, message: '请输入工具名称', trigger: 'blur' }
  ],
  toolType: [
    { required: true, message: '请选择工具类型', trigger: 'change' }
  ],
  clientId: [
    { required: true, message: '请选择客户端', trigger: 'change' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' }
  ],
  config: [
    { validator: validateConfig, trigger: 'blur' }
  ]
}

// 配置验证器
function validateConfig(rule, value, callback) {
  if (value && value.trim()) {
    try {
      JSON.parse(value)
      callback()
    } catch (error) {
      callback(new Error('请输入有效的JSON格式'))
    }
  } else {
    callback()
  }
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
        toolName: 'file_reader',
        toolType: 'file',
        clientId: 'openai_client_001',
        description: '文件读取工具，支持多种文件格式',
        version: '1.2.0',
        author: 'System',
        config: JSON.stringify({
          supportedFormats: ['txt', 'json', 'xml', 'csv'],
          maxFileSize: '10MB',
          encoding: 'utf-8'
        }, null, 2),
        status: 1,
        createTime: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        toolName: 'http_client',
        toolType: 'http',
        clientId: 'openai_client_001',
        description: 'HTTP请求工具，支持GET、POST等方法',
        version: '2.1.0',
        author: 'Developer',
        config: JSON.stringify({
          timeout: 30000,
          retries: 3,
          headers: {
            'User-Agent': 'MCP-Tool/2.1.0'
          }
        }, null, 2),
        status: 1,
        createTime: '2024-01-15 11:00:00'
      },
      {
        id: 3,
        toolName: 'database_query',
        toolType: 'database',
        clientId: 'claude_client_001',
        description: '数据库查询工具，支持MySQL、PostgreSQL',
        version: '1.0.5',
        author: 'DB Team',
        config: JSON.stringify({
          supportedDatabases: ['mysql', 'postgresql', 'sqlite'],
          connectionPool: {
            min: 1,
            max: 10
          },
          queryTimeout: 60000
        }, null, 2),
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

const editTool = (row) => {
  editingTool.value = row
  Object.assign(formData, row)
  showAddDialog.value = true
}

const viewConfig = (row) => {
  currentTool.value = row
  showConfigDialog.value = true
}

const editConfig = () => {
  showConfigDialog.value = false
  editTool(currentTool.value)
}

const saveTool = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingTool.value) {
      // TODO: 调用更新API
      ElMessage.success('工具更新成功')
    } else {
      // TODO: 调用创建API
      ElMessage.success('工具创建成功')
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
    await ElMessageBox.confirm(`确定要${action}该工具吗？`, '操作确认')
    
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

const deleteTool = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除工具 "${row.toolName}" 吗？此操作不可恢复。`,
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
  editingTool.value = null
  Object.assign(formData, {
    toolName: '',
    toolType: 'file',
    clientId: '',
    description: '',
    version: '1.0.0',
    author: '',
    config: '',
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

const getToolTypeTagType = (type) => {
  const typeMap = {
    file: 'primary',
    http: 'success',
    database: 'warning',
    system: 'danger',
    code: 'info',
    other: 'default'
  }
  return typeMap[type] || 'default'
}

const getToolTypeName = (type) => {
  const typeMap = {
    file: '文件操作',
    http: '网络请求',
    database: '数据库',
    system: '系统命令',
    code: '代码执行',
    other: '其他'
  }
  return typeMap[type] || type
}

const formatConfig = (config) => {
  if (!config) return '无配置信息'
  try {
    return JSON.stringify(JSON.parse(config), null, 2)
  } catch (error) {
    return config
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style lang="scss" scoped>
.tool-management {
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
  
  .config-viewer {
    .config-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        margin: 0;
        color: var(--el-text-color-primary);
      }
    }
    
    .config-card {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    .config-json {
      background: var(--el-bg-color-page);
      border: 1px solid var(--el-border-color);
      border-radius: 4px;
      padding: 16px;
      margin: 0;
      white-space: pre-wrap;
      word-break: break-all;
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      font-size: 13px;
      line-height: 1.5;
      color: var(--el-text-color-primary);
      max-height: 400px;
      overflow-y: auto;
    }
  }
}

@media (max-width: 768px) {
  .tool-management {
    padding: 16px;
    
    .action-bar {
      flex-direction: column;
      align-items: stretch;
      
      .el-select {
        width: 100% !important;
      }
    }
    
    .config-viewer {
      .config-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }
    }
  }
}
</style>
