<template>
  <div class="agent-management page-container">
    <div class="page-header">
      <h1 class="page-title">智能体管理</h1>
      <p class="page-description">管理和配置AI智能体</p>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建智能体
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="智能体名称">
          <el-input 
            v-model="searchForm.agentName" 
            placeholder="请输入智能体名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="渠道类型">
          <el-select 
            v-model="searchForm.channel" 
            placeholder="请选择渠道类型"
            clearable
          >
            <el-option label="智能体" value="agent" />
            <el-option label="流式对话" value="chat_stream" />
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
        <el-table-column prop="agentName" label="智能体名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="channel" label="渠道类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.channel === 'agent' ? 'primary' : 'success'">
              {{ row.channel === 'agent' ? '智能体' : '流式对话' }}
            </el-tag>
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
            <el-button 
              type="warning" 
              size="small" 
              text 
              :icon="Setting"
              @click="handlePreheat(row)"
            >
              预热
            </el-button>
            <el-popconfirm
              title="确定要删除这个智能体吗？"
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
        <el-form-item label="智能体名称" prop="agentName">
          <el-input v-model="formData.agentName" placeholder="请输入智能体名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="渠道类型" prop="channel">
          <el-select v-model="formData.channel" placeholder="请选择渠道类型">
            <el-option label="智能体" value="agent" />
            <el-option label="流式对话" value="chat_stream" />
          </el-select>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Edit, Delete, Setting } from '@element-plus/icons-vue'
import { agentManageApi, agentApi } from '@/api/agent'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  agentName: '',
  channel: '',
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
  agentName: '',
  description: '',
  channel: '',
  status: 1
})

// 表单验证规则
const formRules = {
  agentName: [
    { required: true, message: '请输入智能体名称', trigger: 'blur' }
  ],
  channel: [
    { required: true, message: '请选择渠道类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑智能体' : '新建智能体')

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
    const response = await agentManageApi.queryAgentList(params)
    tableData.value = response || []
    // 注意：后端可能没有返回总数，这里需要根据实际情况调整
    pagination.total = response?.length || 0
  } catch (error) {
    console.error('查询智能体列表失败:', error)
    ElMessage.error('查询失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    agentName: '',
    channel: '',
    status: null
  })
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    agentName: '',
    description: '',
    channel: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleView = (row) => {
  router.push(`/agent/detail/${row.id}`)
}

const handlePreheat = async (row) => {
  try {
    await agentApi.preheat(row.id)
    ElMessage.success('智能体预热成功')
  } catch (error) {
    console.error('预热失败:', error)
    ElMessage.error('预热失败，请重试')
  }
}

const handleDelete = async (row) => {
  try {
    await agentManageApi.deleteAgent(row.id)
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
      await agentManageApi.updateAgent(formData)
      ElMessage.success('更新成功')
    } else {
      await agentManageApi.addAgent(formData)
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
.agent-management {
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
}
</style>
