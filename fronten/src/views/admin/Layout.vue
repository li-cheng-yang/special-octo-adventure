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
        <el-tag type="danger" effect="dark" size="small" class="role-badge">
          <el-icon><Monitor /></el-icon>
          管理员
        </el-tag>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          :class="['nav-item', { active: $route.path === item.path }]"
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
            <div class="user-mini-role">管理员</div>
          </div>
        </div>
        <div class="footer-actions">
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
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Document, DataAnalysis, Collection, OfficeBuilding,
  Monitor, Fold, Expand, SwitchButton,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapsed = ref(false)

const menuItems = [
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/companies', label: '企业管理', icon: OfficeBuilding },
  { path: '/admin/internships', label: '实习管理', icon: Document },
  { path: '/admin/statistics', label: '系统统计', icon: DataAnalysis },
]

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/users': '用户管理',
    '/admin/companies': '企业管理',
    '/admin/internships': '实习管理',
    '/admin/statistics': '系统统计',
  }
  return titles[route.path] || '管理员端'
})

const currentPageIcon = computed(() => {
  const icons: Record<string, any> = {
    '/admin/users': User,
    '/admin/companies': OfficeBuilding,
    '/admin/internships': Document,
    '/admin/statistics': DataAnalysis,
  }
  return icons[route.path] || User
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
</script>

<style scoped lang="scss">
.app-layout { display: flex; height: 100vh; background: var(--bg-primary); }

.sidebar { width: 250px; background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%); display: flex; flex-direction: column; transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1); position: relative; z-index: 100;
  &.collapsed { width: 68px; }
}

.sidebar-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 16px 12px; border-bottom: 1px solid rgba(255, 255, 255, 0.06); }
.logo { display: flex; align-items: center; gap: 10px; overflow: hidden; }
.logo-icon { width: 36px; height: 36px; border-radius: var(--radius-md); background: linear-gradient(135deg, var(--primary-500), var(--primary-600)); display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
.logo-text { font-size: 15px; font-weight: 700; color: #f1f5f9; white-space: nowrap; }

.collapse-btn { background: rgba(255, 255, 255, 0.06); border: none; color: #94a3b8; width: 28px; height: 28px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast);
  &:hover { background: rgba(255, 255, 255, 0.12); color: #e2e8f0; }
}

.sidebar-role { padding: 12px 16px; }
.role-badge { border-radius: var(--radius-sm); font-weight: 600; }

.sidebar-nav { flex: 1; padding: 8px 12px; display: flex; flex-direction: column; gap: 4px; }

.nav-item { display: flex; align-items: center; gap: 12px; padding: 11px 14px; border-radius: var(--radius-md); color: #94a3b8; text-decoration: none; font-size: 14px; font-weight: 500; transition: all var(--transition-fast); position: relative; overflow: hidden;
  &:hover { background: rgba(255, 255, 255, 0.06); color: #e2e8f0; }
  &.active { background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(99, 102, 241, 0.1)); color: #c7d2fe; font-weight: 600;
    .nav-active-indicator { opacity: 1; }
  }
  .el-icon { flex-shrink: 0; }
}

.nav-text { white-space: nowrap; }
.nav-active-indicator { position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 3px; height: 20px; background: linear-gradient(180deg, var(--primary-400), var(--primary-500)); border-radius: 0 3px 3px 0; opacity: 0; transition: opacity var(--transition-fast); }

.sidebar-footer { padding: 12px; border-top: 1px solid rgba(255, 255, 255, 0.06); display: flex; align-items: center; justify-content: space-between; gap: 8px; }

.user-mini { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.footer-avatar { background: linear-gradient(135deg, var(--primary-500), var(--accent-500)); color: #fff; font-size: 14px; font-weight: 600; flex-shrink: 0; }
.user-mini-name { font-size: 13px; font-weight: 600; color: #e2e8f0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-mini-role { font-size: 11px; color: #64748b; margin-top: 1px; }

.icon-btn { background: rgba(255, 255, 255, 0.06); border: none; color: #94a3b8; width: 32px; height: 32px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast);
  &:hover { background: rgba(244, 63, 94, 0.2); color: #fb7185; }
}

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

.page-enter-active, .page-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.page-enter-from { opacity: 0; transform: translateY(6px); }
.page-leave-to { opacity: 0; transform: translateY(-3px); }
</style>
