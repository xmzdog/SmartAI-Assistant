<template>
  <div class="knowledge-base page-container">
    <div class="page-header">
      <h1 class="page-title">知识库管理</h1>
      <p class="page-description">管理RAG知识库和文档资源</p>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建知识库
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="知识库名称">
          <el-input 
            v-model="searchForm.ragName" 
            placeholder="请输入知识库名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="知识标签">
          <el-input 
            v-model="searchForm.knowledgeTag" 
            placeholder="请输入知识标签"
            clearable
            @keyup.enter="handleSearch"
          />
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
        <el-table-column prop="ragName" label="知识库名称" min-width="150" />
        <el-table-column prop="knowledgeTag" label="知识标签" min-width="120" />
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
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              text 
              :icon="Upload"
              @click="handleUpload(row)"
            >
              上传文件
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
            <el-popconfirm
              title="确定要删除这个知识库吗？"
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="知识库名称" prop="ragName">
          <el-input v-model="formData.ragName" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="知识标签" prop="knowledgeTag">
          <el-input v-model="formData.knowledgeTag" placeholder="请输入知识标签" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
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

    <!-- 文件上传对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传知识库文件"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          class="upload-dragger"
          drag
          :action="uploadAction"
          :data="uploadData"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :file-list="fileList"
          name="files"
          multiple
          accept=".txt,.pdf,.doc,.docx,.md"
        >
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 txt/pdf/doc/docx/md 格式文件，单个文件大小不超过 10MB
            </div>
          </template>
        </el-upload>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Upload, Edit, Delete } from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api/knowledge'
import { agentApi } from '@/api/agent'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const uploadRef = ref()
const fileList = ref([])
const currentRagOrder = ref(null)

// 搜索表单
const searchForm = reactive({
  ragName: '',
  knowledgeTag: '',
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
  ragName: '',
  knowledgeTag: '',
  status: 1
})

// 表单验证规则
const formRules = {
  ragName: [
    { required: true, message: '请输入知识库名称', trigger: 'blur' }
  ],
  knowledgeTag: [
    { required: true, message: '请输入知识标签', trigger: 'blur' }
  ]
}

// 上传配置
const uploadAction = '/api/v1/ai/agent/file/upload'
const uploadHeaders = {
  'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
}

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑知识库' : '新建知识库')

const uploadData = computed(() => ({
  name: currentRagOrder.value?.ragName || '',
  tag: currentRagOrder.value?.knowledgeTag || ''
}))

// 方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.size
    }
    const response = await knowledgeApi.queryRagOrderList(params)
    tableData.value = response || []
    pagination.total = response?.length || 0
  } catch (error) {
    console.error('查询知识库列表失败:', error)
    ElMessage.error('查询失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    ragName: '',
    knowledgeTag: '',
    status: null
  })
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    ragName: '',
    knowledgeTag: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleUpload = (row) => {
  currentRagOrder.value = row
  fileList.value = []
  uploadDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await knowledgeApi.deleteRagOrder(row.id)
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
      await knowledgeApi.updateRagOrder(formData)
      ElMessage.success('更新成功')
    } else {
      await knowledgeApi.addRagOrder(formData)
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

// 文件上传相关方法
const beforeUpload = (file) => {
  const isValidType = ['txt', 'pdf', 'doc', 'docx', 'md'].some(type => 
    file.name.toLowerCase().endsWith(`.${type}`)
  )
  if (!isValidType) {
    ElMessage.error('只支持 txt/pdf/doc/docx/md 格式文件')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }

  return true
}

const handleUploadSuccess = (response, file) => {
  ElMessage.success(`${file.name} 上传成功`)
  fileList.value = fileList.value.filter(item => item.uid !== file.uid)
}

const handleUploadError = (error, file) => {
  console.error('上传失败:', error)
  ElMessage.error(`${file.name} 上传失败`)
}

// 生命周期
onMounted(() => {
  handleSearch()
})
</script>

<style lang="scss" scoped>
.knowledge-base {
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

  .upload-section {
    .upload-dragger {
      width: 100%;
      
      :deep(.el-upload-dragger) {
        width: 100%;
        height: 180px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      }
    }
  }
}
</style>
