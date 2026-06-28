<template>
  <div class="app-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">
            <el-icon :size="22"><Collection /></el-icon>
          </div>
          <span class="logo-text" v-show="!isCollapsed">实习汇报系统</span>
        </div>
        <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
          <el-icon><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
        </button>
      </div>

      <div class="sidebar-role" v-show="!isCollapsed">
        <el-tag type="success" effect="dark" size="small" class="role-badge">
          <el-icon><UserFilled /></el-icon>
          学生端
        </el-tag>
      </div>

      <!-- 调试信息面板
      <div v-if="showDebug" class="debug-panel">
        <div class="debug-title">🔧 路由调试</div>
        <div class="debug-item">当前路径: {{ $route.path }}</div>
        <div class="debug-item">当前名称: {{ $route.name }}</div>
        <div class="debug-item">Token: {{ userStore.token ? '✅ 有' : '❌ 无' }}</div>
        <div class="debug-item">角色: {{ userStore.user?.role || '未获取' }}</div>
        <div class="debug-item" :class="{ 'debug-error': routerError }">
          路由错误: {{ routerError || '无' }}
        </div>
        <div class="debug-item" :class="{ 'debug-error': componentError }">
          组件错误: {{ componentError || '无' }}
        </div>
        <el-button size="small" type="warning" @click="clearErrors" style="margin-top: 4px">
          清除错误
        </el-button>
      </div> 
      -->

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          :class="['nav-item', { active: isActiveRoute(item.path) }]"
          @click="onMenuClick(item.path)"
        >
          <el-icon :size="18"><component :is="item.icon" /></el-icon>
          <span class="nav-text" v-show="!isCollapsed">{{ item.label }}</span>
          <div class="nav-active-indicator" v-show="!isCollapsed"></div>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-mini" v-show="!isCollapsed">
          <el-avatar v-if="userStore.user?.avatar" :size="32" :src="userStore.user.avatar" class="footer-avatar" />
          <el-avatar v-else :size="32" class="footer-avatar">{{ userStore.user?.realName?.charAt(0) }}</el-avatar>
          <div class="user-mini-info">
            <div class="user-mini-name">{{ userStore.user?.realName }}</div>
            <div class="user-mini-role">学生</div>
          </div>
        </div>
        <div class="footer-actions">
          <!--
          <el-tooltip :content="showDebug ? '关闭调试' : '开启调试'" placement="top">
            <button class="icon-btn debug-btn" @click="showDebug = !showDebug">
              <el-icon><Tools /></el-icon>
            </button>
          </el-tooltip>
          -->
          <el-tooltip content="退出登录" placement="top">
            <button class="icon-btn" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </button>
          </el-tooltip>
        </div>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="breadcrumb">
          <el-icon :size="16" class="breadcrumb-icon"><component :is="currentPageIcon" /></el-icon>
          <span class="page-name">{{ pageTitle }}</span>
        </div>
        <div class="header-actions">
          <el-tooltip content="个人中心" placement="bottom">
            <button class="header-btn" @click="$router.push('/profile')">
              <el-avatar v-if="userStore.user?.avatar" :size="28" :src="userStore.user.avatar" class="header-avatar" />
              <el-avatar v-else :size="28" class="header-avatar">{{ userStore.user?.realName?.charAt(0) }}</el-avatar>
              <span class="header-username">{{ userStore.user?.realName }}</span>
            </button>
          </el-tooltip>
        </div>
      </header>

      <div class="page-content">
        <!-- 带错误捕获的 router-view -->
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <div v-if="componentError" class="error-fallback">
              <el-result icon="error" :title="'组件加载失败'" :sub-title="componentError">
                <template #extra>
                  <el-button type="primary" @click="retryLoad">重新加载</el-button>
                  <el-button @click="goToInternships">返回实习信息</el-button>
                </template>
              </el-result>
            </div>
            <component v-else :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onErrorCaptured, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, Edit, MapLocation, UserFilled, Collection,
  Fold, Expand, SwitchButton, Tools,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapsed = ref(false)
