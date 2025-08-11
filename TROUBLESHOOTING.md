# SolanaActionsContentScript 错误排查指南

## 错误描述
```
Error: Something went wrong.
    at N2 (solanaActionsContentScript.js:28:205753)
    at async H8 (solanaActionsContentScript.js:1215:363)
```

## 排查步骤

### 1. 浏览器扩展检查
- 打开浏览器扩展管理页面 (`chrome://extensions/` 或 `edge://extensions/`)
- 查找 Solana 相关扩展（Phantom、Solflare、Backpack 等）
- 尝试禁用扩展后重新测试

### 2. 控制台检查
```bash
# 打开浏览器开发者工具
按 F12 或右键 -> 检查元素

# 查看控制台标签页
- 寻找红色错误信息
- 查看网络标签页是否有失败的请求
- 检查是否有其他 JavaScript 错误
```

### 3. 清除浏览器数据
```bash
# Chrome/Edge
设置 -> 隐私和安全 -> 清除浏览数据
选择：
- Cookie 和其他网站数据
- 缓存的图片和文件
- 网站设置
```

### 4. 扩展更新
- 检查 Solana 钱包扩展是否有更新
- 更新到最新版本
- 重启浏览器

### 5. 兼容性检查
- 尝试在无痕模式下访问
- 尝试不同的浏览器
- 检查是否与其他扩展冲突

## 预防措施

### 项目级别
```javascript
// 在项目中添加错误边界处理
window.addEventListener('error', (event) => {
  if (event.filename && event.filename.includes('solanaActionsContentScript')) {
    console.warn('Solana扩展错误，已忽略:', event.error);
    event.preventDefault();
    return false;
  }
});

// 检测扩展存在性
const checkSolanaExtension = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const hasSolana = window.solana || window.phantom;
      resolve(hasSolana);
    }, 1000);
  });
};
```

### 用户级别
1. 保持扩展更新
2. 避免安装多个相同功能的钱包扩展
3. 定期清理浏览器缓存
4. 使用最新版本的浏览器

## 常见解决方案

| 症状 | 解决方案 |
|------|----------|
| 页面加载慢 | 禁用不必要的扩展 |
| 频繁报错 | 更新扩展版本 |
| 功能异常 | 清除扩展数据 |
| 内存占用高 | 重启浏览器 |

## 联系支持

如果问题持续存在：
1. 记录完整的错误信息
2. 提供浏览器和扩展版本
3. 描述复现步骤
4. 提供控制台日志截图