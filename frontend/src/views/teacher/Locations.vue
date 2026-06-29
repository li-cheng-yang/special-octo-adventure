<template>
  <div class="page-wrapper">
    <!-- 页面标题 -->
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">签到记录</h2>
        <p class="page-header-desc">查看学生的签到打卡记录</p>
      </div>
      <div class="page-header-actions">
        <el-tooltip content="刷新数据" placement="bottom">
          <el-button circle :icon="Refresh" @click="loadRecords" :loading="loading" />
        </el-tooltip>
        <el-tooltip content="切换视图" placement="bottom">
          <el-button circle :icon="viewMode === 'table' ? Grid : List" @click="toggleViewMode" />
        </el-tooltip>
        <el-button type="primary" :icon="Download" @click="exportRecords" :loading="exporting">
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar animate-fade-in" style="animation-delay: 0.05s">
      <div class="stat-pill">
        <el-icon :size="18" color="#6366f1"><MapLocation /></el-icon>
        <span class="stat-pill-value">{{ records.length }}</span>
        <span class="stat-pill-label">条记录</span>
      </div>
      <div class="stat-pill">
        <el-icon :size="18" color="#10b981"><CircleCheck /></el-icon>
        <span class="stat-pill-value">{{ checkInCount }}</span>
        <span class="stat-pill-label">上班打卡</span>
      </div>
      <div class="stat-pill">
        <el-icon :size="18" color="#f59e0b"><Timer /></el-icon>
        <span class="stat-pill-value">{{ checkOutCount }}</span>
        <span class="stat-pill-label">下班打卡</span>
      </div>
      <div class="stat-pill">
        <el-icon :size="18" color="#8b5cf6"><OfficeBuilding /></el-icon>
        <span class="stat-pill-value">{{ cityCount }}</span>
        <span class="stat-pill-label">个城市</span>
      </div>
    </div>

    <!-- 错误回退 -->
    <div v-if="pageError" class="error-fallback">
      <el-result icon="error" title="页面加载失败" :sub-title="pageError">
        <template #extra>
          <el-button type="primary" @click="retryLoad">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <!-- 表格视图 -->
    <el-card v-else-if="viewMode === 'table'" class="table-card animate-slide-up" v-loading="loading" shadow="never">
      <el-table :data="paginatedRecords" style="width: 100%" :header-cell-style="headerStyle" empty-text="暂无签到记录" highlight-current-row>
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="studentName" label="学生" width="110">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar :size="28" class="name-avatar">{{ row.studentName?.charAt(0) || '?' }}</el-avatar>
              <span>{{ row.studentName || '未知' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="120">
          <template #default="{ row }">
            <span class="student-no">{{ row.studentNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="recordType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.recordType === 'CHECK_IN' ? 'success' : 'warning'" size="small" effect="light" class="type-tag">
              {{ row.recordType === 'CHECK_IN' ? '上班打卡' : '下班打卡' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="打卡位置" min-width="200">
          <template #default="{ row }">
            <div class="location-cell">
              <el-icon><MapLocation /></el-icon>
              <span>{{ row.address || '未知位置' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.city" size="small" effect="plain" type="info">{{ row.city }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="recordTime" label="打卡时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.recordTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120">
          <template #default="{ row }">
            <span class="remark-text">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="records.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <!-- 卡片视图 -->
    <div v-else class="card-view animate-slide-up" style="animation-delay: 0.1s" v-loading="loading">
      <!-- 按打卡类型分组折叠面板 -->
      <el-collapse v-model="activeGroups" class="group-collapse">
        <!-- 上班打卡组 -->
        <el-collapse-item name="checkin" v-if="groupedRecords.checkin.length > 0">
          <template #title>
            <div class="group-title">
              <el-icon color="#16a34a"><CircleCheck /></el-icon>
              <span>上班打卡</span>
              <el-tag size="small" type="success" effect="dark">{{ groupedRecords.checkin.length }}</el-tag>
            </div>
          </template>
          <div class="card-grid">
            <el-card
              v-for="record in groupedRecords.checkin"
              :key="record.id"
              class="record-card checkin"
              shadow="hover"
              :body-style="{ padding: '16px' }"
            >
              <div class="card-header">
                <el-avatar :size="44" class="card-avatar">{{ record.studentName?.charAt(0) || '?' }}</el-avatar>
                <div class="card-header-info">
                  <div class="card-name">{{ record.studentName || '未知' }}</div>
                  <div class="card-no">{{ record.studentNo || '无学号' }}</div>
                </div>
                <el-tag size="small" type="success" effect="light" class="card-type-tag">上班打卡</el-tag>
              </div>
              <el-divider style="margin: 12px 0" />
              <div class="card-body">
                <div class="card-item">
                  <el-icon><MapLocation /></el-icon>
                  <span class="card-label">位置</span>
                  <span class="card-value" :title="record.address">{{ record.address || '未知位置' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span class="card-label">城市</span>
                  <span class="card-value">{{ record.city || '-' }} {{ record.province ? '(' + record.province + ')' : '' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Clock /></el-icon>
                  <span class="card-label">时间</span>
                  <span class="card-value">{{ record.recordTime }}</span>
                </div>
                <div class="card-item" v-if="record.remark">
                  <el-icon><Document /></el-icon>
                  <span class="card-label">备注</span>
                  <span class="card-value">{{ record.remark }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </el-collapse-item>

        <!-- 下班打卡组 -->
        <el-collapse-item name="checkout" v-if="groupedRecords.checkout.length > 0">
          <template #title>
            <div class="group-title">
              <el-icon color="#d97706"><Timer /></el-icon>
              <span>下班打卡</span>
              <el-tag size="small" type="warning" effect="dark">{{ groupedRecords.checkout.length }}</el-tag>
            </div>
          </template>
          <div class="card-grid">
            <el-card
              v-for="record in groupedRecords.checkout"
              :key="record.id"
              class="record-card checkout"
              shadow="hover"
              :body-style="{ padding: '16px' }"
            >
              <div class="card-header">
                <el-avatar :size="44" class="card-avatar checkout-avatar">{{ record.studentName?.charAt(0) || '?' }}</el-avatar>
                <div class="card-header-info">
                  <div class="card-name">{{ record.studentName || '未知' }}</div>
                  <div class="card-no">{{ record.studentNo || '无学号' }}</div>
                </div>
                <el-tag size="small" type="warning" effect="light" class="card-type-tag">下班打卡</el-tag>
              </div>
              <el-divider style="margin: 12px 0" />
              <div class="card-body">
                <div class="card-item">
                  <el-icon><MapLocation /></el-icon>
                  <span class="card-label">位置</span>
                  <span class="card-value" :title="record.address">{{ record.address || '未知位置' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span class="card-label">城市</span>
                  <span class="card-value">{{ record.city || '-' }} {{ record.province ? '(' + record.province + ')' : '' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Clock /></el-icon>
                  <span class="card-label">时间</span>
                  <span class="card-value">{{ record.recordTime }}</span>
                </div>
                <div class="card-item" v-if="record.remark">
                  <el-icon><Document /></el-icon>
                  <span class="card-label">备注</span>
                  <span class="card-value">{{ record.remark }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </el-collapse-item>

        <!-- 其他类型组 -->
        <el-collapse-item name="other" v-if="groupedRecords.other.length > 0">
          <template #title>
            <div class="group-title">
              <el-icon color="#6366f1"><MoreFilled /></el-icon>
              <span>其他类型</span>
              <el-tag size="small" type="info" effect="dark">{{ groupedRecords.other.length }}</el-tag>
            </div>
          </template>
          <div class="card-grid">
            <el-card
              v-for="record in groupedRecords.other"
              :key="record.id"
              class="record-card other"
              shadow="hover"
              :body-style="{ padding: '16px' }"
            >
              <div class="card-header">
                <el-avatar :size="44" class="card-avatar other-avatar">{{ record.studentName?.charAt(0) || '?' }}</el-avatar>
                <div class="card-header-info">
                  <div class="card-name">{{ record.studentName || '未知' }}</div>
                  <div class="card-no">{{ record.studentNo || '无学号' }}</div>
                </div>
                <el-tag size="small" type="info" effect="light" class="card-type-tag">{{ record.recordType || '其他' }}</el-tag>
              </div>
              <el-divider style="margin: 12px 0" />
              <div class="card-body">
                <div class="card-item">
                  <el-icon><MapLocation /></el-icon>
                  <span class="card-label">位置</span>
                  <span class="card-value" :title="record.address">{{ record.address || '未知位置' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span class="card-label">城市</span>
                  <span class="card-value">{{ record.city || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Clock /></el-icon>
                  <span class="card-label">时间</span>
                  <span class="card-value">{{ record.recordTime }}</span>
                </div>
                <div class="card-item" v-if="record.remark">
                  <el-icon><Document /></el-icon>
                  <span class="card-label">备注</span>
                  <span class="card-value">{{ record.remark }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </el-collapse-item>
      </el-collapse>

      <!-- 空状态 -->
      <el-empty v-if="records.length === 0" description="暂无签到记录" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { locationApi } from '@/api'
import {
  MapLocation, Refresh, Grid, List, Download,
  CircleCheck, Timer, OfficeBuilding, Clock, Document, MoreFilled
} from '@element-plus/icons-vue'

const loading = ref(false)
const exporting = ref(false)
const records = ref<any[]>([])
const pageError = ref('')
const viewMode = ref<'table' | 'card'>('table')
const currentPage = ref(1)
const pageSize = ref(10)
const activeGroups = ref(['checkin', 'checkout', 'other'])

const headerStyle = () => ({
  background: '#f8fafc',
  fontWeight: 600,
  color: '#64748b',
  fontSize: '12px',
  letterSpacing: '0.5px',
  textTransform: 'uppercase' as const
})

// 统计
const checkInCount = computed(() => records.value.filter(r => r.recordType === 'CHECK_IN').length)
const checkOutCount = computed(() => records.value.filter(r => r.recordType === 'CHECK_OUT').length)
const cityCount = computed(() => new Set(records.value.map(r => r.city).filter(Boolean)).size)

// 分页数据
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return records.value.slice(start, end)
})

// 按打卡类型分组
const groupedRecords = computed(() => {
  const checkin = records.value.filter(r => r.recordType === 'CHECK_IN')
  const checkout = records.value.filter(r => r.recordType === 'CHECK_OUT')
  const other = records.value.filter(r => r.recordType !== 'CHECK_IN' && r.recordType !== 'CHECK_OUT')
  return { checkin, checkout, other }
})

// 切换视图
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'table' ? 'card' : 'table'
}

const loadRecords = async () => {
  pageError.value = ''
  loading.value = true
  try {
    const res = await locationApi.getTeacherRecords({ page: 0, size: 1000 })
    if (res.code === 200) {
      const data = res.data
      let list: any[] = []
      if (Array.isArray(data)) { list = data }
      else if (data && Array.isArray(data.content)) { list = data.content }
      else { console.warn('[Teacher/Locations] 未识别的数据结构:', data); list = [] }
      records.value = list
    } else {
      ElMessage.error(res.message || '加载失败')
      records.value = []
    }
  } catch (error: any) {
    console.error('[Teacher/Locations] 加载失败:', error)
    pageError.value = error.message || '加载签到记录失败'
    records.value = []
  } finally { loading.value = false }
}

const retryLoad = () => { pageError.value = ''; loadRecords() }

// 导出记录
const exportRecords = () => {
  exporting.value = true
  try {
    const data = records.value
    if (data.length === 0) {
      ElMessage.warning('没有数据可导出')
      exporting.value = false
      return
    }

    const BOM = '\uFEFF'
    const headers = ['学生姓名', '学号', '打卡类型', '打卡位置', '城市', '省份', '打卡时间', '备注']
    const rows = data.map(r => [
      r.studentName || '',
      r.studentNo || '',
      r.recordType === 'CHECK_IN' ? '上班打卡' : r.recordType === 'CHECK_OUT' ? '下班打卡' : (r.recordType || '其他'),
      r.address || '',
      r.city || '',
      r.province || '',
      r.recordTime || '',
      r.remark || ''
    ])

    const csvContent = BOM + [headers, ...rows]
      .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
      .join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const timestamp = new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')
    link.href = URL.createObjectURL(blob)
    link.download = `签到记录_${timestamp}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)

    ElMessage.success(`成功导出 ${data.length} 条签到记录`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => loadRecords())
</script>

<style scoped lang="scss">
.page-wrapper {
  padding: 4px;
}

// 页面标题
.page-header-modern {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-header-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--gray-800);
  margin: 0;
}
.page-header-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin: 4px 0 0;
}
.page-header-actions {
  display: flex;
  gap: 8px;
}

// 统计栏
.stats-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.stat-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-xs);
}
.stat-pill-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-600);
}
.stat-pill-label {
  font-size: 13px;
  color: var(--gray-500);
}

// 错误回退
.error-fallback {
  padding: 40px;
}

// 表格
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}
.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.name-avatar {
  background: linear-gradient(135deg, var(--primary-400), var(--accent-500));
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.student-no {
  font-family: var(--mono);
  font-weight: 600;
  color: var(--gray-700);
  font-size: 13px;
}
.type-tag {
  border-radius: var(--radius-sm);
  font-weight: 600;
}
.location-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--gray-600);
  .el-icon {
    color: var(--primary-500);
    flex-shrink: 0;
  }
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--gray-600);
  .el-icon {
    color: var(--gray-400);
  }
}
.remark-text {
  font-size: 13px;
  color: var(--gray-500);
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
}

// 卡片视图
.card-view {
  .group-collapse {
    :deep(.el-collapse-item__header) {
      padding: 12px 16px;
      font-size: 15px;
      font-weight: 600;
      background: #f8fafc;
      border-radius: 8px;
      margin-bottom: 8px;
    }
    :deep(.el-collapse-item__content) {
      padding-bottom: 16px;
    }
  }
}
.group-title {
  display: flex;
  align-items: center;
  gap: 10px;
  .el-icon {
    font-size: 18px;
  }
  span {
    flex: 1;
  }
}

// 卡片网格
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

// 记录卡片
.record-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  border-left: 4px solid #e2e8f0;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  &.checkin {
    border-left-color: #16a34a;
  }
  &.checkout {
    border-left-color: #d97706;
  }
  &.other {
    border-left-color: #6366f1;
  }
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-avatar {
  background: linear-gradient(135deg, #16a34a, #22c55e);
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
  &.checkout-avatar {
    background: linear-gradient(135deg, #d97706, #f59e0b);
  }
  &.other-avatar {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
  }
}
.card-header-info {
  flex: 1;
  min-width: 0;
}
.card-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--gray-800);
}
.card-no {
  font-size: 12px;
  color: var(--gray-400);
  margin-top: 2px;
  font-family: var(--mono);
}
.card-type-tag {
  flex-shrink: 0;
  font-weight: 600;
}
.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.card-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  .el-icon {
    color: var(--gray-400);
    font-size: 14px;
    width: 16px;
  }
}
.card-label {
  color: var(--gray-400);
  min-width: 36px;
}
.card-value {
  color: var(--gray-700);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

// 动画
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}
.animate-slide-up {
  animation: slideUp 0.5s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

// 响应式
@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  .page-header-modern {
    flex-direction: column;
    align-items: flex-start;
  }
  .page-header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  .stats-bar {
    .dept-switch {
      margin-left: 0;
      width: 100%;
    }
  }
}
</style>
