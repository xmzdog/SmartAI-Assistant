<template>
  <div class="not-found">
    <div class="error-container">
      <div class="error-icon">
        <el-icon size="120" color="#c0c4cc">
          <WarningFilled />
        </el-icon>
      </div>
      
      <div class="error-content">
        <h1 class="error-title">404</h1>
        <h2 class="error-subtitle">页面未找到</h2>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除。
        </p>
        
        <div class="error-actions">
          <el-button type="primary" @click="goHome" size="large">
            返回首页
          </el-button>
          <el-button @click="goBack" size="large">
            返回上页
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="suggestions">
      <h3>您可以尝试：</h3>
      <ul>
        <li>检查URL是否正确</li>
        <li>返回首页重新导航</li>
        <li>使用搜索功能查找内容</li>
        <li>联系管理员获取帮助</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px 20px;
  text-align: center;
  background: var(--bg-page);
  
  .error-container {
    background: var(--bg-white);
    border-radius: 12px;
    padding: 60px 40px;
    box-shadow: var(--shadow-light);
    max-width: 500px;
    width: 100%;
    margin-bottom: 40px;
    
    .error-icon {
      margin-bottom: 24px;
    }
    
    .error-content {
      .error-title {
        font-size: 72px;
        font-weight: 700;
        color: var(--text-primary);
        margin: 0 0 16px 0;
        line-height: 1;
      }
      
      .error-subtitle {
        font-size: 24px;
        font-weight: 500;
        color: var(--text-regular);
        margin: 0 0 16px 0;
      }
      
      .error-description {
        font-size: 16px;
        color: var(--text-secondary);
        margin: 0 0 32px 0;
        line-height: 1.5;
      }
      
      .error-actions {
        display: flex;
        gap: 16px;
        justify-content: center;
        
        @media (max-width: 480px) {
          flex-direction: column;
        }
      }
    }
  }
  
  .suggestions {
    background: var(--bg-white);
    border-radius: 8px;
    padding: 24px;
    box-shadow: var(--shadow-base);
    max-width: 400px;
    width: 100%;
    
    h3 {
      font-size: 18px;
      color: var(--text-primary);
      margin: 0 0 16px 0;
    }
    
    ul {
      text-align: left;
      margin: 0;
      padding-left: 20px;
      
      li {
        color: var(--text-secondary);
        margin-bottom: 8px;
        line-height: 1.5;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .not-found {
    padding: 20px;
    
    .error-container {
      padding: 40px 24px;
      
      .error-title {
        font-size: 56px;
      }
      
      .error-subtitle {
        font-size: 20px;
      }
    }
  }
}
</style>