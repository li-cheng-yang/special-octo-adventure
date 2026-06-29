<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">签到地图</h2>
        <p class="page-header-desc">查看学生签到打卡位置分布</p>
      </div>
    </div>

    <div v-if="pageError" class="error-fallback">
      <el-result icon="error" title="页面加载失败" :sub-title="pageError">
        <template #extra>
          <el-button type="primary" @click="retryLoad">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <el-card v-else class="map-card animate-slide-up" v-loading="loading">
      <div id="amap-container" class="map-container"></div>
      <div class="map-overlay">
        <div class="map-stats">
          <div class="map-stat-item">
            <span class="map-stat-value">{{ markers.length }}</span>
            <span class="map-stat-label">打卡点</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { locationApi } from '@/api'

const loading = ref(false)
const markers = ref<any[]>([])
const pageError = ref('')

const loadLocations = async () => {
  loading.value = true
  pageError.value = ''
  try {
    const res = await locationApi.getTeacherRecords({ page: 0, size: 100 })
    if (res.code === 200) {
      const data = res.data
      let list: any[] = []
      if (Array.isArray(data)) { list = data }
      else if (data && Array.isArray(data.content)) { list = data.content }
      markers.value = list.filter((item: any) => item.latitude && item.longitude)
      initMap()
    }
  } catch (error: any) {
    console.error('[MapView] 加载失败:', error)
    pageError.value = error.message || '加载位置数据失败'
  } finally { loading.value = false }
}

const initMap = () => {
  // 这里使用简化的实现，实际使用时需要替换为真实的高德地图API
  const container = document.getElementById('amap-container')
  if (!container) return

  // 如果没有打卡数据，显示提示
  if (markers.value.length === 0) {
    container.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#999;">暂无打卡位置数据</div>'
    return
  }

  container.innerHTML = `
    <div style="padding:20px;">
      <h3>打卡位置列表</h3>
      <p style="color:#666;margin-bottom:16px;">提示：集成高德地图需要在 public/index.html 中添加高德地图JS SDK</p>
      <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:12px;">
        ${markers.value.map((m, i) => `
          <div style="background:#f8fafc;border-radius:8px;padding:12px;border:1px solid #e2e8f0;">
            <div style="font-weight:600;margin-bottom:4px;">${m.studentName || '未知'} - ${m.recordType === 'CHECK_IN' ? '上班打卡' : '下班打卡'}</div>
            <div style="font-size:13px;color:#64748b;">${m.address || '未知位置'}</div>
            <div style="font-size:12px;color:#94a3b8;margin-top:4px;">经纬度: ${m.longitude}, ${m.latitude}</div>
            <div style="font-size:12px;color:#94a3b8;">${m.recordTime || ''}</div>
          </div>
        `).join('')}
      </div>
    </div>
  `
}

const retryLoad = () => { pageError.value = ''; loadLocations() }

onMounted(() => loadLocations())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.error-fallback { padding: 40px; }

.map-card { position: relative; border-radius: var(--radius-lg); }
.map-container { width: 100%; height: 600px; border-radius: var(--radius-md); overflow: auto; }

.map-overlay {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
}

.map-stats {
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-md);
  padding: 12px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.map-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.map-stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-600);
}

.map-stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .map-container { height: 400px; }
}
</style>
