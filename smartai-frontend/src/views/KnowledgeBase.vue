<template>
  <div class="knowledge-base-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon><Files /></el-icon>
          知识库管理
        </h1>
        <p>上传文档构建专属知识库，提升AI对话效果</p>
      </div>
    </div>

    <!-- 知识库上传卡片 -->
    <el-card class="upload-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>上传知识库文档</span>
          <el-button type="primary" @click="showUploadDialog = true">
            <el-icon><Upload /></el-icon>
            上传文档
          </el-button>
        </div>
      </template>

      <!-- 已有知识库列表 -->
      <div class="knowledge-base-list">
        <el-empty v-if="knowledgeBases.length === 0" description="暂无知识库，点击上方按钮创建" />
        <div v-else class="kb-grid">
          <el-card 
            v-for="kb in knowledgeBases" 
            :key="kb"
            class="kb-item" 
            shadow="hover"
            @click="selectKnowledgeBase(kb)"
          >
            <div class="kb-content">
              <div class="kb-icon">
                <el-icon size="32"><FolderOpened /></el-icon>
              </div>
              <div class="kb-info">
                <h3>{{ kb }}</h3>
                <p>{{ getKnowledgeBaseDocCount(kb) }} 个文档</p>
              </div>
              <div class="kb-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click.stop="deleteKnowledgeBase(kb)"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog 
      v-model="showUploadDialog" 
      title="上传知识库文档"
      width="600px"
      destroy-on-close
    >
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="选择知识库" required>
          <el-select 
            v-model="uploadForm.ragTag" 
            placeholder="请选择现有知识库或输入新知识库名称"
            filterable
            allow-create
            style="width: 100%"
          >
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb"
              :label="kb"
              :value="kb"
            />
          </el-select>
          <div class="form-tip">
            可以选择现有知识库添加文档，或输入新名称创建知识库
          </div>
        </el-form-item>
        
        <el-form-item label="文档上传" required>
          <el-upload
            ref="uploadRef"
            class="upload-area"
            drag
            multiple
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :accept="acceptedFileTypes"
            :before-upload="beforeUpload"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、TXT、Markdown 等格式，单个文件不超过 50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="uploadFiles"
            :loading="uploading"
            :disabled="!uploadForm.ragTag || uploadForm.files.length === 0"
          >
            <el-icon v-if="!uploading"><Upload /></el-icon>
            {{ uploading ? '上传中...' : '开始上传' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 知识库详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      :title="`知识库详情 - ${selectedKnowledgeBase}`"
      width="800px"
    >
      <div class="kb-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="知识库名称">{{ selectedKnowledgeBase }}</el-descriptions-item>
          <el-descriptions-item label="文档数量">{{ getKnowledgeBaseDocCount(selectedKnowledgeBase) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ new Date().toLocaleDateString() }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag type="success">正常</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="action-buttons">
          <el-button type="primary" @click="addDocuments">
            <el-icon><Plus /></el-icon>
            添加文档
          </el-button>
          <el-button type="success" @click="testKnowledgeBase">
            <el-icon><ChatDotRound /></el-icon>
            测试问答
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Files,
  Plus,
  Upload,
  UploadFilled,
  FolderOpened,
  Delete,
  ChatDotRound
} from '@element-plus/icons-vue'
import { knowledgeBaseApi } from '@/api/knowledgeBase'

// 路由
const router = useRouter()

// 响应式数据
const showUploadDialog = ref(false)
const showDetailDialog = ref(false)
const uploading = ref(false)
const knowledgeBases = ref([])
const selectedKnowledgeBase = ref('')
const uploadRef = ref()

// 上传表单
const uploadForm = reactive({
  ragTag: '',
  files: []
})

// 支持的文件类型
const acceptedFileTypes = '.pdf,.doc,.docx,.txt,.md,.rtf'

// 生命周期
onMounted(() => {
  loadKnowledgeBases()
})

// 方法定义
const loadKnowledgeBases = async () => {
  try {
    const response = await knowledgeBaseApi.getKnowledgeBaseList()
    if (response.code === '0000') {
      knowledgeBases.value = response.data || []
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
    ElMessage.error('加载知识库列表失败')
  }
}

const handleFileChange = (file, fileList) => {
  uploadForm.files = fileList
}

const handleFileRemove = (file, fileList) => {
  uploadForm.files = fileList
}

const beforeUpload = (file) => {
  const isValidSize = file.size / 1024 / 1024 < 50 // 50MB
  if (!isValidSize) {
    ElMessage.error('文件大小不能超过 50MB!')
    return false
  }
  return true
}

const uploadFiles = async () => {
  if (!uploadForm.ragTag.trim()) {
    ElMessage.error('请输入知识库名称')
    return
  }
  
  if (uploadForm.files.length === 0) {
    ElMessage.error('请选择要上传的文件')
    return
  }

  uploading.value = true
  
  try {
    const formData = new FormData()
    formData.append('ragTag', uploadForm.ragTag.trim())
    
    // 添加多个文件
    uploadForm.files.forEach(file => {
      formData.append('file', file.raw)
    })

    console.log('准备上传文件:', {
      ragTag: uploadForm.ragTag.trim(),
      fileCount: uploadForm.files.length,
      files: uploadForm.files.map(f => ({ name: f.name, size: f.size }))
    })

    const response = await knowledgeBaseApi.uploadFiles(formData)
    
    console.log('上传响应:', response)
    
    if (response.code === '0000') {
      ElMessage.success('知识库上传成功!')
      showUploadDialog.value = false
      uploadForm.ragTag = ''
      uploadForm.files = []
      uploadRef.value?.clearFiles()
      loadKnowledgeBases()
    } else {
      ElMessage.error(response.info || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请检查网络连接和后端服务状态')
  } finally {
    uploading.value = false
  }
}

const selectKnowledgeBase = (kbName) => {
  selectedKnowledgeBase.value = kbName
  showDetailDialog.value = true
}

const deleteKnowledgeBase = async (kbName) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除知识库 "${kbName}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await knowledgeBaseApi.deleteKnowledgeBase(kbName)
    if (response.code === '0000') {
      ElMessage.success('删除成功')
      loadKnowledgeBases()
    } else {
      ElMessage.error(response.info || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除知识库失败:', error)
      ElMessage.error('删除失败，请检查网络连接')
    }
  }
}

const getKnowledgeBaseDocCount = (kbName) => {
  // TODO: 从后端获取具体文档数量
  return Math.floor(Math.random() * 10) + 1
}

const addDocuments = () => {
  showDetailDialog.value = false
  uploadForm.ragTag = selectedKnowledgeBase.value
  showUploadDialog.value = true
}

const testKnowledgeBase = () => {
  showDetailDialog.value = false
  // 跳转到智能对话页面，并预选知识库
  ElMessage.success(`跳转到智能对话页面，已选择知识库: ${selectedKnowledgeBase.value}`)
  // 实现路由跳转并传递知识库参数
  router.push({
    name: 'ChatPage',
    query: {
      kb: selectedKnowledgeBase.value
    }
  })
}
</script>

<style scoped>
.knowledge-base-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.header-content h1 {
  color: #303133;
  font-size: 28px;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-content p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.upload-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.knowledge-base-list {
  min-height: 200px;
}

.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.kb-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.kb-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.kb-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.kb-icon {
  color: #409eff;
}

.kb-info {
  flex: 1;
}

.kb-info h3 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 16px;
}

.kb-info p {
  margin: 0;
  color: #909399;
  font-size: 12px;
}

.kb-actions {
  opacity: 0;
  transition: opacity 0.3s ease;
}

.kb-item:hover .kb-actions {
  opacity: 1;
}

.upload-area {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.kb-detail {
  padding: 16px 0;
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .knowledge-base-container {
    padding: 16px;
  }
  
  .kb-grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>
