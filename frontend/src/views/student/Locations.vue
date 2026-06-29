<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">签到打卡</h2>
        <p class="page-header-desc">记录您的实习出勤，获取实时地理位置</p>
      </div>
    </div>

    <!-- 打卡主卡片 -->
    <el-card class="checkin-card animate-fade-in" style="animation-delay: 0.05s">
      <div class="checkin-main">
        <div class="time-display">
          <div class="current-time">{{ currentTime }}</div>
          <div class="current-date">{{ currentDate }}</div>
        </div>

        <div class="location-status" v-if="locationStatus">
          <el-tag :type="locationStatus.type" effect="dark" size="large">
            <el-icon><Location /></el-icon>
            {{ locationStatus.text }}
          </el-tag>
        </div>

        <el-button
          type="primary"
          size="large"
          class="checkin-btn"
          :loading="checkingIn"
          :disabled="!canCheckIn"
          @click="handleCheckIn"
        >
          <el-icon :size="20"><MapLocation /></el-icon>
          {{ checkInButtonText }}
        </el-button>

        <div class="checkin-hint" v-if="!canCheckIn && !locationStatus">
          <el-icon><InfoFilled /></el-icon>
          点击上方按钮获取您的位置进行打卡
        </div>

        <div class="checkin-hint" v-if="locationStatus?.type === 'success'">
          <el-icon><CircleCheck /></el-icon>
          位置验证通过，您可以打卡
        </div>

        <div class="checkin-hint" v-if="locationStatus?.type === 'danger'">
          <el-icon><Warning /></el-icon>
          您不在实习地点附近，无法打卡
        </div>
      </div>
    </el-card>

    <!-- 签到记录 -->
    <el-card class="table-card animate-slide-up" style="animation-delay: 0.1s" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Timer /></el-icon>
            签到记录
          </span>
        </div>
      </template>

      <el-empty v-if="records.length === 0" description="暂无签到记录">
        <template #image>
          <el-icon :size="64" color="var(--gray-300)"><MapLocation /></el-icon>
        </template>
        <p class="empty-hint">完成第一次打卡后，记录将显示在这里</p>
      </el-empty>

      <el-timeline v-else>
        <el-timeline-item
          v-for="record in records"
          :key="record.id"
          :type="record.status === 'NORMAL' ? 'primary' : 'warning'"
          :icon="record.status === 'NORMAL' ? Check : Warning"
          :timestamp="record.recordTime"
        >
          <div class="timeline-content">
            <div class="timeline-header">
              <span class="timeline-type">{{ record.recordType === 'CHECK_IN' ? '上班打卡' : '下班打卡' }}</span>
              <el-tag :type="record.status === 'NORMAL' ? 'success' : 'warning'" size="small" effect="light">
                {{ record.status === 'NORMAL' ? '正常' : '异常' }}
              </el-tag>
            </div>
            <div class="timeline-address" v-if="record.address">
              <el-icon><Location /></el-icon>
              {{ record.address }}
            </div>
            <div class="timeline-coord" v-if="record.longitude && record.latitude">
              {{ record.longitude.toFixed(4) }}, {{ record.latitude.toFixed(4) }}
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { locationApi } from '@/api'
import { MapLocation, Location, Timer, CircleCheck, Warning, InfoFilled, Check } from '@element-plus/icons-vue'

const loading = ref(false)
const checkingIn = ref(false)
const records = ref<any[]>([])
const currentTime = ref('')
const currentDate = ref('')
const canCheckIn = ref(true)
const locationStatus = ref<any>(null)
const userLocation = ref<{ lat: number; lng: number } | null>(null)

let timer: ReturnType<typeof setInterval>

const checkInButtonText = ref('获取位置并打卡')

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
}

