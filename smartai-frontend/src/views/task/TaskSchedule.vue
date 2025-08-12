<template>
  <div class="task-schedule page-container">
    <div class="page-header">
      <h1 class="page-title">任务调度管理</h1>
      <p class="page-description">管理智能体定时任务和调度配置</p>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建任务
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="任务名称">
          <el-input 
            v-model="searchForm.taskName" 
            placeholder="请输入任务名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="智能体ID">
          <el-input-number 
            v-model="searchForm.agentId" 
            placeholder="请输入智能体ID"
            clearable
            :min="1"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
          >
            <el-option label="有效" :value="1" />
            <el-option label="无效" :value="0" />
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
        <el-table-column prop="taskName" label="任务名称" min-width="150" />
        <el-table-column prop="agentId" label="智能体ID" width="120" />
        <el-table-column prop="description" label="任务描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="cronExpression" label="Cron表达式" min-width="150">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.cronExpression }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
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
              :icon="VideoPlay"
              @click="handleExecute(row)"
              :disabled="row.status === 0"
            >
              立即执行
            </el-button>
            <el-popconfirm
              title="确定要删除这个任务吗？"
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
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="智能体ID" prop="agentId">
              <el-input-number 
                v-model="formData.agentId" 
                placeholder="请输入智能体ID"
                :min="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>

        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input 
            v-model="formData.cronExpression" 
            placeholder="请输入Cron表达式，如：0/3 * * * * *"
          />
          <div class="cron-tip">
            <el-text type="info" size="small">
              格式：秒 分 时 日 月 周年，例如：0/3 * * * * * 表示每3秒执行一次
            </el-text>
          </div>
        </el-form-item>

        <el-form-item label="任务参数" prop="taskParam">
          <el-input
            v-model="formData.taskParam"
            type="textarea"
            :rows="6"
            placeholder="请输入JSON格式的任务参数"
            show-word-limit
            maxlength="2000"
          />
          <div class="param-tip">
            <el-text type="info" size="small">
              请输入有效的JSON格式参数，例如：{"message": "定时任务消息", "type": "scheduled"}
            </el-text>
          </div>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">有效</el-radio>
            <el-radio :value="0">无效</el-radio>
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
      title="任务详情"
      width="700px"
    >
      <div class="task-detail">
        <div class="detail-item">
          <label>任务名称：</label>
          <span>{{ viewData?.taskName || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>智能体ID：</label>
          <span>{{ viewData?.agentId || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>任务描述：</label>
          <span>{{ viewData?.description || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>Cron表达式：</label>
          <el-tag size="small" type="info">{{ viewData?.cronExpression || '-' }}</el-tag>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <el-tag :type="viewData?.status === 1 ? 'success' : 'danger'">
            {{ viewData?.status === 1 ? '有效' : '无效' }}
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
          <label>任务参数：</label>
          <div class="param-display">
            <el-scrollbar height="200px">
              <pre>{{ formatJson(viewData?.taskParam) }}</pre>
            </el-scrollbar>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Cron表达式帮助对话框 -->
    <el-dialog
      v-model="cronHelpVisible"
      title="Cron表达式帮助"
      width="600px"
    >
      <div class="cron-help">
        <h4>Cron表达式格式</h4>
        <p>格式：<code>秒 分 时 日 月 周 年</code></p>
        
        <h4>字段说明</h4>
        <el-table :data="cronFields" size="small">
          <el-table-column prop="field" label="字段" width="80" />
          <el-table-column prop="range" label="范围" width="100" />
          <el-table-column prop="description" label="说明" />
        </el-table>

        <h4>特殊字符</h4>
        <el-table :data="cronSpecialChars" size="small">
          <el-table-column prop="char" label="字符" width="60" />
          <el-table-column prop="description" label="说明" />
        </el-table>

        <h4>常用示例</h4>
        <el-table :data="cronExamples" size="small">
          <el-table-column prop="expression" label="表达式" width="150" />
          <el-table-column prop="description" label="说明" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, View, Edit, Delete, VideoPlay } from '@element-plus/icons-vue'
import { taskApi } from '@/api/task'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const cronHelpVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const viewData = ref(null)

// 搜索表单
const searchForm = reactive({
  taskName: '',
  agentId: null,
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
  agentId: null,
  taskName: '',
  description: '',
  cronExpression: '0/3 * * * * *',
  taskParam: '',
  status: 1
})

// 表单验证规则
const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  agentId: [
    { required: true, message: '请输入智能体ID', trigger: 'blur' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' },
    { validator: validateCron, trigger: 'blur' }
  ],
  taskParam: [
    { validator: validateJson, trigger: 'blur' }
  ]
}

// Cron帮助数据
const cronFields = [
  { field: '秒', range: '0-59', description: '秒' },
  { field: '分', range: '0-59', description: '分钟' },
  { field: '时', range: '0-23', description: '小时' },
  { field: '日', range: '1-31', description: '月中的天' },
  { field: '月', range: '1-12', description: '月份' },
  { field: '周', range: '0-7', description: '周中的天（0和7都代表周日）' },
  { field: '年', range: '1970-2099', description: '年份（可选）' }
]

const cronSpecialChars = [
  { char: '*', description: '匹配任意值' },
  { char: '?', description: '不指定值（用于日和周字段）' },
  { char: '-', description: '范围，如 1-5' },
  { char: ',', description: '列举，如 1,3,5' },
  { char: '/', description: '步长，如 0/15 表示每15分钟' },
  { char: 'L', description: '最后一天' },
  { char: 'W', description: '工作日' },
  { char: '#', description: '第几个星期几' }
]

const cronExamples = [
  { expression: '0/3 * * * * *', description: '每3秒执行一次' },
  { expression: '0 0/5 * * * *', description: '每5分钟执行一次' },
  { expression: '0 0 2 * * *', description: '每天凌晨2点执行' },
  { expression: '0 0 0 * * MON', description: '每周一凌晨执行' },
  { expression: '0 0 0 1 * *', description: '每月1号凌晨执行' }
]

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑任务' : '新建任务')

// 方法
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
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
function validateCron(rule, value, callback) {
  if (!value) {
    callback(new Error('请输入Cron表达式'))
    return
  }
  
  // 简单的Cron表达式验证
  const parts = value.trim().split(/\s+/)
  if (parts.length < 6 || parts.length > 7) {
    callback(new Error('Cron表达式格式不正确，应包含6-7个字段'))
    return
  }
  
  callback()
}

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
    const response = await taskApi.queryTaskScheduleList(params)
    tableData.value = response || []
    pagination.total = response?.length || 0
  } catch (error) {
    console.error('查询任务调度列表失败:', error)
    ElMessage.error('查询失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    taskName: '',
    agentId: null,
    status: null
  })
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    agentId: null,
    taskName: '',
    description: '',
    cronExpression: '0/3 * * * * *',
    taskParam: '{\n  "message": "定时任务消息",\n  "type": "scheduled"\n}',
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

const handleExecute = async (row) => {
  try {
    ElMessage.info('立即执行功能开发中...')
    // TODO: 调用立即执行任务的API
  } catch (error) {
    console.error('执行任务失败:', error)
    ElMessage.error('执行任务失败，请重试')
  }
}

const handleDelete = async (row) => {
  try {
    await taskApi.deleteTaskSchedule(row.id)
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
      await taskApi.updateTaskSchedule(formData)
      ElMessage.success('更新成功')
    } else {
      await taskApi.addTaskSchedule(formData)
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
.task-schedule {
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

  .cron-tip,
  .param-tip {
    margin-top: 4px;
  }

  .task-detail {
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

  .cron-help {
    h4 {
      margin: 16px 0 8px 0;
      color: var(--text-primary);
    }

    p {
      margin: 8px 0;
      color: var(--text-secondary);
    }

    code {
      background: var(--bg-page);
      padding: 2px 6px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
    }

    .el-table {
      margin: 12px 0;
    }
  }
}
</style>
