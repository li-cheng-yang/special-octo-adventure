<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">消息中心</h2>
        <p class="page-header-desc">查看所有通知消息</p>
      </div>
      <el-button v-if="unreadCount > 0" type="primary" plain @click="markAllRead">
        <el-icon><Check /></el-icon> 全部已读
      </el-button>
    </div>

    <el-card class="notification-page-card animate-slide-up" v-loading="loading">
      <div v-if="notifications.length === 0" class="empty-state">
        <el-empty description="暂无通知" />
      </div>

      <div v-else class="notification-page-list">
        <div
          v-for="item in notifications"
          :key="item.id"
          :class="['notification-page-item', { unread: !item.isRead }]"
          @click="handleClick(item)"
        >
          <div class="notification-page-dot" v-if="!item.isRead"></div>
          <div class="notification-page-icon" :style="{ background: getTypeColor(item.type) }">
            <el-icon :size="18" color="#fff"><Bell /></el-icon>
          </div>
          <div class="notification-page-content">
            <div class="notification-page-title">{{ item.title }}</div>
            <div class="notification-page-desc">{{ item.content }}</div>
            <div class="notification-page-meta">
              <el-tag size="small" effect="plain">{{ getTypeLabel(item.type) }}</el-tag>
              <span class="notification-page-time">{{ item.createdAt }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-wrapper" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadNotifications"
          @current-change="loadNotifications"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { notificationApi } from '@/api'
import { Bell, Check } from '@element-plus/icons-vue'

const router = useRouter()
const notifications = ref<any[]>([])
const unreadCount = ref(0)
const loading = ref(false)
const pagination = reactive({ page: 1, size: 20, total: 0 })

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await notificationApi.getList({ page: pagination.page - 1, size: pagination.size })
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) { notifications.value = data; pagination.total = data.length }
      else if (data && Array.isArray(data.content)) { notifications.value = data.content; pagination.total = data.totalElements || 0 }
    }
    const countRes = await notificationApi.getUnreadCount()
    if (countRes.code === 200) unreadCount.value = countRes.data.count || 0
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const markAllRead = async () => {
  try {
    const res = await notificationApi.markAllRead()
    if (res.code === 200) {
      unreadCount.value = 0
      notifications.value.forEach(n => n.isRead = true)
      ElMessage.success('已全部标记为已读')
    }
  } catch (error) { console.error(error) }
}

const handleClick = (item: any) => {
  item.isRead = true
  if (item.relatedType === 'INTERNSHIP') router.push('/student/internships')
  else if (item.relatedType === 'REPORT') router.push('/student/reports')
}

const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    INTERNSHIP_REVIEWED: '#6366f1',
    REPORT_REVIEWED: '#22c55e',
    CHECKIN_REMIND: '#f59e0b',
    SYSTEM_NOTICE: '#0ea5e9',
    DEADLINE_WARNING: '#f43f5e',
  }
  return colors[type] || '#94a3b8'
}

const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    INTERNSHIP_REVIEWED: '实习审核',
    REPORT_REVIEWED: '汇报批阅',
    CHECKIN_REMIND: '打卡提醒',
    SYSTEM_NOTICE: '系统通知',
    DEADLINE_WARNING: '截止日期',
  }
  return labels[type] || type
}

onMounted(() => loadNotifications())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; max-width: 900px; margin: 0 auto; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.notification-page-card { border-radius: var(--radius-lg); }
.empty-state { padding: 60px 0; }

.notification-page-list { display: flex; flex-direction: column; }

.notification-page-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--border-light);
  position: relative;
  &:last-child { border-bottom: none; }
  &:hover { background: var(--gray-50); }
  &.unread { background: #eef2ff; }
}

.notification-page-dot {
  position: absolute;
  top: 20px;
  left: 6px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-500);
}

.notification-page-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-page-content {
  flex: 1;
  min-width: 0;
}

.notification-page-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--gray-800);
  margin-bottom: 4px;
}

.notification-page-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  margin-bottom: 8px;
}

.notification-page-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notification-page-time {
  font-size: 12px;
  color: var(--gray-400);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
