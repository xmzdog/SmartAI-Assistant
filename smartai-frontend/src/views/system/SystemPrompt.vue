<template>
  <div class="system-prompt page-container">
    <div class="page-header">
      <h1 class="page-title">系统提示词管理</h1>
      <p class="page-description">管理AI智能体的系统提示词模板</p>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建提示词
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="提示词名称">
          <el-input 
            v-model="searchForm.promptName" 
            placeholder="请输入提示词名称"
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
        <el-table-column prop="promptName" label="提示词名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="promptContent" label="提示词内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="prompt-content">
              {{ row.promptContent }}
            </div>
          </template>
        </el-table-column>
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
            <el-popconfirm
              title="确定要删除这个提示词吗？"
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
        label-width="100px"
      >
        <el-form-item label="提示词名称" prop="promptName">
          <el-input v-model="formData.promptName" placeholder="请输入提示词名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="提示词内容" prop="promptContent">
          <el-input
            v-model="formData.promptContent"
            type="textarea"
            :rows="10"
            placeholder="请输入提示词内容"
            show-word-limit
            maxlength="5000"
          />
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

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="提示词详情"
      width="800px"
    >
      <div class="prompt-detail">
        <div class="detail-item">
          <label>提示词名称：</label>
          <span>{{ viewData?.promptName || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>描述：</label>
          <span>{{ viewData?.description || '-' }}</span>
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
        <div class="detail-item content-item">
          <label>提示词内容：</label>
          <div class="content-display">
            <el-scrollbar height="300px">
              <pre>{{ viewData?.promptContent || '-' }}</pre>
            </el-scrollbar>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, View, Edit, Delete } from '@element-plus/icons-vue'
import { systemPromptApi } from '@/api/system'

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
  promptName: '',
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
  promptName: '',
  description: '',
  promptContent: '',
  status: 1
})

// 表单验证规则
const formRules = {
  promptName: [
    { required: true, message: '请输入提示词名称', trigger: 'blur' }
  ],
  promptContent: [
    { required: true, message: '请输入提示词内容', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑提示词' : '新建提示词')

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
    const response = await systemPromptApi.querySystemPromptList(params)
    tableData.value = response || []
    pagination.total = response?.length || 0
  } catch (error) {
    console.error('查询系统提示词列表失败:', error)
    ElMessage.error('查询失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    promptName: '',
    status: null
  })
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    promptName: '',
    description: '',
    promptContent: '',
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

const handleDelete = async (row) => {
  try {
    await systemPromptApi.deleteSystemPrompt(row.id)
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
      await systemPromptApi.updateSystemPrompt(formData)
      ElMessage.success('更新成功')
    } else {
      await systemPromptApi.addSystemPrompt(formData)
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
.system-prompt {
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

    .prompt-content {
      max-width: 300px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .prompt-detail {
    .detail-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 12px;
      gap: 8px;

      &.content-item {
        flex-direction: column;
        align-items: stretch;
      }

      label {
        font-weight: 500;
        color: var(--text-secondary);
        min-width: 100px;
      }

      span {
        color: var(--text-primary);
      }

      .content-display {
        margin-top: 8px;
        border: 1px solid var(--border-light);
        border-radius: 4px;
        background: var(--bg-page);

        pre {
          margin: 0;
          padding: 12px;
          font-family: 'Courier New', monospace;
          font-size: 14px;
          line-height: 1.5;
          white-space: pre-wrap;
          word-wrap: break-word;
        }
      }
    }
  }
}
</style>
