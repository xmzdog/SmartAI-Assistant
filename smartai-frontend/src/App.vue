<template>
  <div id="app" class="app-container">
    <MainLayout />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useSystemStore } from '@/stores/system'
import MainLayout from '@/components/layout/MainLayout.vue'

const systemStore = useSystemStore()

onMounted(() => {
  // 初始化系统状态
  systemStore.init()
})
</script>

<style lang="scss">
.app-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

// 全局样式重置
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  background-color: #f5f5f5;
}

// 滚动条样式
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

// 动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-leave-to {
  transform: translateX(100%);
}

// 暗色主题
.dark {
  body {
    color: #e5e5e5;
    background-color: #1a1a1a;
  }
}
</style>