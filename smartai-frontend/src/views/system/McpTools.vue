<template>
  <div class="mcp-tools page-container">
    <div class="page-header">
      <h1 class="page-title">MCP工具管理</h1>
      <p class="page-description">管理Model Context Protocol (MCP) 工具集成</p>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建MCP工具
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="MCP名称">
          <el-input 
            v-model="searchForm.mcpName" 
            placeholder="请输入MCP名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="传输类型">
          <el-select 
            v-model="searchForm.transportType" 
            placeholder="请选择传输类型"
            clearable
          >
            <el-option label="SSE" value="sse" />
            <el-option label="STDIO" value="stdio" />
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
        <el-table-column prop="mcpName" label="MCP名称" min-width="150" />
        <el-table-column prop="transportType" label="传输类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.transportType === 'sse' ? 'primary' : 'success'">
              {{ row.transportType?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transportConfig" label="传输配置" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestTimeout" label="超时时间(分钟)" width="140" />
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
              :icon="Connection"
              @click="handleTest(row)"
            >
              测试连接
            </el-button>
            <el-popconfirm
              title="确定要删除这个MCP工具吗？"
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
        label-width="120px"
      >
        <el-form-item label="MCP名称" prop="mcpName">
          <el-input v-model="formData.mcpName" placeholder="请输入MCP名称" />
        </el-form-item>
        <el-form-item label="传输类型" prop="transportType">
          <el-select v-model="formData.transportType" placeholder="请选择传输类型">
            <el-option label="SSE (Server-Sent Events)" value="sse" />
            <el-option label="STDIO (Standard Input/Output)" value="stdio" />
          </el-select>
        </el-form-item>
        <el-form-item label="传输配置" prop="transportConfig">
          <el-input
            v-model="formData.transportConfig"
            type="textarea"
            :rows="4"
            placeholder="请输入JSON格式的传输配置"
            show-word-limit
            maxlength="1000"
          />
          <div class="config-tip">
            <el-text type="info" size="small">
              请输入有效的JSON格式配置，例如：{"host": "localhost", "port": 8080}
            </el-text>
          </div>
        </el-form-item>
        <el-form-item label="请求超时时间" prop="requestTimeout">
          <el-input-number 
            v-model="formData.requestTimeout" 
            :min="1" 
            :max="60"
            placeholder="请输入超时时间（分钟）"
          />
          <el-text type="info" size="small" style="margin-left: 8px">分钟</el-text>
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
      title="MCP工具详情"
      width="700px"
    >
      <div class="mcp-detail">
        <div class="detail-item">
          <label>MCP名称：</label>
          <span>{{ viewData?.mcpName || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>传输类型：</label>
          <el-tag :type="viewData?.transportType === 'sse' ? 'primary' : 'success'">
            {{ viewData?.transportType?.toUpperCase() }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>请求超时时间：</label>
          <span>{{ viewData?.requestTimeout || '-' }} 分钟</span>
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
        <div class="detail-item config-item">
          <label>传输配置：</label>
          <div class="config-display">
            <el-scrollbar height="200px">
              <pre>{{ formatJson(viewData?.transportConfig) }}</pre>
            </el-scrollbar>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 测试连接对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      title="测试MCP连接"
      width="600px"
    >
      <div class="test-section">
        <div class="test-info">
          <p>正在测试连接到：<strong>{{ testData?.mcpName }}</strong></p>
          <p>传输类型：<el-tag size="small">{{ testData?.transportType?.toUpperCase() }}</el-tag></p>
        </div>
        <div class="test-result" v-if="testResult">
          <el-alert
            :type="testResult.success ? 'success' : 'error'"
            :title="testResult.success ? '连接成功' : '连接失败'"
            :description="testResult.message"
            show-icon
            :closable="false"
          />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="testDialogVisible = false">关闭</el-button>
          <el-button type="primary" :loading="testing" @click="performTest">
            {{ testing ? '测试中...' : '重新测试' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, View, Edit, Delete, Connection } from '@element-plus/icons-vue'
import { mcpToolApi } from '@/api/system'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const testDialogVisible = ref(false)
const submitting = ref(false)
const testing = ref(false)
const formRef = ref()
const viewData = ref(null)
const testData = ref(null)
const testResult = ref(null)

// 搜索表单
const searchForm = reactive({
  mcpName: '',
  transportType: '',
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
  mcpName: '',
  transportType: 'sse',
  transportConfig: '',
  requestTimeout: 5,
  status: 1
})

// 表单验证规则
const formRules = {
  mcpName: [
    { required: true, message: '请输入MCP名称', trigger: 'blur' }
  ],
  transportType: [
    { required: true, message: '请选择传输类型', trigger: 'change' }
  ],
  transportConfig: [
    { required: true, message: '请输入传输配置', trigger: 'blur' },
    { validator: validateJson, trigger: 'blur' }
  ],
  requestTimeout: [
    { required: true, message: '请输入请求超时时间', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑MCP工具' : '新建MCP工具')

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
function validateJson(rule, value, callback) {
  if (!value) {
    callback(new Error('请输入传输配置'))
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
    const response = await mcpToolApi.queryMcpList(params)
    tableData.value = response || []
    pagination.total = response?.length || 0
  } catch (error) {
    console.error('查询MCP工具列表失败:', error)
    ElMessage.error('查询失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(searchForm, {
    mcpName: '',
    transportType: '',
    status: null
  })
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    id: null,
    mcpName: '',
    transportType: 'sse',
    transportConfig: '{\n  "host": "localhost",\n  "port": 8080\n}',
    requestTimeout: 5,
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

const handleTest = (row) => {
  testData.value = { ...row }
  testResult.value = null
  testDialogVisible.value = true
  // 自动执行一次测试
  performTest()
}

const performTest = async () => {
  testing.value = true
  testResult.value = null
  
  try {
    // 模拟测试连接
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 这里应该调用真实的测试接口
    testResult.value = {
      success: Math.random() > 0.3, // 模拟70%成功率
      message: Math.random() > 0.3 ? 'MCP工具连接正常，可以正常使用' : '连接超时，请检查配置和网络连接'
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    testResult.value = {
      success: false,
      message: '测试过程中发生错误：' + error.message
    }
  } finally {
    testing.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await mcpToolApi.deleteMcp(row.id)
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
      await mcpToolApi.updateMcp(formData)
      ElMessage.success('更新成功')
    } else {
      await mcpToolApi.addMcp(formData)
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
.mcp-tools {
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

  .config-tip {
    margin-top: 4px;
  }

  .mcp-detail {
    .detail-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 12px;
      gap: 8px;

      &.config-item {
        flex-direction: column;
        align-items: stretch;
      }

      label {
        font-weight: 500;
        color: var(--text-secondary);
        min-width: 120px;
      }

      span {
        color: var(--text-primary);
      }

      .config-display {
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

  .test-section {
    .test-info {
      margin-bottom: 16px;
      
      p {
        margin: 8px 0;
        color: var(--text-secondary);
      }
    }

    .test-result {
      margin-top: 16px;
    }
  }
}
</style>
