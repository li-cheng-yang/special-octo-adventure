<template>
  <router-view v-slot="{ Component }">
    <transition name="page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 应用启动时检查认证状态
onMounted(() => {
  // 如果有 token 但没有用户信息，尝试恢复用户状态
  if (userStore.isLoggedIn && !userStore.user) {
    // 可以在这里发送请求获取用户信息
    // 如果请求失败，API 拦截器会处理登出
  }
})
</script>

<style lang="scss">
@use "@/styles/variables.scss" as *;

:root {
  --primary-50: #eef2ff;
  --primary-100: #e0e7ff;
  --primary-200: #c7d2fe;
  --primary-300: #a5b4fc;
  --primary-400: #818cf8;
  --primary-500: #6366f1;
  --primary-600: #4f46e5;
  --primary-700: #4338ca;
  --primary-800: #3730a3;
  --primary-900: #312e81;

  --success-500: #22c55e;
  --warning-500: #f59e0b;
  --danger-500: #f43f5e;
  --info-500: #0ea5e9;

  --gray-50: #f8fafc;
  --gray-100: #f1f5f9;
  --gray-200: #e2e8f0;
  --gray-300: #cbd5e1;
  --gray-400: #94a3b8;
  --gray-500: #64748b;
  --gray-600: #475569;
  --gray-700: #334155;
  --gray-800: #1e293b;
  --gray-900: #0f172a;

  --bg-primary: #f8fafc;
  --border-color: #e2e8f0;
  --border-light: #f1f5f9;
  --text-muted: #94a3b8;

  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
  --radius-full: 9999px;

  --shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.08);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);

  --transition-fast: 0.15s ease;
  --transition-base: 0.25s ease;

  --mono: 'JetBrains Mono', ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: var(--gray-800);
  background: var(--bg-primary);
  -webkit-font-smoothing: antialiased;
}

/* 全局动画 */
.page-enter-active,
.page-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.page-enter-from { opacity: 0; transform: translateY(6px); }
.page-leave-to { opacity: 0; transform: translateY(-3px); }

.animate-fade-in {
  animation: fadeIn 0.5s ease forwards;
}
.animate-slide-up {
  animation: slideUp 0.5s ease forwards;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Element Plus 覆盖 */
.el-table {
  --el-table-border-color: var(--border-color);
  --el-table-header-bg-color: var(--gray-50);
}
.el-card {
  --el-card-border-radius: var(--radius-lg);
  --el-card-padding: 20px;
}
</style>