const showDebug = ref(false)
const routerError = ref('')
const componentError = ref('')

// 监听路由错误
router.onError((err) => {
  console.error('[路由错误]', err)
  routerError.value = err.message || '路由加载失败'
  componentError.value = err.message || '组件加载失败'
})

// 监听路由变化
watch(() => route.path, (newPath, oldPath) => {
  console.log(`[路由变化] ${oldPath} -> ${newPath}`)
  componentError.value = ''
})

const menuItems = [
  { path: '/student/internships', label: '实习信息', icon: Document },
  { path: '/student/reports', label: '汇报管理', icon: Edit },
  { path: '/student/locations', label: '签到打卡', icon: MapLocation },
]

// 判断当前路由是否激活（支持子路由）
function isActiveRoute(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}

function onMenuClick(path: string) {
  console.log(`[菜单点击] 目标路径: ${path}, 当前路径: ${route.path}`)
  if (route.path === path) {
    console.log('[菜单点击] 已经在当前页面，无需跳转')
    return
  }
  // 清除之前的错误
  componentError.value = ''
  routerError.value = ''
}

function clearErrors() {
  componentError.value = ''
  routerError.value = ''
}

function retryLoad() {
  componentError.value = ''
  router.replace(route.path).catch(err => {
    console.error('[重新加载失败]', err)
    componentError.value = err.message
  })
}

function goToInternships() {
  componentError.value = ''
  router.push('/student/internships')
}

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/student/internships': '实习信息管理',
    '/student/internships/create': '新增实习信息',
    '/student/internships/edit': '编辑实习信息',
    '/student/reports': '汇报管理',
    '/student/reports/create': '提交汇报',
    '/student/reports/edit': '编辑汇报',
    '/student/locations': '我的位置',
  }
  // 尝试前缀匹配
  for (const [key, value] of Object.entries(titles)) {
    if (route.path.startsWith(key)) return value
  }
  return '学生端'
})

const currentPageIcon = computed(() => {
  const icons: Record<string, any> = {
    '/student/internships': Document,
    '/student/reports': Edit,
    '/student/locations': MapLocation,
  }
  for (const [key, value] of Object.entries(icons)) {
    if (route.path.startsWith(key)) return value
  }
  return Document
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  })
}

// 全局错误捕获
onErrorCaptured((err, instance, info) => {
  console.error('[组件错误捕获]', err, info)
  componentError.value = err instanceof Error ? err.message : String(err)
  return false // 阻止错误继续传播
})

onMounted(() => {
  console.log('[Layout] 已挂载')
  console.log('[Layout] 当前路由:', route.path)
  console.log('[Layout] 用户角色:', userStore.user?.role)
  console.log('[Layout] Token状态:', userStore.token ? '已登录' : '未登录')
})
</script>

<style scoped lang="scss">
.app-layout { display: flex; height: 100vh; background: var(--bg-primary); }

.sidebar { width: 250px; background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%); display: flex; flex-direction: column; transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1); position: relative; z-index: 100;
  &.collapsed { width: 68px; }
}

