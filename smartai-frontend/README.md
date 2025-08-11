# SmartAI Assistant Frontend

基于Vue3 + Element Plus的SmartAI智能助手前端项目，为SmartAI Assistant后端系统提供现代化的Web界面。

## 🚀 项目特性

- **Vue3 + TypeScript**: 使用最新的Vue3 Composition API和TypeScript开发
- **Element Plus**: 基于Element Plus组件库构建现代化UI
- **Pinia状态管理**: 使用Pinia进行状态管理
- **Vue Router**: 单页面应用路由管理
- **Axios HTTP客户端**: 统一的API请求处理
- **Vite构建工具**: 快速的开发和构建体验
- **响应式设计**: 支持桌面端和移动端适配
- **暗色主题**: 支持明暗主题切换

## 📦 项目结构

```
smartai-frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口定义
│   │   ├── agent.ts       # Agent API
│   │   └── manus.ts       # ManusAgent API
│   ├── components/        # 公共组件
│   │   └── layout/        # 布局组件
│   ├── router/            # 路由配置
│   ├── stores/            # Pinia状态管理
│   │   ├── system.ts      # 系统状态
│   │   └── task.ts        # 任务状态
│   ├── styles/            # 样式文件
│   ├── types/             # TypeScript类型定义
│   ├── utils/             # 工具函数
│   ├── views/             # 页面组件
│   ├── App.vue            # 根组件
│   └── main.ts            # 应用入口
├── package.json
├── vite.config.ts         # Vite配置
└── tsconfig.json          # TypeScript配置
```

## 🛠️ 开发环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

## 📋 安装和启动

### 1. 安装依赖

```bash
npm install
# 或
yarn install
```

### 2. 启动开发服务器

```bash
npm run dev
# 或
yarn dev
```

开发服务器将在 `http://localhost:3000` 启动

### 3. 构建生产版本

```bash
npm run build
# 或
yarn build
```

### 4. 预览生产版本

```bash
npm run preview
# 或
yarn preview
```

## 🎯 主要功能模块

### 1. 仪表板 (Dashboard)
- 系统状态总览
- 任务统计信息
- 最近任务展示
- 快捷操作入口

### 2. ManusAgent
- 快速任务执行
- 高级任务配置
- 任务历史管理
- 实时执行状态

### 3. 智能Agent
- 复杂任务执行
- 知识库问答
- 报告生成
- 会议分析

### 4. 深度推理
- Chain-of-Thought推理
- 多种推理策略
- 推理过程展示
- 历史记录管理

### 5. 任务管理
- 任务列表查看
- 状态筛选搜索
- 任务详情查看
- 任务操作管理

### 6. 系统监控
- Agent状态监控
- 性能指标展示
- 实时数据更新
- 健康检查

## 🔧 配置说明

### API代理配置

在`vite.config.ts`中配置API代理：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8090',
      changeOrigin: true,
      secure: false
    }
  }
}
```

### 环境变量

创建`.env.local`文件配置环境变量：

```env
# API基础URL
VITE_API_BASE_URL=http://localhost:8090

# WebSocket URL
VITE_WS_URL=ws://localhost:8090/ws

# 应用标题
VITE_APP_TITLE=SmartAI Assistant
```

## 🎨 主题定制

项目支持主题定制，在`src/styles/index.scss`中修改CSS变量：

```scss
:root {
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  // ... 更多变量
}
```

## 📱 响应式设计

项目采用响应式设计，支持以下断点：

- 桌面端: >= 1024px
- 平板端: 768px - 1023px
- 移动端: < 768px

## 🔗 API集成

### 后端API对接

项目对接SmartAI Assistant后端的以下API：

- **ManusAgent API**: `/api/v1/manus/*`
- **智能Agent API**: `/api/v1/agent/*`
- **工具管理API**: `/api/v1/tools/*`

### 请求拦截器

所有API请求都经过统一的拦截器处理：

- 自动添加认证Token
- 统一错误处理
- 请求/响应日志
- 加载状态管理

## 🧪 开发指南

### 添加新页面

1. 在`src/views/`目录下创建页面组件
2. 在`src/router/index.ts`中添加路由配置
3. 在侧边栏导航中添加菜单项

### 添加新API

1. 在`src/types/api.ts`中定义类型
2. 在`src/api/`目录下创建API函数
3. 在组件中调用API函数

### 状态管理

使用Pinia进行状态管理：

```typescript
// 定义Store
export const useMyStore = defineStore('my-store', () => {
  const state = ref(initialState)
  
  const actions = {
    // actions
  }
  
  return { state, ...actions }
})

// 使用Store
const myStore = useMyStore()
```

## 🚀 部署说明

### Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://backend:8090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Docker部署

```dockerfile
FROM nginx:alpine

COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目地址: [SmartAI-Assistant](https://github.com/your-org/SmartAI-Assistant)
- 问题反馈: [Issues](https://github.com/your-org/SmartAI-Assistant/issues)
- 邮箱: support@smartai-assistant.com

## 🙏 致谢

感谢以下开源项目的支持：

- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Vite](https://vitejs.dev/)
- [Pinia](https://pinia.vuejs.org/)
- [TypeScript](https://www.typescriptlang.org/)