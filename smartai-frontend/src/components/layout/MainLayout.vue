<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon v-if="!isCollapsed" size="32" color="#409eff">
            <Avatar />
          </el-icon>
          <h1 v-if="!isCollapsed" class="logo-text">SmartAI</h1>
        </div>
        <el-button
          class="collapse-btn"
          :icon="isCollapsed ? Expand : Fold"
          @click="toggleSidebar"
          text
        />
      </div>
      
      <nav class="sidebar-nav">
        <el-menu
          :default-active="currentRoute"
          :collapse="isCollapsed"
          :router="true"
          background-color="var(--bg-white)"
          text-color="var(--text-primary)"
          active-text-color="white"
        >
          <template v-for="route in routes" :key="route.path">
            <el-sub-menu
              v-if="route.children && route.children.length > 0"
              :index="'/' + route.path"
            >
              <template #title>
                <el-icon>
                  <component :is="route.meta?.icon || 'Document'" />
                </el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="'/' + route.path + '/' + child.path"
              >
                {{ child.meta?.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item
              v-else
              :index="'/' + route.path"
            >
              <el-icon>
                <component :is="route.meta?.icon || 'Document'" />
              </el-icon>
              <template #title>{{ route.meta?.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </nav>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航 -->
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteMeta?.title">
              {{ currentRouteMeta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 系统状态指示器 -->
          <div class="status-indicator">
            <el-tooltip :content="agentStatus?.status === 'IDLE' ? 'Agent空闲' : 'Agent忙碌'">
              <el-badge
                :value="agentStatus?.activeTaskCount || 0"
                :max="99"
                :hidden="!agentStatus?.activeTaskCount"
              >
                <div
                  class="status-dot"
                  :class="{
                    idle: agentStatus?.status === 'IDLE',
                    busy: agentStatus?.status === 'BUSY',
                    error: agentStatus?.status === 'ERROR'
                  }"
                />
              </el-badge>
            </el-tooltip>
          </div>
          
          <!-- 主题切换 -->
          <el-button
            :icon="systemStore.theme === 'light' ? Moon : Sunny"
            @click="systemStore.toggleTheme"
            text
            circle
          />
          
          <!-- 刷新 -->
          <el-button
            :icon="Refresh"
            @click="handleRefresh"
            :loading="systemStore.isLoading"
            text
            circle
          />
          
          <!-- 设置 -->
          <el-tooltip content="系统设置">
            <el-button
              :icon="Setting"
              text
              circle
              disabled
            />
          </el-tooltip>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSystemStore } from '@/stores/system'
import {
  Avatar, Expand, Fold, Moon, Sunny, Refresh, Setting,
  Dashboard, Cpu, Tools, Monitor, Document, ChatLineSquare, FolderOpened, User, Timer
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const systemStore = useSystemStore()

// 侧边栏状态
const isCollapsed = ref(false)

// 路由配置
const routes = [
  { path: 'dashboard', meta: { title: '仪表板', icon: 'Dashboard' } },
  { path: 'chat', meta: { title: '智能对话', icon: 'ChatLineSquare' } },
  { path: 'agent', meta: { title: '智能体列表', icon: 'User' } },
  { path: 'agent/chat', meta: { title: 'Agent对话', icon: 'ChatLineSquare' } },
  { path: 'model', meta: { title: '模型管理', icon: 'Cpu' } },
  { path: 'knowledge', meta: { title: '知识库', icon: 'FolderOpened' } },
  { 
    path: 'system', 
    meta: { title: '系统配置', icon: 'Setting' },
    children: [
      { path: 'prompt', meta: { title: '系统提示词' } },
      { path: 'advisor', meta: { title: '顾问配置' } },
      { path: 'tools', meta: { title: 'MCP工具' } }
    ]
  },
  { path: 'task', meta: { title: '任务调度', icon: 'Timer' } },
  { path: 'monitoring', meta: { title: '系统监控', icon: 'Monitor' } },
  { path: 'settings', meta: { title: '系统设置', icon: 'Tools' } }
]

// 计算属性
const currentRoute = computed(() => {
  // 返回完整路径来匹配菜单项的 index
  return route.path
})
const currentRouteMeta = computed(() => route.meta)
const agentStatus = computed(() => systemStore.agentStatus)

// 方法
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebarCollapsed', isCollapsed.value.toString())
}

const handleRefresh = () => {
  systemStore.fetchAgentStatus()
  systemStore.fetchStatistics()
}

// 生命周期
onMounted(() => {
  // 恢复侧边栏状态
  const savedCollapsed = localStorage.getItem('sidebarCollapsed')
  if (savedCollapsed) {
    isCollapsed.value = savedCollapsed === 'true'
  }
})
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-page);
}

.sidebar {
  width: 250px;
  background: var(--bg-white);
  border-right: 1px solid var(--border-light);
  transition: width 0.3s ease;
  
  &.collapsed {
    width: 64px;
  }
  
  .sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--border-light);
    height: 60px;
    
    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .logo-text {
        font-size: 20px;
        font-weight: 600;
        color: var(--primary-color);
        margin: 0;
      }
    }
    
    .collapse-btn {
      color: var(--text-secondary);
    }
  }
  
  .sidebar-nav {
    padding: 8px 0;
    
    :deep(.el-menu) {
      border-right: none;
      
      .el-menu-item {
        margin: 0 8px;
        border-radius: 6px;
        
        &:hover {
          background-color: var(--fill-lighter);
        }
        
        &.is-active {
          background-color: var(--primary-color);
          color: white !important;
          
          .el-icon {
            color: white !important;
          }
        }
      }
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-light);
  
  .header-left {
    .el-breadcrumb {
      font-size: 14px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .status-indicator {
      margin-right: 8px;
      
      .status-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        transition: background-color 0.3s;
        
        &.idle {
          background-color: var(--success-color);
        }
        
        &.busy {
          background-color: var(--warning-color);
        }
        
        &.error {
          background-color: var(--danger-color);
        }
      }
    }
  }
}

.page-content {
  flex: 1;
  overflow: auto;
  background: var(--bg-page);
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式设计
@media (max-width: 768px) {
  .sidebar {
    position: absolute;
    z-index: 1000;
    height: 100%;
    
    &:not(.collapsed) {
      box-shadow: var(--shadow-dark);
    }
  }
  
  .header {
    padding: 0 16px;
    
    .header-right {
      gap: 4px;
    }
  }
}
</style>