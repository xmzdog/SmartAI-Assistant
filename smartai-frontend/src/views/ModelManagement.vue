<template>
  <div class="model-management">
    <div class="page-header">
      <h1 class="page-title">AI模型管理</h1>
      <p class="page-description">管理AI客户端支持的模型配置</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增模型
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
        <el-table-column prop="modelId" label="模型ID" width="200" />
        <el-table-column prop="modelName" label="模型名称" width="200" />
        <el-table-column prop="clientId" label="所属客户端" width="150">
          <template #default="{ row }">
            <el-tag type="info">{{ row.clientId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelType" label="模型类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getModelTypeTagType(row.modelType)">
              {{ getModelTypeName(row.modelType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxTokens" label="最大Token" width="100" />
        <el-table-column prop="temperature" label="温度" width="80">
          <template #default="{ row }">
            {{ row.temperature || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="topP" label="Top-P" width="80">
          <template #default="{ row }">
            {{ row.topP || '-' }}
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editModel(row)">
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="deleteModel(row)">
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
      :title="editingModel ? '编辑模型' : '新增模型'"
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
            <el-form-item label="模型ID" prop="modelId">
              <el-input
                v-model="formData.modelId"
                placeholder="请输入模型ID"
                :disabled="!!editingModel"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="formData.modelName" placeholder="请输入模型名称" />
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
            <el-form-item label="模型类型" prop="modelType">
              <el-select v-model="formData.modelType" placeholder="请选择模型类型">
                <el-option label="文本生成" value="text_generation" />
                <el-option label="对话聊天" value="chat" />
                <el-option label="代码生成" value="code_generation" />
                <el-option label="图像生成" value="image_generation" />
                <el-option label="嵌入向量" value="embedding" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最大Token" prop="maxTokens">
              <el-input-number
                v-model="formData.maxTokens"
                :min="1"
                :max="200000"
                placeholder="最大Token数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="温度" prop="temperature">
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
            <el-form-item label="Top-P" prop="topP">
              <el-input-number
                v-model="formData.topP"
                :min="0"
                :max="1"
                :step="0.1"
                placeholder="0-1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模型描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="支持流式输出">
              <el-switch
                v-model="formData.supportStream"
                active-text="是"
                inactive-text="否"
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
          <el-button type="primary" @click="saveModel" :loading="saving">
            {{ editingModel ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { modelApi } from '@/api/model'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingModel = ref(null)
const tableData = ref([])
const clientList = ref([])
const selectedClientId = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref()

// 表单数据
const formData = reactive({
  modelId: '',
  modelName: '',
  clientId: '',
  modelType: 'chat',
  maxTokens: 4096,
  temperature: 0.7,
  topP: 0.9,
  description: '',
  supportStream: true,
  status: 1
})

// 表单验证规则
const formRules = {
  modelId: [
    { required: true, message: '请输入模型ID', trigger: 'blur' }
  ],
  modelName: [
    { required: true, message: '请输入模型名称', trigger: 'blur' }
  ],
  clientId: [
    { required: true, message: '请选择客户端', trigger: 'change' }
  ],
  modelType: [
    { required: true, message: '请选择模型类型', trigger: 'change' }
  ],
  maxTokens: [
    { required: true, message: '请输入最大Token数', trigger: 'blur' }
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
    const response = await modelApi.getClientList()
    if (response.code === 200) {
      clientList.value = response.data
    }
  } catch (error) {
    console.error('加载客户端列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      search: searchForm.search,
      status: searchForm.status,
      clientId: selectedClientId.value
    }
    
    const response = await modelApi.getModelList(params)
    
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

const handleClientChange = () => {
  loadData()
}

const editModel = (row) => {
  editingModel.value = row
  Object.assign(formData, row)
  showAddDialog.value = true
}

const saveModel = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    let response
    if (editingModel.value) {
      response = await modelApi.updateModel(editingModel.value.id, formData)
      ElMessage.success('模型更新成功')
    } else {
      response = await modelApi.createModel(formData)
      ElMessage.success('模型创建成功')
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

const toggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}该模型吗？`, '操作确认')
    
    const newStatus = row.status === 1 ? 0 : 1
    const response = await modelApi.updateModelStatus(row.id, newStatus)
    
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

const deleteModel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模型 "${row.modelName}" 吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    
    const response = await modelApi.deleteModel(row.id)
    
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
  editingModel.value = null
  Object.assign(formData, {
    modelId: '',
    modelName: '',
    clientId: '',
    modelType: 'chat',
    maxTokens: 4096,
    temperature: 0.7,
    topP: 0.9,
    description: '',
    supportStream: true,
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

const getModelTypeTagType = (type) => {
  const typeMap = {
    text_generation: 'primary',
    chat: 'success',
    code_generation: 'warning',
    image_generation: 'info',
    embedding: 'default'
  }
  return typeMap[type] || 'default'
}

const getModelTypeName = (type) => {
  const typeMap = {
    text_generation: '文本生成',
    chat: '对话聊天',
    code_generation: '代码生成',
    image_generation: '图像生成',
    embedding: '嵌入向量'
  }
  return typeMap[type] || type
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style lang="scss" scoped>
.model-management {
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
}

@media (max-width: 768px) {
  .model-management {
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
