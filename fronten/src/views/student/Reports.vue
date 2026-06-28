<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">汇报管理</h2>
        <p class="page-header-desc">管理您的实习汇报与批阅状态</p>
      </div>
      <el-button type="primary" size="large" @click="handleCreate" class="btn-gradient">
        <el-icon><Plus /></el-icon>
        提交汇报
      </el-button>
    </div>

    <div class="filter-chips animate-fade-in" style="animation-delay: 0.05s">
      <div v-for="chip in filterChips" :key="chip.value"
           :class="['filter-chip', { active: activeFilter === chip.value }]"
           @click="activeFilter = chip.value">
        <span class="chip-dot" :style="{ background: chip.color }"></span>
        {{ chip.label }}
        <span class="chip-count">{{ chip.count }}</span>
      </div>
    </div>

    <el-card class="table-card animate-slide-up" style="animation-delay: 0.1s" v-loading="loading">
      <el-table :data="filteredReports" style="width: 100%" :header-cell-style="headerStyle">
        <el-table-column prop="title" label="汇报标题" min-width="160">
          <template #default="{ row }">
            <div class="report-title-cell">
              <el-icon :size="16" :style="{ color: reportTypeColor(row.reportType) }"><Document /></el-icon>
              <div>
                <div class="report-name">{{ row.title }}</div>
                <div class="report-meta">{{ row.company }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reportType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.reportType === 'WEEKLY' ? 'primary' : row.reportType === 'MONTHLY' ? 'warning' : 'success'"
                    size="small" effect="light" class="type-tag">
              {{ reportTypeLabel(row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weekNumber" label="周次" width="80">
          <template #default="{ row }">
            <span class="week-badge" v-if="row.weekNumber">第{{ row.weekNumber }}周</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="reportDate" label="日期" width="110">
          <template #default="{ row }">
            <div class="date-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ row.reportDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status.toLowerCase()">
              <span class="status-pulse"></span>
              {{ statusLabel(row.status) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="80">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined" class="score-value"
                  :class="{ excellent: row.score >= 90, good: row.score >= 80 && row.score < 90, pass: row.score < 80 }">
              {{ row.score }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn view" @click="viewDetail(row)"><el-icon><View /></el-icon></button>
              <button class="action-btn edit" v-if="row.status === 'DRAFT'" @click="editReport(row)"><el-icon><Edit /></el-icon></button>
              <button class="action-btn submit" v-if="row.status === 'DRAFT'" @click="submitReport(row)"><el-icon><Upload /></el-icon></button>
              <button class="action-btn delete" v-if="row.status === 'DRAFT'" @click="deleteReport(row)"><el-icon><Delete /></el-icon></button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
                       :total="pagination.total" :page-sizes="[10, 20, 50]"
                       layout="total, sizes, prev, pager, next"
                       @size-change="loadReports" @current-change="loadReports" />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="汇报详情" width="700px" destroy-on-close>
      <div class="detail-content" v-if="currentReport">
        <div class="detail-header-bar">
          <div class="detail-title-section">
            <el-tag :type="currentReport.reportType === 'WEEKLY' ? 'primary' : currentReport.reportType === 'MONTHLY' ? 'warning' : 'success'"
                    size="small" effect="light">
              {{ reportTypeLabel(currentReport.reportType) }}
            </el-tag>
            <h3>{{ currentReport.title }}</h3>
          </div>
          <div class="status-badge" :class="currentReport.status.toLowerCase()">
            {{ statusLabel(currentReport.status) }}
          </div>
        </div>

        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="实习单位">{{ currentReport.company }}</el-descriptions-item>
          <el-descriptions-item label="汇报日期">{{ currentReport.reportDate }}</el-descriptions-item>
          <el-descriptions-item label="周次" v-if="currentReport.weekNumber">第{{ currentReport.weekNumber }}周</el-descriptions-item>
          <el-descriptions-item label="评分">
            <span v-if="currentReport.score" class="score-value" :class="{ excellent: currentReport.score >= 90, good: currentReport.score >= 80 }">
              {{ currentReport.score }} 分
            </span>
            <span v-else class="text-muted">未评分</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-sections">
          <div class="detail-section" v-if="currentReport.content">
            <h4><el-icon><Document /></el-icon> 汇报内容</h4>
            <p>{{ currentReport.content }}</p>
          </div>
          <div class="detail-section" v-if="currentReport.summary">
            <h4><el-icon><Notebook /></el-icon> 工作总结</h4>
            <p>{{ currentReport.summary }}</p>
          </div>
          <div class="detail-section" v-if="currentReport.problem">
            <h4><el-icon><Warning /></el-icon> 遇到的问题</h4>
            <p>{{ currentReport.problem }}</p>
          </div>
          <div class="detail-section" v-if="currentReport.solution">
            <h4><el-icon><CircleCheck /></el-icon> 解决方案</h4>
            <p>{{ currentReport.solution }}</p>
          </div>
          <div class="detail-section" v-if="currentReport.plan">
            <h4><el-icon><Flag /></el-icon> 下周计划</h4>
            <p>{{ currentReport.plan }}</p>
          </div>
          <div class="detail-section highlight" v-if="currentReport.reviewComment">
            <h4><el-icon><ChatDotRound /></el-icon> 批阅意见</h4>
            <p class="review-text">{{ currentReport.reviewComment }}</p>
            <div class="reviewer-info">
              <el-avatar :size="24" class="mini-avatar">{{ currentReport.reviewerName?.charAt(0) }}</el-avatar>
              <span>批阅人: {{ currentReport.reviewerName }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportApi } from '@/api'
import { Plus, Document, Calendar, View, Edit, Upload, Delete, Notebook, Warning, CircleCheck, Flag, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const reports = ref<any[]>([])
const detailVisible = ref(false)
const currentReport = ref<any>(null)
const activeFilter = ref('ALL')
const pagination = reactive({ page: 1, size: 10, total: 0 })

const filterChips = computed(() => {
  const counts: Record<string, number> = { ALL: reports.value.length }
  reports.value.forEach(r => { counts[r.status] = (counts[r.status] || 0) + 1 })
  return [
    { value: 'ALL', label: '全部', color: '#6366f1', count: counts.ALL || 0 },
    { value: 'DRAFT', label: '草稿', color: '#94a3b8', count: counts.DRAFT || 0 },
    { value: 'SUBMITTED', label: '已提交', color: '#f59e0b', count: counts.SUBMITTED || 0 },
    { value: 'REVIEWED', label: '已批阅', color: '#22c55e', count: counts.REVIEWED || 0 },
    { value: 'RETURNED', label: '已退回', color: '#f43f5e', count: counts.RETURNED || 0 },
  ]
})

const filteredReports = computed(() => {
  if (activeFilter.value === 'ALL') return reports.value
  return reports.value.filter(r => r.status === activeFilter.value)
})

const reportTypeLabel = (type: string) => ({ WEEKLY: '周报', MONTHLY: '月报', FINAL: '总结报告' }[type] || type)
const statusLabel = (status: string) => ({ DRAFT: '草稿', SUBMITTED: '已提交', REVIEWED: '已批阅', RETURNED: '已退回' }[status] || status)
const reportTypeColor = (type: string) => ({ WEEKLY: '#6366f1', MONTHLY: '#f59e0b', FINAL: '#22c55e' }[type] || '#6366f1')
const headerStyle = () => ({ background: '#f8fafc', fontWeight: 600, color: '#64748b', fontSize: '12px', letterSpacing: '0.5px', textTransform: 'uppercase' })

const loadReports = async () => {
  loading.value = true
  try {
    const res = await reportApi.getStudentReports({ page: pagination.page - 1, size: pagination.size })
    if (res.code === 200) {
      const data = res.data
      let list: any[] = []
      let total = 0
      if (Array.isArray(data)) { list = data; total = data.length }
      else if (data && Array.isArray(data.content)) { list = data.content; total = data.totalElements || list.length }
      else if (data && Array.isArray(data.list)) { list = data.list; total = data.total || list.length }
      else { console.warn('[Reports] 未识别的数据结构:', data); list = []; total = 0 }
      reports.value = list
      pagination.total = total
      console.log(`[Reports] 加载成功: ${list.length} 条记录`)
    } else { ElMessage.error(res.message || '加载失败'); reports.value = [] }
  } catch (error) {
    console.error('[Reports] 加载失败:', error)
    ElMessage.error('加载汇报数据失败')
    reports.value = []
  } finally { loading.value = false }
}

const handleCreate = () => router.push('/student/reports/create')
const viewDetail = (row: any) => { currentReport.value = row; detailVisible.value = true }
const editReport = (row: any) => router.push(`/student/reports/${row.id}/edit`)

const submitReport = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要提交此汇报吗？', '提示', { type: 'warning' })
    const res = await reportApi.submit(row.id)
    if (res.code === 200) { ElMessage.success('提交成功'); loadReports() }
  } catch (error) { console.error(error) }
}

const deleteReport = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除此汇报吗？', '警告', { type: 'warning' })
    const res = await reportApi.delete(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadReports() }
  } catch (error) { console.error(error) }
}

onMounted(() => loadReports())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }
.btn-gradient { background: linear-gradient(135deg, var(--primary-500), var(--primary-600)); border: none; box-shadow: 0 4px 14px rgba(99, 102, 241, 0.3); }

.filter-chips { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.filter-chip {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 16px; background: #ffffff; border: 1px solid var(--border-color);
  border-radius: var(--radius-full); font-size: 13px; font-weight: 500; color: var(--gray-600);
  cursor: pointer; transition: all var(--transition-fast);
  &:hover { border-color: var(--primary-300); color: var(--primary-600); }
  &.active { background: linear-gradient(135deg, var(--primary-500), var(--primary-600)); color: #ffffff; border-color: transparent; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    .chip-dot { background: #ffffff !important; }
    .chip-count { background: rgba(255,255,255,0.2); color: #ffffff; }
  }
}
.chip-dot { width: 8px; height: 8px; border-radius: 50%; }
.chip-count { padding: 2px 8px; background: var(--gray-100); border-radius: var(--radius-full); font-size: 11px; font-weight: 600; }

.report-title-cell { display: flex; align-items: center; gap: 10px;
  .el-icon { flex-shrink: 0; }
}
.report-name { font-weight: 600; color: var(--gray-800); font-size: 14px; }
.report-meta { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.type-tag { border-radius: var(--radius-sm); font-weight: 600; }
.week-badge { font-size: 12px; color: var(--gray-500); background: var(--gray-100); padding: 2px 8px; border-radius: var(--radius-sm); }

.date-cell { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--gray-600); .el-icon { color: var(--gray-400); } }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 5px 12px; border-radius: var(--radius-full); font-size: 12px; font-weight: 600;
  &.draft { background: #f1f5f9; color: #64748b; }
  &.submitted { background: #fef3c7; color: #d97706; .status-pulse { background: #f59e0b; } }
  &.reviewed { background: #dcfce7; color: #16a34a; .status-pulse { background: #22c55e; } }
  &.returned { background: #ffe4e6; color: #e11d48; .status-pulse { background: #f43f5e; } }
  .status-pulse { width: 6px; height: 6px; border-radius: 50%; animation: pulse-soft 2s ease-in-out infinite; }
}

.score-value { font-weight: 700; font-size: 15px;
  &.excellent { color: #16a34a; }
  &.good { color: #d97706; }
  &.pass { color: #64748b; }
}

.action-group { display: flex; gap: 6px; }
.action-btn {
  width: 32px; height: 32px; border-radius: var(--radius-sm); border: none;
  display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast); font-size: 14px;
  &.view { background: #eef2ff; color: #6366f1; &:hover { background: #6366f1; color: #fff; } }
  &.edit { background: #fef3c7; color: #d97706; &:hover { background: #f59e0b; color: #fff; } }
  &.submit { background: #dcfce7; color: #16a34a; &:hover { background: #22c55e; color: #fff; } }
  &.delete { background: #ffe4e6; color: #e11d48; &:hover { background: #f43f5e; color: #fff; } }
}

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.text-muted { color: var(--gray-400); font-size: 13px; }

// Detail
.detail-content { padding: 4px; }
.detail-header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.detail-title-section { display: flex; align-items: center; gap: 12px;
  h3 { margin: 0; font-size: 18px; font-weight: 700; color: var(--gray-800); }
}

:deep(.detail-descriptions) {
  .el-descriptions__label { font-weight: 600; color: var(--gray-600); background: var(--gray-50); }
}

.detail-sections { margin-top: 20px; display: flex; flex-direction: column; gap: 16px; }
.detail-section {
  padding: 16px 20px; background: var(--gray-50); border-radius: var(--radius-md);
  border-left: 3px solid var(--primary-400);
  h4 { display: flex; align-items: center; gap: 8px; margin: 0 0 10px; font-size: 14px; font-weight: 600; color: var(--gray-700);
    .el-icon { color: var(--primary-500); }
  }
  p { margin: 0; color: var(--gray-600); font-size: 13px; line-height: 1.6; white-space: pre-wrap; }

  &.highlight {
    background: linear-gradient(135deg, #eef2ff, #f5f3ff);
    border-left-color: var(--primary-500);
    .review-text { font-style: italic; color: var(--primary-700); }
  }
}

.reviewer-info { display: flex; align-items: center; gap: 8px; margin-top: 10px; font-size: 12px; color: var(--gray-500);
  .mini-avatar { background: linear-gradient(135deg, var(--primary-400), var(--accent-500)); color: #fff; font-size: 10px; }
}
</style>