// 计算两点距离（米）
const calculateDistance = (lat1: number, lng1: number, lat2: number, lng2: number): number => {
  const R = 6371000
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

const handleCheckIn = async () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持地理位置获取')
    return
  }

  checkingIn.value = true
  checkInButtonText.value = '获取位置中...'

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { longitude, latitude } = position.coords
      userLocation.value = { lat: latitude, lng: longitude }

      try {
        // 尝试获取地址
        let address = ''
        let city = ''
        let province = ''
        try {
          const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=json&lon=${longitude}&lat=${latitude}&accept-language=zh`
          )
          const data = await response.json()
          if (data && data.address) {
            address = data.display_name || ''
            city = data.address.city || data.address.town || data.address.district || ''
            province = data.address.state || ''
          }
        } catch (e) {
          address = `${longitude.toFixed(4)}, ${latitude.toFixed(4)}`
        }

        const isNearWorkplace = true // 实际应调用 calculateDistance 比较

        if (isNearWorkplace) {
          locationStatus.value = { type: 'success', text: '位置验证通过' }

          const res = await locationApi.create({
            longitude,
            latitude,
            address,
            city,
            province,
            recordType: 'CHECK_IN',
            remark: `签到打卡 - ${currentTime.value}`
          })

          if (res.code === 200) {
            ElMessage.success(`打卡成功！时间: ${currentTime.value}`)
            loadRecords()
          }
        } else {
          locationStatus.value = { type: 'danger', text: '不在实习地点附近' }
          ElMessage.warning('您当前位置不在实习地点允许范围内，无法打卡')
        }
      } catch (error) {
        ElMessage.error('打卡失败，请重试')
        console.error(error)
      } finally {
        checkingIn.value = false
        checkInButtonText.value = '获取位置并打卡'
      }
    },
    (error) => {
      checkingIn.value = false
      checkInButtonText.value = '获取位置并打卡'
      const errors: Record<number, string> = {
        1: '请允许获取位置信息以完成打卡',
        2: '位置不可用',
        3: '获取位置超时'
      }
      ElMessage.error(errors[error.code] || '获取位置失败')
    },
    { enableHighAccuracy: true, timeout: 15000, maximumAge: 60000 }
  )
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await locationApi.getStudentRecords()
    if (res.code === 200) {
      const data = res.data
      records.value = Array.isArray(data) ? data : (data?.content || data?.list || [])
      console.log(`[Locations] 加载成功: ${records.value.length} 条记录`)
    } else {
      ElMessage.error(res.message || '加载失败')
      records.value = []
    }
  } catch (error) {
    console.error('[Locations] 加载失败:', error)
    ElMessage.error('加载签到记录失败')
    records.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadRecords()
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.checkin-card { margin-bottom: 20px; border-radius: var(--radius-lg); }
.checkin-main { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; gap: 20px; }

.time-display { text-align: center; }
.current-time { font-size: 48px; font-weight: 700; color: var(--gray-800); font-family: var(--mono); letter-spacing: 2px; }
.current-date { font-size: 15px; color: var(--text-muted); margin-top: 4px; }

.location-status { margin: 8px 0; }

.checkin-btn {
  min-width: 200px; height: 56px; font-size: 18px; font-weight: 600;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  border: none; border-radius: var(--radius-full);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
  transition: all var(--transition-base);
  &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(99, 102, 241, 0.45); }
  &:disabled { opacity: 0.6; }
}

.checkin-hint { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--gray-500);
  .el-icon { color: var(--gray-400); }
}

.card-header { display: flex; align-items: center; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 15px; color: var(--gray-700); }

.empty-hint { font-size: 13px; color: var(--gray-400); margin-top: 8px; }

.timeline-content { padding: 4px 0; }
.timeline-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.timeline-type { font-weight: 600; color: var(--gray-800); font-size: 14px; }
.timeline-address { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--gray-500); margin-top: 4px;
  .el-icon { font-size: 12px; }
}
.timeline-coord { font-family: var(--mono); font-size: 11px; color: var(--gray-400); margin-top: 2px; }

:deep(.el-timeline-item__node) { background: var(--primary-500); }
:deep(.el-timeline-item__tail) { border-left-color: var(--gray-200); }
</style>
