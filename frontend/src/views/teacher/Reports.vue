<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">汇报批阅</h2>
        <p class="page-header-desc">批阅学生提交的实习汇报</p>
      </div>
      <div class="header-filter">
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="loadReports" style="width: 160px">
          <el-option label="全部" value="" />
          <el-option label="待批阅" value="SUBMITTED" />
          <el-option label="已批阅" value="REVIEWED" />
          <el-option label="已退回" value="RETURNED" />
        </el-select>
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

    <el-card v-else class="table-card animate-slide-up" v-loading="loading">
      <el-table :data="reports" style="width: 100%" :header-cell-style="headerStyle" empty-text="暂无汇报">
        <el-table-column prop="studentName" label="学生" width="100">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar :size="28" class="name-avatar">{{ row.studentName?.charAt(0) }}</el-avatar>
              <span>{{ row.studentName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="120">
          <template #default="{ row }">
            <span class="student-no">{{ row.studentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140">
          <template #default="{ row }">
            <div class="report-name-cell">
              <div class="r-name">{{ row.title }}</div>
              <div class="r-company">{{ getCompanyName(row) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reportType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.reportType === 'WEEKLY' ? 'primary' : row.reportType === 'MONTHLY' ? 'warning' : 'success'"
                    size="small" effect="light" class="type-tag">
              {{ reportTypeLabel(row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weekNumber" label="周次" width="70">
          <template #default="{ row }">
            <span v-if="row.weekNumber">第{{ row.weekNumber }}周</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status?.toLowerCase()">
              <span class="status-pulse"></span>
              {{ statusLabel(row.status) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn view" @click="viewDetail(row)"><el-icon><View /></el-icon></button>
              <button class="action-btn success" v-if="row.status === 'SUBMITTED'" @click="handleReview(row)"><el-icon><EditPen /></el-icon></button>
              <button class="action-btn edit" v-else @click="handleReview(row)"><el-icon><RefreshLeft /></el-icon></button>
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

    <!-- Review dialog -->
    <el-dialog v-model="reviewVisible" title="汇报批阅" width="560px" class="review-dialog" destroy-on-close>
      <div class="report-detail" v-if="currentReport">
        <div class="report-detail-header">
          <el-avatar :size="36" class="detail-avatar">{{ currentReport.studentName?.charAt(0) }}</el-avatar>
          <div>
            <div class="detail-name">{{ currentReport.studentName }} <span class="detail-no">{{ currentReport.studentNo }}</span></div>
            <div class="detail-meta">{{ getCompanyName(currentReport) }} · {{ reportTypeLabel(currentReport.reportType) }}
              <span v-if="currentReport.weekNumber"> · 第{{ currentReport.weekNumber }}周</span>
            </div>
          </div>
        </div>
        <div class="report-content-box">
          <h4>{{ currentReport.title }}</h4>
          <p>{{ currentReport.content }}</p>
        </div>
      </div>
      <el-form :model="reviewForm" label-position="top" class="review-form">
        <el-form-item label="批阅结果">
          <el-radio-group v-model="reviewForm.approved" size="large">
            <el-radio-button :value="true">
              <el-icon><CircleCheck /></el-icon> 通过
            </el-radio-button>
            <el-radio-button :value="false">
              <el-icon><Close /></el-icon> 退回修改
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评分" v-if="reviewForm.approved">
          <el-slider v-model="reviewForm.score" :min="0" :max="100" show-stops :step="5" show-score />
        </el-form-item>
        <el-form-item label="批阅意见">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请输入批阅意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportApi } from '@/api'
import { View, EditPen, CircleCheck, RefreshLeft, Close } from '@element-plus/icons-vue'

const loading = ref(false)
const reports = ref<any[]>([])
const filterStatus = ref('')
const reviewVisible = ref(false)
const reviewLoading = ref(false)
const currentReport = ref<any>(null)
const pageError = ref('')
const pagination = reactive({ page: 1, size: 10, total: 0 })
const reviewForm = reactive({ approved: true, comment: '', score: 80 })

const reportTypeLabel = (type: string) => ({ WEEKLY: '周报', MONTHLY: '月报', FINAL: '总结' }[type] || type)
const statusLabel = (status: string) => ({ DRAFT: '草稿', SUBMITTED: '待批阅', REVIEWED: '已批阅', RETURNED: '已退回' }[status] || status)
const headerStyle = () => ({ background: '#f8fafc', fontWeight: 600, color: '#64748b', fontSize: '12px', letterSpacing: '0.5px', textTransform: 'uppercase' })

const getCompanyName = (row: any): string => {
  if (!row) return '未知企业'
  const c = row.company
  if (typeof c === 'string') return c
  if (c && typeof c === 'object') return c.name || c.companyName || '未知企业'
  return row.companyName || '未知企业'
}

const loadReports = async () => {
  pageError.value = ''
  loading.value = true
  try {
    const res = await reportApi.getTeacherReports({ page: pagination.page - 1, size: pagination.size })
    if (res.code === 200) {
      const data = res.data
      let list: any[] = []
      let total = 0
      if (Array.isArray(data)) { list = data; total = data.length }
      else if (data && Array.isArray(data.content)) { list = data.content; total = data.totalElements || list.length }
      else { console.warn('[Teacher/Reports] 未识别的数据结构:', data); list = []; total = 0 }
      if (filterStatus.value) list = list.filter((item: any) => item.status === filterStatus.value)
      reports.value = list
      pagination.total = total
    } else { ElMessage.error(res.message || '加载失败'); reports.value = [] }
  } catch (error: any) {
    console.error('[Teacher/Reports] 加载失败:', error)
    pageError.value = error.message || '加载汇报数据失败'
    reports.value = []
  } finally { loading.value = false }
}

const retryLoad = () => { pageError.value = ''; loadReports() }

const viewDetail = (row: any) => {
  currentReport.value = row
  reviewForm.approved = true
  reviewForm.comment = ''
  reviewForm.score = 80
  reviewVisible.value = true
}
const handleReview = (row: any) => { viewDetail(row) }

const submitReview = async () => {
  if (!reviewForm.comment) { ElMessage.warning('请输入批阅意见'); return }
  reviewLoading.value = true
  try {
    const res = await reportApi.review(currentReport.value.id, reviewForm)
    if (res.code === 200) { ElMessage.success('批阅成功'); reviewVisible.value = false; loadReports() }
  } catch (error) { console.error(error) }
  finally { reviewLoading.value = false }
}

onMounted(() => loadReports())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.error-fallback { padding: 40px; }

.name-cell { display: flex; align-items: center; gap: 10px; }
.name-avatar { background: linear-gradient(135deg, var(--primary-400), var(--accent-500)); color: #fff; font-size: 12px; font-weight: 600; }
.student-no { font-family: var(--mono); font-weight: 600; color: var(--gray-700); font-size: 13px; }

.report-name-cell { min-width: 0; }
.r-name { font-weight: 600; color: var(--gray-800); font-size: 14px; }
.r-company { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.type-tag { border-radius: var(--radius-sm); font-weight: 600; }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px; padding: 5px 12px; border-radius: var(--radius-full); font-size: 12px; font-weight: 600;
  &.draft { background: #f1f5f9; color: #64748b; }
  &.submitted { background: #fef3c7; color: #d97706; .status-pulse { background: #f59e0b; } }
  &.reviewed { background: #dcfce7; color: #16a34a; .status-pulse { background: #22c55e; } }
  &.returned { background: #ffe4e6; color: #e11d48; .status-pulse { background: #f43f5e; } }
  .status-pulse { width: 6px; height: 6px; border-radius: 50%; animation: pulse-soft 2s ease-in-out infinite; }
}

.action-group { display: flex; gap: 6px; }
.action-btn {
  width: 32px; height: 32px; border-radius: var(--radius-sm); border: none; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast); font-size: 14px;
  &.view { background: #eef2ff; color: #6366f1; &:hover { background: #6366f1; color: #fff; } }
  &.success { background: #dcfce7; color: #16a34a; &:hover { background: #22c55e; color: #fff; } }
  &.edit { background: #fef3c7; color: #d97706; &:hover { background: #f59e0b; color: #fff; } }
}
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

.report-detail { margin-bottom: 20px; }
.report-detail-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--border-light); }
.detail-avatar { background: linear-gradient(135deg, var(--primary-500), var(--accent-500)); color: #fff; font-size: 14px; font-weight: 600; }
.detail-name { font-weight: 600; color: var(--gray-800); font-size: 15px; }
.detail-no { font-size: 12px; color: var(--text-muted); font-weight: 400; }
.detail-meta { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.report-content-box { background: #f8fafc; border-radius: var(--radius-md); padding: 16px; }
.report-content-box h4 { margin: 0 0 8px; font-size: 15px; color: var(--gray-800); }
.report-content-box p { margin: 0; font-size: 13px; color: var(--gray-600); line-height: 1.7; white-space: pre-wrap; }

.review-form { margin-top: 16px; }
.review-form :deep(.el-radio-button__inner) { display: flex; align-items: center; gap: 6px; padding: 10px 20px; }

@keyframes pulse-soft {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
