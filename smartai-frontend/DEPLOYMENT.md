# SmartAI Assistant 前端部署指南

本文档详细说明了如何部署SmartAI Assistant前端项目到生产环境。

## 📋 部署前准备

### 系统要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0
- Web服务器 (Nginx推荐)
- SSL证书 (生产环境推荐)

### 环境检查

```bash
# 检查Node.js版本
node --version

# 检查npm版本
npm --version

# 检查系统资源
free -h
df -h
```

## 🚀 构建部署

### 1. 克隆代码

```bash
git clone <repository-url>
cd SmartAI-Assistant/smartai-frontend
```

### 2. 安装依赖

```bash
# 使用npm
npm ci

# 或使用yarn
yarn install --frozen-lockfile
```

### 3. 环境配置

创建生产环境配置文件：

```bash
# 创建生产环境变量文件
cp .env.example .env.production

# 编辑配置
vim .env.production
```

生产环境配置示例：

```env
# API配置
VITE_API_BASE_URL=https://api.yourdomain.com
VITE_WS_URL=wss://api.yourdomain.com/ws

# 应用配置
VITE_APP_TITLE=SmartAI Assistant
VITE_APP_VERSION=1.0.0

# 功能开关
VITE_ENABLE_MOCK=false
VITE_ENABLE_DEVTOOLS=false

# 安全配置
VITE_SECURE_COOKIES=true
VITE_ENABLE_CSP=true
```

### 4. 构建生产版本

```bash
# 构建
npm run build

# 检查构建结果
ls -la dist/
```

### 5. 构建产物优化

```bash
# 分析构建包大小
npm run build -- --analyze

# 检查构建质量
npm run lint
npm run type-check
```

## 🌐 Web服务器配置

### Nginx 配置

创建Nginx配置文件：

```nginx
# /etc/nginx/sites-available/smartai-frontend
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;
    
    # SSL配置
    ssl_certificate /path/to/ssl/cert.pem;
    ssl_certificate_key /path/to/ssl/private.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # 安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' ws: wss:";
    
    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;
    
    # 静态文件配置
    location / {
        root /var/www/smartai-frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
        
        # 缓存策略
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
            access_log off;
        }
        
        location ~* \.(html)$ {
            expires 1h;
            add_header Cache-Control "public";
        }
    }
    
    # API代理
    location /api {
        proxy_pass http://localhost:8090;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # WebSocket代理
    location /ws {
        proxy_pass http://localhost:8090;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 日志配置
    access_log /var/log/nginx/smartai-frontend.access.log;
    error_log /var/log/nginx/smartai-frontend.error.log;
}
```

启用站点：

```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/smartai-frontend /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重载配置
sudo systemctl reload nginx
```

### Apache 配置

如果使用Apache，创建虚拟主机配置：

```apache
<VirtualHost *:80>
    ServerName yourdomain.com
    DocumentRoot /var/www/smartai-frontend
    
    # 重定向到HTTPS
    Redirect permanent / https://yourdomain.com/
</VirtualHost>

<VirtualHost *:443>
    ServerName yourdomain.com
    DocumentRoot /var/www/smartai-frontend
    
    # SSL配置
    SSLEngine on
    SSLCertificateFile /path/to/ssl/cert.pem
    SSLCertificateKeyFile /path/to/ssl/private.key
    
    # 单页应用配置
    <Directory /var/www/smartai-frontend>
        Options -Indexes
        AllowOverride All
        Require all granted
        
        # 路由重写
        RewriteEngine On
        RewriteBase /
        RewriteRule ^index\.html$ - [L]
        RewriteCond %{REQUEST_FILENAME} !-f
        RewriteCond %{REQUEST_FILENAME} !-d
        RewriteRule . /index.html [L]
    </Directory>
    
    # 静态资源缓存
    <LocationMatch "\.(css|js|png|jpg|jpeg|gif|ico|svg)$">
        ExpiresActive On
        ExpiresDefault "access plus 1 year"
        Header append Cache-Control "public"
    </LocationMatch>
    
    # API代理
    ProxyPreserveHost On
    ProxyPass /api http://localhost:8090/api
    ProxyPassReverse /api http://localhost:8090/api
</VirtualHost>
```

## 🐳 Docker 部署

### Dockerfile

```dockerfile
# 多阶段构建
FROM node:18-alpine as builder

WORKDIR /app

# 复制依赖文件
COPY package*.json ./
COPY yarn.lock ./

# 安装依赖
RUN npm ci --only=production

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产镜像
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制Nginx配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动命令
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  smartai-frontend:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./ssl:/etc/nginx/ssl:ro
      - ./logs:/var/log/nginx
    environment:
      - NODE_ENV=production
    restart: unless-stopped
    depends_on:
      - smartai-backend
    networks:
      - smartai-network

  smartai-backend:
    image: smartai-backend:latest
    ports:
      - "8090:8090"
    networks:
      - smartai-network

networks:
  smartai-network:
    driver: bridge
```

构建和运行：

```bash
# 构建镜像
docker build -t smartai-frontend:latest .

# 使用Docker Compose运行
docker-compose up -d

# 查看日志
docker-compose logs -f smartai-frontend
```

## 📊 监控和日志

### 系统监控

```bash
# 安装监控工具
sudo apt install htop iotop

# 监控系统资源
htop

# 监控磁盘I/O
iotop

# 监控网络连接
netstat -tulpn | grep :80
```

