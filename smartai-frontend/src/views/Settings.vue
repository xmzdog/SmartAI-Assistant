<template>
  <div class="settings-container">
    <div class="page-header">
      <h1>系统设置</h1>
      <p>配置系统参数和用户偏好</p>
    </div>

    <!-- 基础设置 -->
    <div class="settings-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>基础设置</span>
          </div>
        </template>
        <el-form :model="basicSettings" label-width="120px">
          <el-form-item label="系统名称">
            <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
          </el-form-item>
          <el-form-item label="系统描述">
            <el-input 
              v-model="basicSettings.systemDescription" 
              type="textarea" 
              :rows="3"
              placeholder="请输入系统描述"
            />
          </el-form-item>
          <el-form-item label="系统版本">
            <el-input v-model="basicSettings.version" disabled />
          </el-form-item>
          <el-form-item label="语言设置">
            <el-select v-model="basicSettings.language" placeholder="选择语言">
              <el-option label="中文简体" value="zh-CN" />
              <el-option label="English" value="en-US" />
              <el-option label="日本語" value="ja-JP" />
            </el-select>
          </el-form-item>
          <el-form-item label="时区设置">
            <el-select v-model="basicSettings.timezone" placeholder="选择时区">
              <el-option label="北京时间 (UTC+8)" value="Asia/Shanghai" />
              <el-option label="东京时间 (UTC+9)" value="Asia/Tokyo" />
              <el-option label="纽约时间 (UTC-5)" value="America/New_York" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- AI服务设置 -->
    <div class="settings-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>AI服务设置</span>
          </div>
        </template>
        <el-form :model="aiSettings" label-width="120px">
          <el-form-item label="默认模型">
            <el-select v-model="aiSettings.defaultModel" placeholder="选择默认AI模型">
              <el-option label="GPT-4" value="gpt-4" />
              <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
              <el-option label="Claude-3" value="claude-3" />
              <el-option label="文心一言" value="ernie-bot" />
            </el-select>
          </el-form-item>
          <el-form-item label="API超时时间">
            <el-input-number 
              v-model="aiSettings.apiTimeout" 
              :min="5" 
              :max="300" 
              :step="5"
              controls-position="right"
            />
            <span style="margin-left: 8px; color: #909399;">秒</span>
          </el-form-item>
          <el-form-item label="最大重试次数">
            <el-input-number 
              v-model="aiSettings.maxRetries" 
              :min="0" 
              :max="10" 
              controls-position="right"
            />
          </el-form-item>
          <el-form-item label="启用流式响应">
            <el-switch v-model="aiSettings.enableStreaming" />
          </el-form-item>
          <el-form-item label="启用上下文记忆">
            <el-switch v-model="aiSettings.enableContext" />
          </el-form-item>
          <el-form-item label="上下文长度">
            <el-input-number 
              v-model="aiSettings.contextLength" 
              :min="1" 
              :max="50" 
              controls-position="right"
              :disabled="!aiSettings.enableContext"
            />
            <span style="margin-left: 8px; color: #909399;">条对话</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 安全设置 -->
    <div class="settings-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>安全设置</span>
          </div>
        </template>
        <el-form :model="securitySettings" label-width="120px">
          <el-form-item label="会话超时">
            <el-input-number 
              v-model="securitySettings.sessionTimeout" 
              :min="5" 
              :max="1440" 
              :step="5"
              controls-position="right"
            />
            <span style="margin-left: 8px; color: #909399;">分钟</span>
          </el-form-item>
          <el-form-item label="启用访问日志">
            <el-switch v-model="securitySettings.enableAccessLog" />
          </el-form-item>
          <el-form-item label="启用API限流">
            <el-switch v-model="securitySettings.enableRateLimit" />
          </el-form-item>
          <el-form-item label="每分钟请求限制" v-if="securitySettings.enableRateLimit">
            <el-input-number 
              v-model="securitySettings.rateLimit" 
              :min="1" 
              :max="1000" 
              controls-position="right"
            />
            <span style="margin-left: 8px; color: #909399;">次/分钟</span>
          </el-form-item>
          <el-form-item label="IP白名单">
            <el-input 
              v-model="securitySettings.ipWhitelist" 
              type="textarea" 
              :rows="3"
              placeholder="请输入IP地址，每行一个"
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 存储设置 -->
    <div class="settings-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>存储设置</span>
          </div>
        </template>
        <el-form :model="storageSettings" label-width="120px">
          <el-form-item label="数据保留天数">
            <el-input-number 
              v-model="storageSettings.dataRetentionDays" 
              :min="1" 
              :max="3650" 
              controls-position="right"
            />
            <span style="margin-left: 8px; color: #909399;">天</span>
          </el-form-item>
          <el-form-item label="自动清理日志">
            <el-switch v-model="storageSettings.autoCleanLogs" />
          </el-form-item>
          <el-form-item label="日志保留天数" v-if="storageSettings.autoCleanLogs">
            <el-input-number 
              v-model="storageSettings.logRetentionDays" 
              :min="1" 
              :max="365" 
              controls-position="right"
            />
            <span style="margin-left: 8px; color: #909399;">天</span>
          </el-form-item>
          <el-form-item label="启用数据备份">
            <el-switch v-model="storageSettings.enableBackup" />
          </el-form-item>
          <el-form-item label="备份频率" v-if="storageSettings.enableBackup">
            <el-select v-model="storageSettings.backupFrequency" placeholder="选择备份频率">
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 通知设置 -->
    <div class="settings-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>通知设置</span>
          </div>
        </template>
        <el-form :model="notificationSettings" label-width="120px">
          <el-form-item label="启用邮件通知">
            <el-switch v-model="notificationSettings.enableEmail" />
          </el-form-item>
          <el-form-item label="邮件服务器" v-if="notificationSettings.enableEmail">
            <el-input v-model="notificationSettings.emailServer" placeholder="smtp.example.com" />
          </el-form-item>
          <el-form-item label="邮件端口" v-if="notificationSettings.enableEmail">
            <el-input-number 
              v-model="notificationSettings.emailPort" 
              :min="1" 
              :max="65535" 
              controls-position="right"
            />
          </el-form-item>
          <el-form-item label="发送邮箱" v-if="notificationSettings.enableEmail">
            <el-input v-model="notificationSettings.emailFrom" placeholder="noreply@example.com" />
          </el-form-item>
          <el-form-item label="启用系统错误通知">
            <el-switch v-model="notificationSettings.enableErrorNotification" />
          </el-form-item>
          <el-form-item label="启用性能警告">
            <el-switch v-model="notificationSettings.enablePerformanceWarning" />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" size="large" @click="saveSettings" :loading="saving">
        保存设置
      </el-button>
      <el-button size="large" @click="resetSettings">
        重置为默认值
      </el-button>
      <el-button type="danger" size="large" @click="exportSettings">
        导出配置
      </el-button>
      <el-button type="warning" size="large" @click="importSettings">
        导入配置
      </el-button>
    </div>

    <!-- 隐藏的文件输入 -->
    <input 
      ref="fileInput" 
      type="file" 
      accept=".json" 
      style="display: none" 
      @change="handleFileImport"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useSystemStore } from '@/stores/system'

