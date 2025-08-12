<template>
  <div class="client-management">
    <div class="page-header">
      <h1 class="page-title">AI客户端管理</h1>
      <p class="page-description">管理AI客户端配置，包括API接口和认证信息</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增客户端
      </el-button>
      <el-button @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="clientId" label="客户端ID" width="150" />
        <el-table-column prop="clientName" label="客户端名称" width="200" />
        <el-table-column prop="clientType" label="客户端类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getClientTypeTagType(row.clientType)">
              {{ getClientTypeName(row.clientType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="基础URL" show-overflow-tooltip />
        <el-table-column prop="apiKey" label="API密钥" width="150">
          <template #default="{ row }">
            <span v-if="row.apiKey">
              {{ maskApiKey(row.apiKey) }}
            </span>
            <span v-else>-</span>
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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editClient(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="viewApis(row)">
              API配置
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="deleteClient(row)">
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
      :title="editingClient ? '编辑客户端' : '新增客户端'"
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
            <el-form-item label="客户端ID" prop="clientId">
              <el-input
                v-model="formData.clientId"
                placeholder="请输入客户端ID"
                :disabled="!!editingClient"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户端名称" prop="clientName">
              <el-input v-model="formData.clientName" placeholder="请输入客户端名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户端类型" prop="clientType">
              <el-select v-model="formData.clientType" placeholder="请选择客户端类型">
                <el-option label="OpenAI" value="openai" />
                <el-option label="Claude" value="claude" />
                <el-option label="通义千问" value="qwen" />
                <el-option label="文心一言" value="ernie" />
                <el-option label="其他" value="other" />
              </el-select>
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

        <el-form-item label="基础URL" prop="baseUrl">
          <el-input v-model="formData.baseUrl" placeholder="请输入API基础URL" />
        </el-form-item>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            placeholder="请输入API密钥"
            show-password
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>

        <el-form-item label="超时时间" prop="timeout">
          <el-input-number
            v-model="formData.timeout"
            :min="1"
            :max="300"
            placeholder="秒"
          />
          <span style="margin-left: 8px; color: var(--el-text-color-regular)">秒</span>
        </el-form-item>

        <el-form-item label="最大重试次数" prop="maxRetries">
          <el-input-number
            v-model="formData.maxRetries"
            :min="0"
            :max="10"
            placeholder="次"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveClient" :loading="saving">
            {{ editingClient ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- API配置对话框 -->
    <el-dialog
      v-model="showApiDialog"
      title="API配置管理"
      width="900px"
      destroy-on-close
    >
      <div class="api-management">
        <div class="api-header">
          <el-button type="primary" size="small" @click="addApi">
            <el-icon><Plus /></el-icon>
            新增API
          </el-button>
        </div>
        
        <el-table :data="apiList" stripe style="width: 100%">
          <el-table-column prop="apiName" label="API名称" width="150" />
          <el-table-column prop="apiPath" label="API路径" />
          <el-table-column prop="method" label="请求方法" width="100">
            <template #default="{ row }">
              <el-tag :type="getMethodTagType(row.method)">
                {{ row.method }}
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
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editApi(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteApi(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clientApi } from '@/api/client'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showApiDialog = ref(false)
const editingClient = ref(null)
const currentClient = ref(null)
const tableData = ref([])
const apiList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref()

// 表单数据
const formData = reactive({
  clientId: '',
  clientName: '',
  clientType: 'openai',
  baseUrl: '',
  apiKey: '',
  description: '',
  timeout: 30,
  maxRetries: 3,
  status: 1
})

// 表单验证规则
const formRules = {
  clientId: [
    { required: true, message: '请输入客户端ID', trigger: 'blur' }
  ],
  clientName: [
    { required: true, message: '请输入客户端名称', trigger: 'blur' }
  ],
  clientType: [
    { required: true, message: '请选择客户端类型', trigger: 'change' }
  ],
  baseUrl: [
    { required: true, message: '请输入基础URL', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL', trigger: 'blur' }
  ],
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  loadData()
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      search: searchForm.search,
      status: searchForm.status
    }
    
    const response = await clientApi.getClientList(params)
    
    if (response.code === 200) {
      tableData.value = response.data.records
      total.value = response.data.total
    } else {
      throw new Error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + error.message)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const editClient = (row) => {
  editingClient.value = row
  Object.assign(formData, row)
  showAddDialog.value = true
}

const saveClient = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    let response
    if (editingClient.value) {
      response = await clientApi.updateClient(editingClient.value.id, formData)
      ElMessage.success('客户端更新成功')
    } else {
      response = await clientApi.createClient(formData)
      ElMessage.success('客户端创建成功')
    }
    
    if (response.code !== 200) {
      throw new Error(response.message || '保存失败')
    }
    
    showAddDialog.value = false
    resetForm()
    loadData()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const viewApis = async (row) => {
  currentClient.value = row
  showApiDialog.value = true
  
  // TODO: 加载API配置数据
  // 模拟数据
  apiList.value = [
    {
      id: 1,
      apiName: 'chat_completions',
      apiPath: '/chat/completions',
      method: 'POST',
      status: 1
    },
    {
      id: 2,
      apiName: 'models',
      apiPath: '/models',
      method: 'GET',
      status: 1
    }
  ]
}

const addApi = () => {
  // TODO: 实现新增API功能
  ElMessage.info('新增API功能开发中')
}

const editApi = (row) => {
  // TODO: 实现编辑API功能
  ElMessage.info('编辑API功能开发中')
}

const deleteApi = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除API "${row.apiName}" 吗？`, '删除确认')
    // TODO: 调用删除API
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const toggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}该客户端吗？`, '操作确认')
    
    const newStatus = row.status === 1 ? 0 : 1
    const response = await clientApi.updateClientStatus(row.id, newStatus)
    
    if (response.code === 200) {
      row.status = newStatus
      ElMessage.success(`${action}成功`)
    } else {
      throw new Error(response.message || '状态切换失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error('状态切换失败: ' + error.message)
    }
  }
}

const deleteClient = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除客户端 "${row.clientName}" 吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    
    const response = await clientApi.deleteClient(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      throw new Error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const resetForm = () => {
  editingClient.value = null
  Object.assign(formData, {
    clientId: '',
    clientName: '',
    clientType: 'openai',
    baseUrl: '',
    apiKey: '',
    description: '',
    timeout: 30,
    maxRetries: 3,
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

const getClientTypeTagType = (type) => {
  const typeMap = {
    openai: 'primary',
    claude: 'success',
    qwen: 'warning',
    ernie: 'info',
    other: 'default'
  }
  return typeMap[type] || 'default'
}

const getClientTypeName = (type) => {
  const typeMap = {
    openai: 'OpenAI',
    claude: 'Claude',
    qwen: '通义千问',
    ernie: '文心一言',
    other: '其他'
  }
  return typeMap[type] || type
}

const getMethodTagType = (method) => {
  const methodMap = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger'
  }
  return methodMap[method] || 'default'
}

const maskApiKey = (apiKey) => {
  if (!apiKey) return ''
  if (apiKey.length <= 8) return '*'.repeat(apiKey.length)
  return apiKey.substring(0, 4) + '*'.repeat(apiKey.length - 8) + apiKey.substring(apiKey.length - 4)
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style lang="scss" scoped>
.client-management {
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
    gap: 12px;
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
  
  .api-management {
    .api-header {
      margin-bottom: 16px;
    }
  }
}

@media (max-width: 768px) {
  .client-management {
    padding: 16px;
    
    .action-bar {
      flex-direction: column;
    }
  }
}
</style>