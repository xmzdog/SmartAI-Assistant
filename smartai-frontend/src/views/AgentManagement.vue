<template>
  <div class="agent-management">
    <div class="page-header">
      <h1 class="page-title">AI智能体管理</h1>
      <p class="page-description">管理和配置AI智能体，支持不同渠道和功能</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增智能体
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
        <el-table-column prop="agentId" label="智能体ID" width="150" />
        <el-table-column prop="agentName" label="智能体名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="channel" label="渠道类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getChannelTagType(row.channel)">
              {{ getChannelName(row.channel) }}
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
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editAgent(row)">
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="deleteAgent(row)">
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
      :title="editingAgent ? '编辑智能体' : '新增智能体'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="智能体ID" prop="agentId">
          <el-input
            v-model="formData.agentId"
            placeholder="请输入智能体ID"
            :disabled="!!editingAgent"
          />
        </el-form-item>
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
            <el-option label="Agent" value="agent" />
            <el-option label="聊天流" value="chat_stream" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAgent" :loading="saving">
            {{ editingAgent ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { agentApi } from '@/api/agent'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingAgent = ref(null)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref()

// 表单数据
const formData = reactive({
  agentId: '',
  agentName: '',
  description: '',
  channel: 'agent',
  status: 1
})

// 表单验证规则
const formRules = {
  agentId: [
    { required: true, message: '请输入智能体ID', trigger: 'blur' }
  ],
  agentName: [
    { required: true, message: '请输入智能体名称', trigger: 'blur' }
  ],
  channel: [
    { required: true, message: '请选择渠道类型', trigger: 'change' }
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
    
    const response = await agentApi.getAgentList(params)
    
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

const editAgent = (row) => {
  editingAgent.value = row
  Object.assign(formData, row)
  showAddDialog.value = true
}

const saveAgent = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    let response
    if (editingAgent.value) {
      response = await agentApi.updateAgent(editingAgent.value.id, formData)
      ElMessage.success('智能体更新成功')
    } else {
      response = await agentApi.createAgent(formData)
      ElMessage.success('智能体创建成功')
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
    await ElMessageBox.confirm(`确定要${action}该智能体吗？`, '操作确认')
    
    const newStatus = row.status === 1 ? 0 : 1
    const response = await agentApi.updateAgentStatus(row.id, newStatus)
    
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

const deleteAgent = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体 "${row.agentName}" 吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    
    const response = await agentApi.deleteAgent(row.id)
    
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
  editingAgent.value = null
  Object.assign(formData, {
    agentId: '',
    agentName: '',
    description: '',
    channel: 'agent',
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

const getChannelTagType = (channel) => {
  return channel === 'agent' ? 'primary' : 'success'
}

const getChannelName = (channel) => {
  const channelMap = {
    agent: 'Agent',
    chat_stream: '聊天流'
  }
  return channelMap[channel] || channel
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style lang="scss" scoped>
.agent-management {
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
}

@media (max-width: 768px) {
  .agent-management {
    padding: 16px;
    
    .action-bar {
      flex-direction: column;
    }
  }
}
</style>
