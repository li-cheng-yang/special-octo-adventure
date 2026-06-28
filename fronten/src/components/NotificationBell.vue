<template>
  <div class="notification-bell">
    <el-popover
      placement="bottom-end"
      :width="360"
      trigger="click"
      popper-class="notification-popover"
      @show="onPopoverShow"
    >
      <template #reference>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="bell-badge">
          <el-button circle class="bell-btn">
            <el-icon :size="18"><Bell /></el-icon>
          </el-button>
        </el-badge>
      </template>

      <div class="notification-panel">
        <div class="notification-header">
          <span class="notification-title">消息通知</span>
          <el-button v-if="unreadCount > 0" link type="primary" size="small" @click="markAllRead">
            全部已读
          </el-button>
        </div>

        <div class="notification-list" v-loading="loading">
          <div v-if="notifications.length === 0" class="notification-empty">
            <el-icon :size="40" class="empty-icon"><Bell /></el-icon>
            <p>暂无通知</p>
          </div>

          <div
            v-for="item in notifications"
            :key="item.id"
            :class="['notification-item', { unread: !item.isRead }]"
            @click="handleClick(item)"
          >
            <div class="notification-dot" v-if="!item.isRead"></div>
            <div class="notification-content">
              <div class="notification-item-title">{{ item.title }}</div>
              <div class="notification-item-desc">{{ item.content }}</div>
              <div class="notification-item-time">{{ formatTime(item.createdAt) }}</div>
            </div>
          </div>
        </div>

        <div class="notification-footer">
          <el-button link type="primary" size="small" @click="$router.push('/notifications')">
            查看全部
          </el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { notificationApi } from '@/api'

const router = useRouter()
const notifications = ref<any[]>([])
const unreadCount = ref(0)
const loading = ref(false)
let pollTimer: number | null = null

const loadNotifications = async () => {
  try {
    const res = await notificationApi.getLatest()
    if (res.code === 200) {
      notifications.value = res.data || []
    }
    const countRes = await notificationApi.getUnreadCount()
    if (countRes.code === 200) {
      unreadCount.value = countRes.data.count || 0
    }
  } catch (error) {
    console.error('[NotificationBell] 加载通知失败:', error)
  }
}

const onPopoverShow = () => {
  loading.value = true
  loadNotifications().finally(() => { loading.value = false })
}

const markAllRead = async () => {
  try {
    const res = await notificationApi.markAllRead()
    if (res.code === 200) {
      unreadCount.value = 0
      notifications.value.forEach(n => n.isRead = true)
      ElMessage.success('已全部标记为已读')
    }
  } catch (error) {
    console.error(error)
  }
}

const handleClick = (item: any) => {
  item.isRead = true
  if (unreadCount.value > 0) unreadCount.value--
  // 根据通知类型跳转
  if (item.relatedType === 'INTERNSHIP') {
    router.push('/student/internships')
  } else if (item.relatedType === 'REPORT') {
    router.push('/student/reports')
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

onMounted(() => {
  loadNotifications()
  // 轮询获取未读数量（每30秒）
  pollTimer = window.setInterval(() => {
    notificationApi.getUnreadCount().then((res: any) => {
      if (res.code === 200) {
        unreadCount.value = res.data.count || 0
      }
    }).catch(() => {})
  }, 30000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped lang="scss">
.notification-bell {
  .bell-btn {
    width: 36px;
    height: 36px;
    background: transparent;
    border: 1px solid var(--border-color);
    &:hover {
      background: var(--gray-50);
      border-color: var(--primary-200);
    }
  }
}

.notification-panel {
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.notification-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--gray-800);
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 320px;
}

.notification-empty {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-muted);
  .empty-icon {
    margin-bottom: 8px;
    color: var(--gray-300);
  }
  p {
    margin: 0;
    font-size: 13px;
  }
}

.notification-item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  &:hover {
    background: var(--gray-50);
  }
  &.unread {
    background: #eef2ff;
    &:hover {
      background: #e0e7ff;
    }
  }
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-500);
  flex-shrink: 0;
  margin-top: 4px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-item-title {
  font-weight: 600;
  font-size: 13px;
  color: var(--gray-800);
  margin-bottom: 4px;
}

.notification-item-desc {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-item-time {
  font-size: 11px;
  color: var(--gray-400);
  margin-top: 4px;
}

.notification-footer {
  text-align: center;
  padding: 8px;
  border-top: 1px solid var(--border-light);
}
</style>
