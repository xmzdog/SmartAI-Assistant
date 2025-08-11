<template>
  <div class="settings page-container">
      <div class="page-header">
        <h1 class="page-title">系统设置</h1>
        <p class="page-description">配置和管理SmartAI Assistant系统参数</p>
      </div>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 系统配置 -->
        <el-tab-pane label="系统配置" name="system">
          <div class="settings-section">
            <div class="section-title">
              <el-icon><Setting /></el-icon>
              <span>基础配置</span>
            </div>
            
            <el-form :model="systemConfig" label-width="150px" class="config-form">
              <el-form-item label="系统名称">
                <el-input v-model="systemConfig.systemName" placeholder="SmartAI Assistant" />
              </el-form-item>
              
              <el-form-item label="系统描述">
                <el-input
                  v-model="systemConfig.systemDescription"
                  type="textarea"
                  :rows="3"
                  placeholder="智能AI助手系统"
                />
              </el-form-item>
              
              <el-form-item label="默认语言">
                <el-select v-model="systemConfig.defaultLanguage">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="时区设置">
                <el-select v-model="systemConfig.timezone" filterable>
                  <el-option label="Asia/Shanghai (UTC+8)" value="Asia/Shanghai" />
                  <el-option label="America/New_York (UTC-5)" value="America/New_York" />
                  <el-option label="Europe/London (UTC+0)" value="Europe/London" />
                  <el-option label="Asia/Tokyo (UTC+9)" value="Asia/Tokyo" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="日志级别">
                <el-select v-model="systemConfig.logLevel">
                  <el-option label="DEBUG" value="debug" />
                  <el-option label="INFO" value="info" />
                  <el-option label="WARN" value="warn" />
                  <el-option label="ERROR" value="error" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="自动备份">
                <el-switch v-model="systemConfig.autoBackup" />
                <span class="form-help">启用系统数据自动备份</span>
              </el-form-item>
              
              <el-form-item label="备份间隔" v-if="systemConfig.autoBackup">
                <el-input-number
                  v-model="systemConfig.backupInterval"
                  :min="1"
                  :max="168"
                  suffix="小时"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Agent配置 -->
        <el-tab-pane label="Agent配置" name="agent">
          <div class="settings-section">
            <div class="section-title">
                              <el-icon><Service /></el-icon>
              <span>Agent参数</span>
            </div>
            
            <el-form :model="agentConfig" label-width="150px" class="config-form">
              <el-form-item label="默认超时时间">
                <el-input-number
                  v-model="agentConfig.defaultTimeout"
                  :min="1"
                  :max="300"
                  suffix="分钟"
                />
              </el-form-item>
              
              <el-form-item label="最大并发任务">
                <el-input-number
                  v-model="agentConfig.maxConcurrentTasks"
                  :min="1"
                  :max="100"
                />
              </el-form-item>
              
              <el-form-item label="任务重试次数">
                <el-input-number
                  v-model="agentConfig.maxRetries"
                  :min="0"
                  :max="10"
                />
              </el-form-item>
              
              <el-form-item label="推理策略">
                <el-select v-model="agentConfig.defaultStrategy">
                  <el-option label="综合分析" value="COMPREHENSIVE" />
                  <el-option label="步骤推理" value="STEP_BY_STEP" />
                  <el-option label="快速模式" value="QUICK" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="启用CoT推理">
                <el-switch v-model="agentConfig.enableCoT" />
                <span class="form-help">启用Chain-of-Thought推理模式</span>
              </el-form-item>
              
              <el-form-item label="上下文窗口大小">
                <el-input-number
                  v-model="agentConfig.contextWindowSize"
                  :min="1024"
                  :max="32768"
                  :step="1024"
                />
              </el-form-item>
              
              <el-form-item label="温度参数">
                <el-slider
                  v-model="agentConfig.temperature"
                  :min="0"
                  :max="2"
                  :step="0.1"
                  show-input
                  :show-input-controls="false"
                />
                <span class="form-help">控制输出的随机性，值越高越随机</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <div class="settings-section">
            <div class="section-title">
              <el-icon><Lock /></el-icon>
              <span>安全配置</span>
            </div>
            
            <el-form :model="securityConfig" label-width="150px" class="config-form">
              <el-form-item label="启用认证">
                <el-switch v-model="securityConfig.enableAuth" />
                <span class="form-help">启用用户身份认证</span>
              </el-form-item>
              
              <el-form-item label="会话超时" v-if="securityConfig.enableAuth">
                <el-input-number
                  v-model="securityConfig.sessionTimeout"
                  :min="5"
                  :max="1440"
                  suffix="分钟"
                />
              </el-form-item>
              
              <el-form-item label="API密钥">
                <el-input
                  v-model="securityConfig.apiKey"
                  type="password"
                  placeholder="输入API密钥"
                  show-password
                />
              </el-form-item>
              
              <el-form-item label="允许的IP范围">
                <el-input
                  v-model="securityConfig.allowedIPs"
                  placeholder="192.168.1.0/24, 10.0.0.0/8"
                />
                <span class="form-help">支持CIDR格式，多个范围用逗号分隔</span>
              </el-form-item>
              
              <el-form-item label="启用HTTPS">
                <el-switch v-model="securityConfig.enableHTTPS" />
                <span class="form-help">强制使用HTTPS连接</span>
              </el-form-item>
              
              <el-form-item label="启用访问日志">
                <el-switch v-model="securityConfig.enableAccessLog" />
                <span class="form-help">记录所有API访问日志</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <div class="settings-section">
            <div class="section-title">
              <el-icon><Bell /></el-icon>
              <span>通知配置</span>
            </div>
            
            <el-form :model="notificationConfig" label-width="150px" class="config-form">
              <el-form-item label="启用邮件通知">
                <el-switch v-model="notificationConfig.enableEmail" />
              </el-form-item>
              
              <div v-if="notificationConfig.enableEmail" class="sub-config">
                <el-form-item label="SMTP服务器">
                  <el-input v-model="notificationConfig.smtpHost" placeholder="smtp.example.com" />
                </el-form-item>
                
                <el-form-item label="SMTP端口">
                  <el-input-number v-model="notificationConfig.smtpPort" :min="1" :max="65535" />
                </el-form-item>
                
                <el-form-item label="发送邮箱">
                  <el-input v-model="notificationConfig.fromEmail" placeholder="noreply@example.com" />
                </el-form-item>
                
                <el-form-item label="邮箱密码">
                  <el-input
                    v-model="notificationConfig.emailPassword"
                    type="password"
                    placeholder="邮箱授权密码"
                    show-password
                  />
                </el-form-item>
              </div>
              
              <el-form-item label="启用Webhook">
                <el-switch v-model="notificationConfig.enableWebhook" />
              </el-form-item>
              
              <div v-if="notificationConfig.enableWebhook" class="sub-config">
                <el-form-item label="Webhook URL">
                  <el-input v-model="notificationConfig.webhookUrl" placeholder="https://hooks.example.com/webhook" />
                </el-form-item>
                
                <el-form-item label="密钥">
                  <el-input
                    v-model="notificationConfig.webhookSecret"
                    type="password"
                    placeholder="Webhook密钥"
                    show-password
                  />
                </el-form-item>
              </div>
              
              <el-form-item label="通知事件">
                <el-checkbox-group v-model="notificationConfig.notifyEvents">
                  <el-checkbox label="task_completed">任务完成</el-checkbox>
                  <el-checkbox label="task_failed">任务失败</el-checkbox>
                  <el-checkbox label="system_error">系统错误</el-checkbox>
                  <el-checkbox label="resource_warning">资源警告</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 存储设置 -->
        <el-tab-pane label="存储设置" name="storage">
          <div class="settings-section">
            <div class="section-title">
              <el-icon><FolderOpened /></el-icon>
              <span>存储配置</span>
            </div>
            
            <el-form :model="storageConfig" label-width="150px" class="config-form">
              <el-form-item label="数据存储路径">
                <el-input v-model="storageConfig.dataPath" placeholder="/data/smartai" />
              </el-form-item>
              
              <el-form-item label="日志存储路径">
                <el-input v-model="storageConfig.logPath" placeholder="/logs/smartai" />
              </el-form-item>
              
              <el-form-item label="临时文件路径">
                <el-input v-model="storageConfig.tempPath" placeholder="/tmp/smartai" />
              </el-form-item>
              
              <el-form-item label="最大存储大小">
                <el-input-number
                  v-model="storageConfig.maxStorageSize"
                  :min="1"
                  :max="1000"
                  suffix="GB"
                />
              </el-form-item>
              
              <el-form-item label="自动清理">
                <el-switch v-model="storageConfig.autoCleanup" />
                <span class="form-help">自动清理过期文件</span>
              </el-form-item>
              
              <el-form-item label="保留天数" v-if="storageConfig.autoCleanup">
                <el-input-number
                  v-model="storageConfig.retentionDays"
                  :min="1"
                  :max="365"
                  suffix="天"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 操作按钮 -->
      <div class="settings-actions">
        <el-button @click="resetSettings">重置为默认值</el-button>
        <el-button @click="exportSettings">导出配置</el-button>
        <el-button @click="importSettings">导入配置</el-button>
        <el-button type="primary" @click="saveSettings" :loading="isSaving">
          保存设置
        </el-button>
      </div>

      <!-- 导入配置文件对话框 -->
      <input
        ref="fileInput"
        type="file"
        accept=".json"
        style="display: none"
        @change="handleFileImport"
      />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useSystemStore } from '@/stores/system'

