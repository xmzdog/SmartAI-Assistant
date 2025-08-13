<template>
  <div class="agent-flow-config">
    <div class="page-header">
      <h1 class="page-title">流程配置</h1>
      <div class="actions">
        <el-input v-model="queryAgentId" placeholder="按智能体ID筛选" style="width: 220px" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="openEdit()">新增配置</el-button>
      </div>
    </div>

    <el-table :data="tableData" stripe style="width: 100%">
      <el-table-column prop="agentName" label="智能体" min-width="160" />
      <el-table-column prop="clientType" label="类型" width="140" />
      <el-table-column prop="clientName" label="名称" />
      <el-table-column prop="sequence" label="顺序" width="100" />
      <el-table-column prop="stepPrompt" label="阶段提示" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑配置' : '新增配置'" width="600px">
      <el-form :model="form" label-width="120px" :rules="formRules" ref="formRef">
        <el-form-item label="智能体" prop="agentId">
          <el-select v-model="form.agentId" placeholder="请选择智能体" style="width: 100%;">
            <el-option
              v-for="agent in agentOptions"
              :key="agent.id"
              :label="agent.agentName"
              :value="agent.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Client类型" prop="clientType">
          <el-input v-model="form.clientType" placeholder="请输入Client类型" />
        </el-form-item>
        <el-form-item label="Client名称" prop="clientName">
          <el-input v-model="form.clientName" placeholder="请输入Client名称" />
        </el-form-item>
        <el-form-item label="顺序" prop="sequence">
          <el-input-number v-model="form.sequence" :min="0" />
        </el-form-item>
        <el-form-item label="阶段提示" prop="stepPrompt">
          <el-input v-model="form.stepPrompt" type="textarea" :rows="3" placeholder="请输入阶段提示词" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
  
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { agentFlowConfigApi } from '@/api/system'
import { agentManageApi } from '@/api/agent'

const tableData = ref([])
const queryAgentId = ref('')
const visible = ref(false)
const saving = ref(false)
const formRef = ref()
const agentOptions = ref([])
const form = ref({
  id: '', agentId: '', clientType: '', clientName: '', sequence: 0, stepPrompt: ''
})

// 表单验证规则
const formRules = {
  agentId: [
    { required: true, message: '请选择智能体', trigger: 'change' }
  ],
  clientType: [
    { required: true, message: '请输入Client类型', trigger: 'blur' }
  ],
  clientName: [
    { required: true, message: '请输入Client名称', trigger: 'blur' }
  ],
  sequence: [
    { required: true, message: '请输入顺序', trigger: 'blur' }
  ]
}


const loadData = async () => {
  try {
    const params = queryAgentId.value ? { agentId: queryAgentId.value } : {}
    const res = await agentFlowConfigApi.list(params)
    
    const rows = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
    
    tableData.value = rows.map((item) => {
      const agentId = item.agentId ?? item.agent_id ?? item.agentID ?? ''
      const agentName = getAgentNameById(agentId)
      
      return {
        ...item,
        agentId: agentId,
        agentName: agentName,
        clientId: item.clientId ?? item.client_id ?? '',
        clientName: item.clientName ?? item.client_name ?? '',
        clientType: item.clientType ?? item.client_type ?? '',
        sequence: Number(item.sequence ?? 0),
        stepPrompt: item.stepPrompt ?? item.step_prompt ?? '',
        modelName: item.modelName ?? item.model_name ?? ''
      }
    })
    
  } catch (e) {
    console.error('加载失败:', e)
    ElMessage.error('加载失败: ' + (e.message || '未知错误'))
  }
}

const openEdit = (row) => {
  visible.value = true
  if (row) {
    // 编辑模式：复制现有数据
    form.value = { ...row }
  } else {
    // 新增模式：重置表单
    form.value = {
      id: '', 
      agentId: '', 
      clientType: '', 
      clientName: '', 
      sequence: 0, 
      stepPrompt: ''
    }
  }
  
  // 清除表单验证状态
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 根据智能体ID获取智能体名称
const getAgentNameById = (agentId) => {
  const agent = agentOptions.value.find(item => item.id == agentId)
  return agent ? agent.agentName : `Agent-${agentId}`
}

// 加载智能体列表
const loadAgentOptions = async () => {
  try {
    const response = await agentManageApi.queryAgentList()
    if (response.code === 200 && response.data && response.data.records) {
      agentOptions.value = response.data.records
    } else if (Array.isArray(response.data)) {
      agentOptions.value = response.data
    } else if (Array.isArray(response)) {
      agentOptions.value = response
    } else {
      agentOptions.value = []
    }
    console.log('智能体选项:', agentOptions.value)
  } catch (e) {
    console.error('加载智能体列表失败:', e)
    ElMessage.error('加载智能体列表失败')
    agentOptions.value = []
  }
}

const save = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    
    saving.value = true
    
    // 准备保存数据，如果后端需要 clientId，可以用一个默认值或生成一个
    const saveData = {
      ...form.value,
      clientId: form.value.clientId || 'auto-generated-' + Date.now() // 如果没有clientId，生成一个
    }
    
    if (form.value.id) {
      await agentFlowConfigApi.update(saveData)
      ElMessage.success('已更新')
    } else {
      await agentFlowConfigApi.add(saveData)
      ElMessage.success('已新增')
    }
    visible.value = false
    await loadData()
  } catch (e) {
    if (e !== 'validation failed') {
      console.error('保存失败:', e)
      ElMessage.error('保存失败: ' + (e.message || '未知错误'))
    }
  } finally {
    saving.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该配置吗？', '提示', { type: 'warning' })
    await agentFlowConfigApi.delete(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  // 并行加载智能体选项和流程配置数据
  await Promise.all([
    loadAgentOptions(),
    loadData()
  ])
})
</script>

<style scoped>
.agent-flow-config { padding: 16px; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.page-title { margin: 0; }
</style>