const systemStore = useSystemStore()

// 加载状态
const saving = ref(false)

// 基础设置
const basicSettings = reactive({
  systemName: 'SmartAI Assistant',
  systemDescription: '智能AI助手系统，提供多模态AI服务',
  version: '1.0.0',
  language: 'zh-CN',
  timezone: 'Asia/Shanghai'
})

// AI服务设置
const aiSettings = reactive({
  defaultModel: 'gpt-4',
  apiTimeout: 30,
  maxRetries: 3,
  enableStreaming: true,
  enableContext: true,
  contextLength: 10
})

// 安全设置
const securitySettings = reactive({
  sessionTimeout: 30,
  enableAccessLog: true,
  enableRateLimit: true,
  rateLimit: 100,
  ipWhitelist: ''
})

// 存储设置
const storageSettings = reactive({
  dataRetentionDays: 365,
  autoCleanLogs: true,
  logRetentionDays: 30,
  enableBackup: true,
  backupFrequency: 'daily'
})

// 通知设置
const notificationSettings = reactive({
  enableEmail: false,
  emailServer: '',
  emailPort: 587,
  emailFrom: '',
  enableErrorNotification: true,
  enablePerformanceWarning: true
})

// 文件输入引用
const fileInput = ref()

// 方法
const saveSettings = async () => {
  saving.value = true
  try {
    // 模拟保存API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 这里应该调用实际的保存API
    // await settingsApi.saveSettings({
    //   basic: basicSettings,
    //   ai: aiSettings,
    //   security: securitySettings,
    //   storage: storageSettings,
    //   notification: notificationSettings
    // })
    
    ElMessage.success('设置保存成功')
  } catch (error) {
    ElMessage.error('保存设置失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const resetSettings = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有设置为默认值吗？此操作不可撤销。', '确认重置', {
      type: 'warning'
    })
    
    // 重置为默认值
    Object.assign(basicSettings, {
      systemName: 'SmartAI Assistant',
      systemDescription: '智能AI助手系统，提供多模态AI服务',
      version: '1.0.0',
      language: 'zh-CN',
      timezone: 'Asia/Shanghai'
    })
    
    Object.assign(aiSettings, {
      defaultModel: 'gpt-4',
      apiTimeout: 30,
      maxRetries: 3,
      enableStreaming: true,
      enableContext: true,
      contextLength: 10
    })
    
    Object.assign(securitySettings, {
      sessionTimeout: 30,
      enableAccessLog: true,
      enableRateLimit: true,
      rateLimit: 100,
      ipWhitelist: ''
    })
    
    Object.assign(storageSettings, {
      dataRetentionDays: 365,
      autoCleanLogs: true,
      logRetentionDays: 30,
      enableBackup: true,
      backupFrequency: 'daily'
    })
    
    Object.assign(notificationSettings, {
      enableEmail: false,
      emailServer: '',
      emailPort: 587,
      emailFrom: '',
      enableErrorNotification: true,
      enablePerformanceWarning: true
    })
    
    ElMessage.success('设置已重置为默认值')
  } catch {
    // 用户取消
  }
}