### 日志配置

```bash
# 配置日志轮转
sudo vim /etc/logrotate.d/smartai-frontend

# 日志轮转配置
/var/log/nginx/smartai-frontend*.log {
    daily
    missingok
    rotate 52
    compress
    delaycompress
    notifempty
    postrotate
        systemctl reload nginx
    endscript
}
```

### 应用监控

```bash
# 安装PM2 (如果使用Node.js直接运行)
npm install -g pm2

# 使用PM2管理应用
pm2 start ecosystem.config.js
pm2 monit
pm2 logs
```

## 🔧 性能优化

### 1. CDN配置

```javascript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      external: ['vue', 'element-plus'],
      output: {
        globals: {
          vue: 'Vue',
          'element-plus': 'ElementPlus'
        }
      }
    }
  }
})
```

### 2. 缓存策略

```nginx
# 强缓存配置
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
    add_header Vary "Accept-Encoding";
}

# 协商缓存配置
location ~* \.(html|json)$ {
    expires 1h;
    add_header Cache-Control "public, must-revalidate";
    etag on;
}
```

### 3. 压缩优化

```nginx
# Brotli压缩 (需要安装nginx-module-brotli)
load_module modules/ngx_http_brotli_filter_module.so;
load_module modules/ngx_http_brotli_static_module.so;

http {
    brotli on;
    brotli_comp_level 6;
    brotli_types
        text/plain
        text/css
        application/json
        application/javascript
        text/xml
        application/xml
        application/xml+rss
        text/javascript;
}
```

## 🔒 安全配置

### 1. 防火墙设置

```bash
# UFW防火墙配置
sudo ufw enable
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 8090/tcp  # 禁止直接访问后端端口
```

### 2. SSL/TLS配置

```bash
# 使用Let's Encrypt获取证书
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com

# 自动续期
sudo crontab -e
# 添加: 0 12 * * * /usr/bin/certbot renew --quiet
```

### 3. 安全头配置

```nginx
# 安全响应头
add_header X-Frame-Options DENY always;
add_header X-Content-Type-Options nosniff always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Permissions-Policy "camera=(), microphone=(), geolocation=()" always;
```

## 🚨 故障排除

### 常见问题

1. **白屏问题**
   ```bash
   # 检查构建是否正确
   ls -la dist/
   
   # 检查Nginx错误日志
   tail -f /var/log/nginx/error.log
   ```

2. **API请求失败**
   ```bash
   # 检查后端服务状态
   systemctl status smartai-backend
   
   # 检查代理配置
   nginx -t
   ```

3. **静态资源404**
   ```bash
   # 检查文件权限
   ls -la /var/www/smartai-frontend/
   
   # 设置正确权限
   sudo chown -R www-data:www-data /var/www/smartai-frontend/
   sudo chmod -R 755 /var/www/smartai-frontend/
   ```

### 性能问题诊断

```bash
# 检查网站性能
curl -o /dev/null -s -w "时间_总计: %{time_total}s\n" https://yourdomain.com

# 检查DNS解析
dig yourdomain.com

# 检查SSL握手时间
openssl s_client -connect yourdomain.com:443
```

## 📈 维护和更新

### 更新流程

```bash
#!/bin/bash
# deploy.sh - 部署脚本

set -e

echo "开始部署SmartAI Frontend..."

# 1. 备份当前版本
sudo cp -r /var/www/smartai-frontend /var/www/smartai-frontend.backup.$(date +%Y%m%d_%H%M%S)

# 2. 拉取最新代码
git pull origin main

# 3. 安装依赖
npm ci

# 4. 构建
npm run build

# 5. 部署
sudo rm -rf /var/www/smartai-frontend/*
sudo cp -r dist/* /var/www/smartai-frontend/

# 6. 设置权限
sudo chown -R www-data:www-data /var/www/smartai-frontend/
sudo chmod -R 755 /var/www/smartai-frontend/

# 7. 重载Nginx
sudo nginx -t && sudo systemctl reload nginx

echo "部署完成!"
```

### 备份策略

```bash
#!/bin/bash
# backup.sh - 备份脚本

BACKUP_DIR="/backup/smartai-frontend"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份应用文件
tar -czf $BACKUP_DIR/app_$DATE.tar.gz /var/www/smartai-frontend/

# 备份配置文件
tar -czf $BACKUP_DIR/config_$DATE.tar.gz /etc/nginx/sites-available/smartai-frontend

# 清理旧备份 (保留30天)
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete

echo "备份完成: $BACKUP_DIR"
```

## 📞 技术支持

如果在部署过程中遇到问题，请参考：

1. **日志文件**
   - Nginx访问日志: `/var/log/nginx/smartai-frontend.access.log`
   - Nginx错误日志: `/var/log/nginx/smartai-frontend.error.log`
   - 系统日志: `/var/log/syslog`

2. **配置检查**
   ```bash
   nginx -t
   systemctl status nginx
   systemctl status smartai-backend
   ```

3. **联系方式**
   - 项目Issues: [GitHub Issues](https://github.com/your-org/SmartAI-Assistant/issues)
   - 邮箱: support@smartai-assistant.com
   - 文档: [在线文档](https://docs.smartai-assistant.com)

---

**注意**: 请确保在生产环境部署前在测试环境进行充分测试。