import {
  Setting, Service, Lock, Bell, FolderOpened
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const systemStore = useSystemStore()

// 响应式数据
const activeTab = ref('system')
const isSaving = ref(false)
const fileInput = ref<HTMLInputElement>()

// 配置表单
const systemConfig = reactive({
  systemName: 'SmartAI Assistant',
  systemDescription: '基于SpringAI的智能助手系统',
  defaultLanguage: 'zh-CN',
  timezone: 'Asia/Shanghai',
  logLevel: 'info',
  autoBackup: true,
  backupInterval: 24
})

const agentConfig = reactive({
  defaultTimeout: 30,
  maxConcurrentTasks: 10,
  maxRetries: 3,
  defaultStrategy: 'COMPREHENSIVE',
  enableCoT: true,
  contextWindowSize: 8192,
  temperature: 0.7
})

const securityConfig = reactive({
  enableAuth: false,
  sessionTimeout: 120,
  apiKey: '',
  allowedIPs: '',
  enableHTTPS: false,
  enableAccessLog: true
})

const notificationConfig = reactive({
  enableEmail: false,
  smtpHost: '',
  smtpPort: 587,
  fromEmail: '',
  emailPassword: '',
  enableWebhook: false,
  webhookUrl: '',
  webhookSecret: '',
  notifyEvents: ['task_completed', 'task_failed', 'system_error']
})

const storageConfig = reactive({
  dataPath: '/data/smartai',
  logPath: '/logs/smartai',
  tempPath: '/tmp/smartai',
  maxStorageSize: 100,
  autoCleanup: true,
  retentionDays: 30
})

// 方法
const saveSettings = async () => {
  try {
    isSaving.value = true
    
    // 构建完整配置对象
    const allConfigs = {
      system: systemConfig,
      agent: agentConfig,
      security: securityConfig,
      notification: notificationConfig,
      storage: storageConfig,
      lastModified: new Date().toISOString()
    }
    
    // 模拟保存到后端
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 保存到本地存储
    localStorage.setItem('smartai-settings', JSON.stringify(allConfigs))
    
    ElMessage.success('设置已保存')
  } catch (error: any) {
    ElMessage.error('保存设置失败: ' + error.message)
  } finally {
    isSaving.value = false
  }
}

const resetSettings = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有设置为默认值吗？此操作不可撤销。', '确认重置', {
      type: 'warning',
      confirmButtonText: '确认重置',
      cancelButtonText: '取消'
    })
    
    // 重置所有配置为默认值
    Object.assign(systemConfig, {
      systemName: 'SmartAI Assistant',
      systemDescription: '基于SpringAI的智能助手系统',
      defaultLanguage: 'zh-CN',
      timezone: 'Asia/Shanghai',
      logLevel: 'info',
      autoBackup: true,
      backupInterval: 24
    })
    
    Object.assign(agentConfig, {
      defaultTimeout: 30,
      maxConcurrentTasks: 10,
      maxRetries: 3,
      defaultStrategy: 'COMPREHENSIVE',
      enableCoT: true,
      contextWindowSize: 8192,
      temperature: 0.7
    })
    
    Object.assign(securityConfig, {
      enableAuth: false,
      sessionTimeout: 120,
      apiKey: '',
      allowedIPs: '',
      enableHTTPS: false,
      enableAccessLog: true
    })
    
    Object.assign(notificationConfig, {
      enableEmail: false,
      smtpHost: '',
      smtpPort: 587,
      fromEmail: '',
      emailPassword: '',
      enableWebhook: false,
      webhookUrl: '',
      webhookSecret: '',
      notifyEvents: ['task_completed', 'task_failed', 'system_error']
    })
    
    Object.assign(storageConfig, {
      dataPath: '/data/smartai',
      logPath: '/logs/smartai',
      tempPath: '/tmp/smartai',
      maxStorageSize: 100,
      autoCleanup: true,
      retentionDays: 30
    })
    
    ElMessage.success('设置已重置为默认值')
  } catch {
    // 用户取消
  }
}