const exportSettings = () => {
  const settings = {
    basic: basicSettings,
    ai: aiSettings,
    security: securitySettings,
    storage: storageSettings,
    notification: notificationSettings,
    exportTime: new Date().toISOString()
  }
  
  const dataStr = JSON.stringify(settings, null, 2)
  const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr)
  
  const exportFileDefaultName = `smartai-settings-${new Date().toISOString().split('T')[0]}.json`
  
  const linkElement = document.createElement('a')
  linkElement.setAttribute('href', dataUri)
  linkElement.setAttribute('download', exportFileDefaultName)
  linkElement.click()
  
  ElMessage.success('配置已导出')
}

const importSettings = () => {
  fileInput.value.click()
}

const handleFileImport = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const settings = JSON.parse(e.target.result)
      
      // 验证配置文件格式
      if (!settings.basic || !settings.ai || !settings.security || !settings.storage || !settings.notification) {
        throw new Error('配置文件格式不正确')
      }
      
      // 导入设置
      Object.assign(basicSettings, settings.basic)
      Object.assign(aiSettings, settings.ai)
      Object.assign(securitySettings, settings.security)
      Object.assign(storageSettings, settings.storage)
      Object.assign(notificationSettings, settings.notification)
      
      ElMessage.success('配置导入成功')
    } catch (error) {
      ElMessage.error('导入配置失败: ' + error.message)
    }
  }
  reader.readAsText(file)
  
  // 清空文件输入
  event.target.value = ''
}
</script>

<style scoped>
.settings-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.page-header p {
  margin: 5px 0 0 0;
  color: #909399;
}

.settings-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.action-buttons {
  text-align: center;
  padding: 30px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

.action-buttons .el-button {
  margin: 0 10px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-input-number) {
  width: 150px;
}

:deep(.el-select) {
  width: 200px;
}

:deep(.el-textarea) {
  width: 400px;
}

:deep(.el-input) {
  width: 300px;
}
</style>
