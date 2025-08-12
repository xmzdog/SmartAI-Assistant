import { createRouter, createWebHistory } from 'vue-router'

// 页面组件
import Dashboard from '@/views/Dashboard.vue'
import ChatPage from '@/views/ChatPage.vue'

// 智能体管理
import AgentManagement from '@/views/agent/AgentManagement.vue'
import AgentDetail from '@/views/agent/AgentDetail.vue'
import AgentChat from '@/views/agent/AgentChat.vue'

// 模型管理
import ModelManagement from '@/views/model/ModelManagement.vue'

// 知识库管理
import KnowledgeBase from '@/views/knowledge/KnowledgeBase.vue'

// 系统配置
import SystemPrompt from '@/views/system/SystemPrompt.vue'
import AdvisorConfig from '@/views/system/AdvisorConfig.vue'
import McpTools from '@/views/system/McpTools.vue'

// 任务管理
import TaskSchedule from '@/views/task/TaskSchedule.vue'

// 监控
import Monitoring from '@/views/Monitoring.vue'
import Settings from '@/views/Settings.vue'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { title: '仪表板', icon: 'Dashboard' }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatPage,
    meta: { title: '智能对话', icon: 'ChatLineSquare' }
  },
  {
    path: '/agent',
    name: 'AgentManagement',
    component: AgentManagement,
    meta: { title: '智能体列表', icon: 'User' }
  },
  {
    path: '/agent/chat',
    name: 'AgentChat',
    component: AgentChat,
    meta: { title: 'Agent对话', icon: 'ChatLineSquare' }
  },
  {
    path: '/agent/detail/:id?',
    name: 'AgentDetail',
    component: AgentDetail,
    meta: { title: '智能体详情', icon: 'User', hideInMenu: true }
  },
  {
    path: '/model',
    name: 'Model',
    component: ModelManagement,
    meta: { title: '模型管理', icon: 'Cpu' }
  },
  {
    path: '/knowledge',
    name: 'Knowledge',
    component: KnowledgeBase,
    meta: { title: '知识库', icon: 'FolderOpened' }
  },
  {
    path: '/system',
    name: 'System',
    meta: { title: '系统配置', icon: 'Setting' },
    children: [
      {
        path: 'prompt',
        name: 'SystemPrompt',
        component: SystemPrompt,
        meta: { title: '系统提示词' }
      },
      {
        path: 'advisor',
        name: 'AdvisorConfig',
        component: AdvisorConfig,
        meta: { title: '顾问配置' }
      },
      {
        path: 'tools',
        name: 'McpTools',
        component: McpTools,
        meta: { title: 'MCP工具' }
      }
    ]
  },
  {
    path: '/task',
    name: 'Task',
    component: TaskSchedule,
    meta: { title: '任务调度', icon: 'Timer' }
  },
  {
    path: '/monitoring',
    name: 'Monitoring',
    component: Monitoring,
    meta: { title: '系统监控', icon: 'Monitor' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { title: '系统设置', icon: 'Tools' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { hideInMenu: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router