const exportSettings = () => {
  const allConfigs = {
    system: systemConfig,
    agent: agentConfig,
    security: securityConfig,
    notification: notificationConfig,
    storage: storageConfig,
    exportTime: new Date().toISOString(),
    version: '1.0.0'
  }
  
  const blob = new Blob([JSON.stringify(allConfigs, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `smartai-settings-${dayjs().format('YYYY-MM-DD')}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  ElMessage.success('配置已导出')
}

const importSettings = () => {
  fileInput.value?.click()
}

const handleFileImport = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const importedConfig = JSON.parse(e.target?.result as string)
      
      // 验证配置格式
      if (!importedConfig.system || !importedConfig.agent) {
        throw new Error('配置文件格式不正确')
      }
      
      // 应用导入的配置
      if (importedConfig.system) Object.assign(systemConfig, importedConfig.system)
      if (importedConfig.agent) Object.assign(agentConfig, importedConfig.agent)
      if (importedConfig.security) Object.assign(securityConfig, importedConfig.security)
      if (importedConfig.notification) Object.assign(notificationConfig, importedConfig.notification)
      if (importedConfig.storage) Object.assign(storageConfig, importedConfig.storage)
      
      ElMessage.success('配置已导入')
    } catch (error: any) {
      ElMessage.error('导入失败: ' + error.message)
    }
  }
  
  reader.readAsText(file)
}

const loadSettings = () => {
  try {
    const saved = localStorage.getItem('smartai-settings')
    if (saved) {
      const savedConfig = JSON.parse(saved)
      
      if (savedConfig.system) Object.assign(systemConfig, savedConfig.system)
      if (savedConfig.agent) Object.assign(agentConfig, savedConfig.agent)
      if (savedConfig.security) Object.assign(securityConfig, savedConfig.security)
      if (savedConfig.notification) Object.assign(notificationConfig, savedConfig.notification)
      if (savedConfig.storage) Object.assign(storageConfig, savedConfig.storage)
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadSettings()
})
</script>

<style lang="scss" scoped>
.settings {
  padding: 24px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-light);
  }
  
  .settings-section {
    padding: 24px;
  }
  
  .config-form {
    max-width: 600px;
    
    .form-help {
      font-size: 12px;
      color: var(--text-placeholder);
      margin-left: 8px;
    }
    
    .sub-config {
      margin-left: 20px;
      padding-left: 20px;
      border-left: 2px solid var(--border-light);
    }
  }
  
  .settings-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid var(--border-light);
  }
}

:deep(.el-tabs--border-card) {
  border: 1px solid var(--border-light);
  
  .el-tabs__header {
    background: var(--fill-lighter);
    border-bottom: 1px solid var(--border-light);
  }
  
  .el-tabs__item {
    color: var(--text-secondary);
    
    &.is-active {
      color: var(--primary-color);
      background: var(--bg-white);
    }
  }
  
  .el-tabs__content {
    background: var(--bg-white);
  }
}

:deep(.el-form-item__label) {
  color: var(--text-primary);
  font-weight: 500;
}

:deep(.el-checkbox__label) {
  color: var(--text-regular);
}

@media (max-width: 768px) {
  .settings {
    padding: 16px;
    
    .config-form {
      max-width: 100%;
    }
    
    .settings-actions {
      flex-direction: column;
    }
  }
}
</style>