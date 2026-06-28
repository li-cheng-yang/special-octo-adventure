<template>
  <div class="dashboard-wrapper" v-loading="loading">
    <!-- 未登录提示 -->
    <div v-if="!isLoggedIn" class="not-login">
      <div class="not-login-icon">
        <el-icon :size="64" color="var(--warning-500)"><WarningFilled /></el-icon>
      </div>
      <h2 class="not-login-title">未登录</h2>
      <p class="not-login-desc">请先登录后查看仪表盘</p>
      <el-button type="primary" size="large" @click="goToLogin" class="login-btn">
        去登录
      </el-button>
    </div>

    <!-- 登录后显示数据 -->
    <template v-else>
      <!-- 仪表盘头部 -->
      <div class="dashboard-header animate-fade-in">
        <div class="header-left">
          <h1 class="header-title">仪表盘</h1>
          <p class="header-subtitle">欢迎回来，{{ userStore.user?.realName || '用户' }}</p>
        </div>
        <div class="header-right">
          <el-tag :type="roleTagType" effect="dark" size="large" class="role-tag">
            {{ roleLabel }}
          </el-tag>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid animate-fade-in" style="animation-delay: 0.05s">
        <div v-for="(card, index) in statCards" :key="index" class="stat-card" :class="card.type">
          <div class="stat-icon">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </div>

      <!-- 详细统计 -->
      <div class="detail-grid animate-slide-up" style="animation-delay: 0.1s">
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>实习状态统计</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div v-for="(item, idx) in internshipStats" :key="idx" class="bar-item">
              <div class="bar-label">{{ item.label }}</div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
              <div class="bar-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><Edit /></el-icon>
              <span>汇报状态统计</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div v-for="(item, idx) in reportStats" :key="idx" class="bar-item">
              <div class="bar-label">{{ item.label }}</div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
              <div class="bar-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { statsApi } from '@/api'
import { User, Document, Edit, Check, WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const stats = ref<any>({})

// 检查登录状态 - 直接读取 token
const isLoggedIn = computed(() => {
  return !!userStore.token && userStore.token.length > 0
})

const goToLogin = () => {
  router.push('/login')
}

const roleLabel = computed(() => {
  const labels: Record<string, string> = { STUDENT: '学生', TEACHER: '导师', ADMIN: '管理员' }
  return labels[userStore.user?.role || ''] || ''
})

const roleTagType = computed(() => {
  const types: Record<string, string> = { STUDENT: 'success', TEACHER: 'warning', ADMIN: 'danger' }
  return types[userStore.user?.role || ''] || ''
})

// 统计卡片
const statCards = computed(() => [
  { type: 'primary', icon: 'User', value: stats.value.totalStudents || 0, label: '学生人数' },
  { type: 'warning', icon: 'User', value: stats.value.totalTeachers || 0, label: '导师人数' },
  { type: 'success', icon: 'Document', value: stats.value.ongoingInternships || 0, label: '进行中实习' },
  { type: 'info', icon: 'Edit', value: stats.value.submittedReports || 0, label: '待批阅汇报' },
  { type: 'primary', icon: 'Check', value: stats.value.completedInternships || 0, label: '已完成实习' },
  { type: 'success', icon: 'Check', value: stats.value.reviewedReports || 0, label: '已批阅汇报' },
])

// 实习统计
const internshipStats = computed(() => {
  const max = Math.max(
    stats.value.ongoingInternships || 0,
    stats.value.pendingInternships || 0,
    stats.value.completedInternships || 0,
    stats.value.draftInternships || 0,
    1
  )
  return [
    { label: '进行中', value: stats.value.ongoingInternships || 0, color: '#22c55e', percent: ((stats.value.ongoingInternships || 0) / max) * 100 },
    { label: '待审核', value: stats.value.pendingInternships || 0, color: '#f59e0b', percent: ((stats.value.pendingInternships || 0) / max) * 100 },
    { label: '已完成', value: stats.value.completedInternships || 0, color: '#6366f1', percent: ((stats.value.completedInternships || 0) / max) * 100 },
    { label: '草稿', value: stats.value.draftInternships || 0, color: '#94a3b8', percent: ((stats.value.draftInternships || 0) / max) * 100 },
  ]
})

// 汇报统计
const reportStats = computed(() => {
  const max = Math.max(
    stats.value.submittedReports || 0,
    stats.value.reviewedReports || 0,
    stats.value.returnedReports || 0,
    stats.value.draftReports || 0,
    1
  )
  return [
    { label: '待批阅', value: stats.value.submittedReports || 0, color: '#f59e0b', percent: ((stats.value.submittedReports || 0) / max) * 100 },
    { label: '已批阅', value: stats.value.reviewedReports || 0, color: '#22c55e', percent: ((stats.value.reviewedReports || 0) / max) * 100 },
    { label: '已退回', value: stats.value.returnedReports || 0, color: '#f43f5e', percent: ((stats.value.returnedReports || 0) / max) * 100 },
    { label: '草稿', value: stats.value.draftReports || 0, color: '#94a3b8', percent: ((stats.value.draftReports || 0) / max) * 100 },
  ]
})

const loadStats = async () => {
  if (!isLoggedIn.value) {
    return
  }

  loading.value = true
  try {
    const res = await statsApi.getOverview()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.dashboard-wrapper { padding: 4px; }

// 未登录提示
.not-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}
.not-login-icon { margin-bottom: 16px; }
.not-login-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0 0 8px; }
.not-login-desc { font-size: 14px; color: var(--text-muted); margin: 0 0 24px; }
.login-btn { min-width: 120px; }

// 仪表盘头部
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.header-title { font-size: 24px; font-weight: 700; color: var(--gray-800); margin: 0; }
.header-subtitle { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }
.role-tag { border-radius: var(--radius-md); font-weight: 600; }

// 统计卡片
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }

.stat-card { background: #ffffff; border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 20px; display: flex; align-items: center; gap: 16px; transition: all var(--transition-base);
  &:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
  &.primary { border-left: 4px solid var(--primary-500); .stat-icon { background: #eef2ff; color: var(--primary-600); } }
  &.success { border-left: 4px solid var(--success-500); .stat-icon { background: #f0fdf4; color: var(--success-600); } }
  &.warning { border-left: 4px solid var(--warning-500); .stat-icon { background: #fffbeb; color: var(--warning-600); } }
  &.info { border-left: 4px solid var(--info-500); .stat-icon { background: #f0f9ff; color: var(--info-600); } }
}
.stat-icon { width: 48px; height: 48px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--gray-800); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--text-muted); margin-top: 2px; }

// 详细统计
.detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
@media (max-width: 768px) { .detail-grid { grid-template-columns: 1fr; } }

.detail-card { border-radius: var(--radius-lg); }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--gray-700); }

.bar-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.bar-label { width: 60px; font-size: 13px; color: var(--gray-600); text-align: right; flex-shrink: 0; }
.bar-track { flex: 1; height: 8px; background: var(--gray-100); border-radius: var(--radius-full); overflow: hidden; }
.bar-fill { height: 100%; border-radius: var(--radius-full); transition: width 0.5s ease; }
.bar-value { width: 40px; font-size: 14px; font-weight: 600; color: var(--gray-700); text-align: right; flex-shrink: 0; }
</style>
