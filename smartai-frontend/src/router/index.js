import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '仪表板',
          icon: 'Dashboard'
        }
      },
      {
        path: 'task',
        name: 'TaskManagement',
        component: () => import('@/views/TaskManagement.vue'),
        meta: {
          title: '任务管理',
          icon: 'List'
        }
      },
      {
        path: 'manus',
        name: 'ManusAgent',
        component: () => import('@/views/ManusAgent.vue'),
        meta: {
          title: 'ManusAgent',
          icon: 'Cpu'
        }
      },
      {
        path: 'agent',
        name: 'SmartAgent',
        component: () => import('@/views/SmartAgent.vue'),
        meta: {
          title: '智能Agent',
          icon: 'Avatar'
        }
      },
      {
        path: 'reasoning',
        name: 'Reasoning',
        component: () => import('@/views/Reasoning.vue'),
        meta: {
          title: '深度推理',
          icon: 'BrainFull'
        }
      },
      {
        path: 'tools',
        name: 'Tools',
        component: () => import('@/views/Tools.vue'),
        meta: {
          title: '工具管理',
          icon: 'Tools'
        }
      },
      {
        path: 'monitoring',
        name: 'Monitoring',
        component: () => import('@/views/Monitoring.vue'),
        meta: {
          title: '系统监控',
          icon: 'Monitor'
        }
      },
      {
        path: 'knowledge-base',
        name: 'KnowledgeBase',
        component: () => import('@/views/KnowledgeBase.vue'),
        meta: {
          title: '知识库管理',
          icon: 'Files'
        }
      },
      {
        path: 'chat',
        name: 'ChatPage',
        component: () => import('@/views/ChatPage.vue'),
        meta: {
          title: '智能对话',
          icon: 'ChatDotRound'
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: {
          title: '系统设置',
          icon: 'Setting'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面未找到'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - SmartAI Assistant`
  } else {
    document.title = 'SmartAI Assistant'
  }
  
  next()
})

export default router