<template>
  <div class="advisor-config page-container">
    <div class="page-header">
      <h1 class="page-title">顾问配置管理</h1>
      <p class="page-description">管理智能体顾问配置和参数</p>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建顾问配置
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="顾问名称">
          <el-input 
            v-model="searchForm.advisorName" 
            placeholder="请输入顾问名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="顾问类型">
          <el-select 
            v-model="searchForm.advisorType" 
            placeholder="请选择顾问类型"
            clearable
          >
            <el-option label="提示词聊天记忆" value="PromptChatMemory" />
            <el-option label="RAG回答" value="RagAnswer" />
            <el-option label="简单日志顾问" value="SimpleLoggerAdvisor" />
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
        <el-table-column prop="advisorName" label="顾问名称" min-width="150" />
        <el-table-column prop="advisorType" label="顾问类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getAdvisorTypeTag(row.advisorType)">
              {{ getAdvisorTypeLabel(row.advisorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="顺序号" width="100" />
        <el-table-column prop="extParam" label="扩展参数" min-width="200" show-overflow-tooltip />
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
              title="确定要删除这个顾问配置吗？"
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="顾问名称" prop="advisorName">
          <el-input v-model="formData.advisorName" placeholder="请输入顾问名称" />
        </el-form-item>
        <el-form-item label="顾问类型" prop="advisorType">
          <el-select v-model="formData.advisorType" placeholder="请选择顾问类型">
            <el-option label="提示词聊天记忆" value="PromptChatMemory" />
            <el-option label="RAG回答" value="RagAnswer" />
            <el-option label="简单日志顾问" value="SimpleLoggerAdvisor" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="顺序号" prop="orderNum">
          <el-input-number 
            v-model="formData.orderNum" 
            :min="1" 
            :max="999"
            placeholder="请输入顺序号"
          />
        </el-form-item>
        <el-form-item label="扩展参数" prop="extParam">
          <el-input
            v-model="formData.extParam"
            type="textarea"
            :rows="6"
            placeholder="请输入JSON格式的扩展参数"
            show-word-limit
            maxlength="2000"
          />
          <div class="param-tip">
            <el-text type="info" size="small">
              请输入有效的JSON格式参数，例如：{"key": "value"}
            </el-text>
          </div>
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
      title="顾问配置详情"
      width="700px"
    >
      <div class="advisor-detail">
        <div class="detail-item">
          <label>顾问名称：</label>
          <span>{{ viewData?.advisorName || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>顾问类型：</label>
          <el-tag :type="getAdvisorTypeTag(viewData?.advisorType)">
            {{ getAdvisorTypeLabel(viewData?.advisorType) }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>顺序号：</label>
          <span>{{ viewData?.orderNum || '-' }}</span>
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
        <div class="detail-item param-item">
          <label>扩展参数：</label>
          <div class="param-display">
            <el-scrollbar height="200px">
              <pre>{{ formatJson(viewData?.extParam) }}</pre>
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
import { advisorApi } from '@/api/system'

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
  advisorName: '',
  advisorType: '',
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
  advisorName: '',
  advisorType: '',
  orderNum: 1,
  extParam: '',
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
  orderNum: [
    { required: true, message: '请输入顺序号', trigger: 'blur' }
  ],
  extParam: [
    { validator: validateJson, trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑顾问配置' : '新建顾问配置')

// 方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getAdvisorTypeLabel = (type) => {
  const labels = {
    PromptChatMemory: '提示词聊天记忆',
    RagAnswer: 'RAG回答',
    SimpleLoggerAdvisor: '简单日志顾问',
    other: '其他'
  }
  return labels[type] || type
}

const getAdvisorTypeTag = (type) => {
  const tags = {
    PromptChatMemory: 'primary',
    RagAnswer: 'success',
    SimpleLoggerAdvisor: 'warning',
    other: 'info'
  }
  return tags[type] || 'info'
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return '-'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 自定义验证器
function validateJson(rule, value, callback) {
  if (!value) {
    callback()
    return
  }
  try {
    JSON.parse(value)
    callback()
  } catch (e) {
    callback(new Error('请输入有效的JSON格式'))
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.size
    }
    const response = await advisorApi.queryAdvisorList(params)
    tableData.value = response || []
    pagination.total = response?.length || 0
  } catch (error) {
    console.error('查询顾问配置列表失败:', error)
    ElMessage.error('查询失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    advisorName: '',
    advisorType: '',
    status: null
  })
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    advisorName: '',
    advisorType: '',
    orderNum: 1,
    extParam: '',
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
    await advisorApi.deleteAdvisor(row.id)
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
      await advisorApi.updateAdvisor(formData)
      ElMessage.success('更新成功')
    } else {
      await advisorApi.addAdvisor(formData)
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
.advisor-config {
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

  .param-tip {
    margin-top: 4px;
  }

  .advisor-detail {
    .detail-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 12px;
      gap: 8px;

      &.param-item {
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

      .param-display {
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
