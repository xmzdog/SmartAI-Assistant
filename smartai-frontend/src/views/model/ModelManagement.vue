<template>
  <div class="model-management page-container">
    <div class="page-header">
      <h1 class="page-title">模型管理</h1>
      <p class="page-description">管理AI模型配置和参数</p>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建模型
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="模型名称">
          <el-input 
            v-model="searchForm.modelName" 
            placeholder="请输入模型名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="模型类型">
          <el-select 
            v-model="searchForm.modelType" 
            placeholder="请选择模型类型"
            clearable
          >
            <el-option label="OpenAI" value="openai" />
            <el-option label="Azure OpenAI" value="azure" />
            <el-option label="Claude" value="claude" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="tableData"
        :loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="modelName" label="模型名称" min-width="150" />
        <el-table-column prop="modelType" label="模型类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getModelTypeTag(row.modelType)">
              {{ getModelTypeLabel(row.modelType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="基础URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="modelVersion" label="版本" width="100" />
        <el-table-column prop="timeout" label="超时时间(秒)" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              text 
              :icon="View"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              text 
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              text 
              :icon="Connection"
              @click="handleTest(row)"
            >
              测试
            </el-button>
            <el-popconfirm
              title="确定要删除这个模型配置吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button 
                  type="danger" 
                  size="small" 
                  text 
                  :icon="Delete"
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="formData.modelName" placeholder="请输入模型名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型类型" prop="modelType">
              <el-select v-model="formData.modelType" placeholder="请选择模型类型">
                <el-option label="OpenAI" value="openai" />
                <el-option label="Azure OpenAI" value="azure" />
                <el-option label="Claude" value="claude" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基础URL" prop="baseUrl">
              <el-input v-model="formData.baseUrl" placeholder="请输入基础URL" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型版本" prop="modelVersion">
              <el-input v-model="formData.modelVersion" placeholder="请输入模型版本" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input 
            v-model="formData.apiKey" 
            type="password" 
            placeholder="请输入API密钥"
            show-password
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="完成路径" prop="completionsPath">
              <el-input v-model="formData.completionsPath" placeholder="如: /v1/chat/completions" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="嵌入路径" prop="embeddingsPath">
              <el-input v-model="formData.embeddingsPath" placeholder="如: /v1/embeddings" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="超时时间(秒)" prop="timeout">
              <el-input-number 
                v-model="formData.timeout" 
                :min="1" 
                :max="300"
                placeholder="请输入超时时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="模型详情"
      width="600px"
    >
      <div class="model-detail">
        <div class="detail-item">
          <label>模型名称：</label>
          <span>{{ viewData?.modelName || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>模型类型：</label>
          <el-tag :type="getModelTypeTag(viewData?.modelType)">
            {{ getModelTypeLabel(viewData?.modelType) }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>基础URL：</label>
          <span>{{ viewData?.baseUrl || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>模型版本：</label>
          <span>{{ viewData?.modelVersion || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>完成路径：</label>
          <span>{{ viewData?.completionsPath || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>嵌入路径：</label>
          <span>{{ viewData?.embeddingsPath || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>超时时间：</label>
          <span>{{ viewData?.timeout || '-' }}秒</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <el-tag :type="viewData?.status === 1 ? 'success' : 'danger'">
            {{ viewData?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>创建时间：</label>
          <span>{{ formatDate(viewData?.createTime) }}</span>
        </div>
        <div class="detail-item">
          <label>更新时间：</label>
          <span>{{ formatDate(viewData?.updateTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, View, Edit, Delete, Connection } from '@element-plus/icons-vue'
import { modelApi } from '@/api/model'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const viewData = ref(null)

// 搜索表单
const searchForm = reactive({
  modelName: '',
  modelType: '',
  status: null
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 表单数据
const formData = reactive({
  id: null,
  modelName: '',
  baseUrl: '',
  apiKey: '',
  completionsPath: '/v1/chat/completions',
  embeddingsPath: '/v1/embeddings',
  modelType: 'openai',
  modelVersion: '',
  timeout: 30,
  status: 1
})

// 表单验证规则
const formRules = {
  modelName: [
    { required: true, message: '请输入模型名称', trigger: 'blur' }
  ],
  baseUrl: [
    { required: true, message: '请输入基础URL', trigger: 'blur' }
  ],
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur' }
  ],
  modelType: [
    { required: true, message: '请选择模型类型', trigger: 'change' }
  ],
  timeout: [
    { required: true, message: '请输入超时时间', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑模型' : '新建模型')

// 方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getModelTypeLabel = (type) => {
  const labels = {
    openai: 'OpenAI',
    azure: 'Azure OpenAI',
    claude: 'Claude',
    other: '其他'
  }
  return labels[type] || type
}

const getModelTypeTag = (type) => {
  const tags = {
    openai: 'primary',
    azure: 'success',
    claude: 'warning',
    other: 'info'
  }
  return tags[type] || 'info'
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.size
    }
    const response = await modelApi.queryModelList(params)
    // 后端直接返回数组数据
    if (Array.isArray(response)) {
      // 过滤搜索条件
      let filteredData = response
      if (searchForm.modelName) {
        filteredData = filteredData.filter(item => 
          item.modelName?.toLowerCase().includes(searchForm.modelName.toLowerCase())
        )
      }
      if (searchForm.modelType) {
        filteredData = filteredData.filter(item => item.modelType === searchForm.modelType)
      }
      if (searchForm.status !== null && searchForm.status !== undefined && searchForm.status !== '') {
        filteredData = filteredData.filter(item => item.status === searchForm.status)
      }
      
      // 设置总数
      pagination.total = filteredData.length
      
      // 前端分页
      const startIndex = (pagination.current - 1) * pagination.size
      const endIndex = startIndex + pagination.size
      tableData.value = filteredData.slice(startIndex, endIndex)
    } else {
      console.warn('返回数据格式不正确:', response)
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('查询模型列表失败:', error)
    ElMessage.error('查询失败，请重试')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    modelName: '',
    modelType: '',
    status: null
  })
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    modelName: '',
    baseUrl: '',
    apiKey: '',
    completionsPath: '/v1/chat/completions',
    embeddingsPath: '/v1/embeddings',
    modelType: 'openai',
    modelVersion: '',
    timeout: 30,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

const handleTest = async (row) => {
  ElMessage.info('模型连接测试功能开发中...')
}

const handleDelete = async (row) => {
  try {
    await modelApi.deleteModel(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (formData.id) {
      await modelApi.updateModel(formData)
      ElMessage.success('更新成功')
    } else {
      await modelApi.addModel(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    handleSearch()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(() => {
  handleSearch()
})
</script>

<style lang="scss" scoped>
.model-management {
  .search-section {
    background: var(--bg-white);
    padding: 20px;
    margin-bottom: 16px;
    border-radius: 8px;
    box-shadow: var(--shadow-light);
  }

  .table-section {
    background: var(--bg-white);
    padding: 20px;
    border-radius: 8px;
    box-shadow: var(--shadow-light);
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .model-detail {
    .detail-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      gap: 8px;

      label {
        font-weight: 500;
        color: var(--text-secondary);
        min-width: 100px;
      }

      span {
        color: var(--text-primary);
      }
    }
  }
}
</style>