.sidebar-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 16px 12px; border-bottom: 1px solid rgba(255, 255, 255, 0.06); }
.logo { display: flex; align-items: center; gap: 10px; overflow: hidden; }
.logo-icon { width: 36px; height: 36px; border-radius: var(--radius-md); background: linear-gradient(135deg, var(--primary-500), var(--primary-600)); display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
.logo-text { font-size: 15px; font-weight: 700; color: #f1f5f9; white-space: nowrap; transition: opacity 0.2s; }

.collapse-btn { background: rgba(255, 255, 255, 0.06); border: none; color: #94a3b8; width: 28px; height: 28px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast);
  &:hover { background: rgba(255, 255, 255, 0.12); color: #e2e8f0; }
}

.sidebar-role { padding: 12px 16px; }
.role-badge { border-radius: var(--radius-sm); font-weight: 600; }

/* 调试面板 */
.debug-panel { margin: 8px 12px; padding: 10px; background: rgba(0, 0, 0, 0.4); border-radius: var(--radius-md); border: 1px solid rgba(234, 179, 8, 0.3); font-size: 11px; color: #eab308; }
.debug-title { font-weight: 700; margin-bottom: 6px; color: #fbbf24; font-size: 12px; }
.debug-item { padding: 2px 0; word-break: break-all; }
.debug-error { color: #f87171; font-weight: 600; }

.sidebar-nav { flex: 1; padding: 8px 12px; display: flex; flex-direction: column; gap: 4px; overflow-y: auto; }

.nav-item { display: flex; align-items: center; gap: 12px; padding: 11px 14px; border-radius: var(--radius-md); color: #94a3b8; text-decoration: none; font-size: 14px; font-weight: 500; transition: all var(--transition-fast); position: relative; overflow: hidden;
  &:hover { background: rgba(255, 255, 255, 0.06); color: #e2e8f0; }
  &.active { background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(99, 102, 241, 0.1)); color: #c7d2fe; font-weight: 600;
    .nav-active-indicator { opacity: 1; }
  }
  .el-icon { flex-shrink: 0; }
}

.nav-text { white-space: nowrap; transition: opacity 0.2s; }
.nav-active-indicator { position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 3px; height: 20px; background: linear-gradient(180deg, var(--primary-400), var(--primary-500)); border-radius: 0 3px 3px 0; opacity: 0; transition: opacity var(--transition-fast); }

.sidebar-footer { padding: 12px; border-top: 1px solid rgba(255, 255, 255, 0.06); display: flex; align-items: center; justify-content: space-between; gap: 8px; }

.user-mini { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.footer-avatar { background: linear-gradient(135deg, var(--primary-500), var(--accent-500)); color: #fff; font-size: 14px; font-weight: 600; flex-shrink: 0; }
.user-mini-name { font-size: 13px; font-weight: 600; color: #e2e8f0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-mini-role { font-size: 11px; color: #64748b; margin-top: 1px; }

.footer-actions { display: flex; gap: 6px; }
.icon-btn { background: rgba(255, 255, 255, 0.06); border: none; color: #94a3b8; width: 32px; height: 32px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast);
  &:hover { background: rgba(244, 63, 94, 0.2); color: #fb7185; }
}
.debug-btn:hover { background: rgba(234, 179, 8, 0.2); color: #fbbf24; }

.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

.top-header { height: 64px; background: #ffffff; border-bottom: 1px solid var(--border-color); display: flex; align-items: center; justify-content: space-between; padding: 0 28px; flex-shrink: 0; }

.breadcrumb { display: flex; align-items: center; gap: 10px; }
.breadcrumb-icon { color: var(--primary-500); }
.page-name { font-size: 16px; font-weight: 600; color: var(--gray-800); }

.header-actions { display: flex; align-items: center; gap: 8px; }
.header-btn { display: flex; align-items: center; gap: 10px; padding: 6px 12px; background: transparent; border: 1px solid var(--border-color); border-radius: var(--radius-full); cursor: pointer; transition: all var(--transition-fast);
  &:hover { background: var(--gray-50); border-color: var(--primary-200); }
}
.header-avatar { background: linear-gradient(135deg, var(--primary-500), var(--primary-600)); color: #fff; font-size: 12px; }
.header-username { font-size: 13px; font-weight: 600; color: var(--gray-700); }

.page-content { flex: 1; overflow-y: auto; padding: 24px 28px; background: var(--bg-primary); }

/* 错误回退页面 */
.error-fallback { padding: 40px; }

.page-enter-active, .page-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.page-enter-from { opacity: 0; transform: translateY(6px); }
.page-leave-to { opacity: 0; transform: translateY(-3px); }
</style>
