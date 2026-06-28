<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">实习审核</h2>
        <p class="page-header-desc">审核学生提交的实习申请</p>
      </div>
      <div class="header-filter">
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="loadInternships" style="width: 160px">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已驳回" value="REJECTED" />
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
      <el-table :data="internships" style="width: 100%" :header-cell-style="headerStyle" empty-text="暂无实习申请">
        <el-table-column prop="studentName" label="学生" width="110">
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
        <el-table-column prop="company" label="实习单位" min-width="120">
          <template #default="{ row }">
            <div class="company-cell">
              <div class="company-icon-mini" :style="{ background: stringToColor(getCompanyName(row)) }">
                {{ getCompanyName(row).charAt(0) }}
              </div>
              <span>{{ getCompanyName(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="岗位" width="140" />
        <el-table-column label="实习时间" width="190">
          <template #default="{ row }">
            <div class="date-cell"><el-icon><Calendar /></el-icon><span>{{ row.startDate }} ~ {{ row.endDate }}</span></div>
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn view" @click="viewDetail(row)"><el-icon><View /></el-icon></button>
              <button class="action-btn success" v-if="row.status === 'PENDING'" @click="handleReview(row)"><el-icon><CircleCheck /></el-icon></button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
                       :total="pagination.total" :page-sizes="[10, 20, 50]"
                       layout="total, sizes, prev, pager, next"
                       @size-change="loadInternships" @current-change="loadInternships" />
      </div>
    </el-card>

    <!-- Review dialog -->
    <el-dialog v-model="reviewVisible" title="实习审核" width="500px" class="review-dialog" destroy-on-close>
      <div class="review-student-info" v-if="currentInternship">
        <el-avatar :size="40" class="review-avatar">{{ currentInternship.studentName?.charAt(0) }}</el-avatar>
        <div>
          <div class="review-student-name">{{ currentInternship.studentName }}</div>
          <div class="review-company">{{ getCompanyName(currentInternship) }} - {{ currentInternship.position }}</div>
        </div>
      </div>
      <el-form :model="reviewForm" label-position="top" class="review-form">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.approved" size="large">
            <el-radio-button :value="true">
              <el-icon><CircleCheck /></el-icon> 通过
            </el-radio-button>
            <el-radio-button :value="false">
              <el-icon><Close /></el-icon> 驳回
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请输入审核意见" />
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
import { internshipApi } from '@/api'
import { Calendar, View, CircleCheck, Close } from '@element-plus/icons-vue'

const loading = ref(false)
const internships = ref<any[]>([])
const filterStatus = ref('')
const reviewVisible = ref(false)
const reviewLoading = ref(false)
const currentInternship = ref<any>(null)
const pageError = ref('')
const pagination = reactive({ page: 1, size: 10, total: 0 })
const reviewForm = reactive({ approved: true, comment: '' })

const statusLabel = (status: string) => ({ DRAFT: '草稿', PENDING: '待审核', ONGOING: '进行中', COMPLETED: '已完成', REJECTED: '已驳回' }[status] || status)

/** v2.0 修复：安全获取企业名称，兼容 String(v1.0) 和 Object(v2.0) */
const getCompanyName = (row: any): string => {
  if (!row) return '未知企业'
  const c = row.company
  if (typeof c === 'string') return c
  if (c && typeof c === 'object') return c.name || c.companyName || '未知企业'
  return row.companyName || '未知企业'
}

const stringToColor = (str: string) => {
  const s = str || '?'
  const colors = ['#6366f1', '#22c55e', '#f59e0b', '#f43f5e', '#0ea5e9', '#8b5cf6', '#ec4899']
  let hash = 0
  for (let i = 0; i < s.length; i++) hash = s.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}
const headerStyle = () => ({ background: '#f8fafc', fontWeight: 600, color: '#64748b', fontSize: '12px', letterSpacing: '0.5px', textTransform: 'uppercase' })

/** v2.0 修复：安全解析分页数据 */
const loadInternships = async () => {
  pageError.value = ''
  loading.value = true
  try {
    const res = await internshipApi.getTeacherInternships({ page: pagination.page - 1, size: pagination.size })
    if (res.code === 200) {
      const data = res.data
      let list: any[] = []
      let total = 0
      if (Array.isArray(data)) { list = data; total = data.length }
      else if (data && Array.isArray(data.content)) { list = data.content; total = data.totalElements || list.length }
      else { console.warn('[Teacher/Internships] 未识别的数据结构:', data); list = []; total = 0 }
      if (filterStatus.value) list = list.filter((item: any) => item.status === filterStatus.value)
      internships.value = list
      pagination.total = total
    } else {
      ElMessage.error(res.message || '加载失败')
      internships.value = []
    }
  } catch (error: any) {
    console.error('[Teacher/Internships] 加载失败:', error)
    pageError.value = error.message || '加载实习数据失败，请检查网络连接'
    internships.value = []
  } finally { loading.value = false }
}

const retryLoad = () => { pageError.value = ''; loadInternships() }

const viewDetail = (row: any) => {
  const company = getCompanyName(row)
  ElMessageBox.alert(
    `学生：${row.studentName} (${row.studentNo})\n实习单位：${company}\n岗位：${row.position || '未填写'}\n地址：${row.address || '未填写'}\n时间：${row.startDate} ~ ${row.endDate}\n描述：${row.description || '暂无'}\n工作内容：${row.workContent || '暂无'}`,
    '实习详情', { confirmButtonText: '关闭' }
  )
}

const handleReview = (row: any) => {
  currentInternship.value = row
  reviewForm.approved = true
  reviewForm.comment = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.comment) { ElMessage.warning('请输入审核意见'); return }
  reviewLoading.value = true
  try {
    const res = await internshipApi.review(currentInternship.value.id, reviewForm)
    if (res.code === 200) { ElMessage.success('审核成功'); reviewVisible.value = false; loadInternships() }
  } catch (error) { console.error(error) }
  finally { reviewLoading.value = false }
}

onMounted(() => loadInternships())
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
.company-cell { display: flex; align-items: center; gap: 10px; }
.company-icon-mini { width: 28px; height: 28px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; font-weight: 700; }
.date-cell { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--gray-600); .el-icon { color: var(--gray-400); } }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px; padding: 5px 12px; border-radius: var(--radius-full); font-size: 12px; font-weight: 600;
  &.draft { background: #f1f5f9; color: #64748b; }
  &.pending { background: #fef3c7; color: #d97706; .status-pulse { background: #f59e0b; } }
  &.ongoing { background: #eef2ff; color: #4338ca; .status-pulse { background: #6366f1; } }
  &.completed { background: #dcfce7; color: #16a34a; .status-pulse { background: #22c55e; } }
  &.rejected { background: #ffe4e6; color: #e11d48; .status-pulse { background: #f43f5e; } }
  .status-pulse { width: 6px; height: 6px; border-radius: 50%; animation: pulse-soft 2s ease-in-out infinite; }
}

.action-group { display: flex; gap: 6px; }
.action-btn {
  width: 32px; height: 32px; border-radius: var(--radius-sm); border: none; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast); font-size: 14px;
  &.view { background: #eef2ff; color: #6366f1; &:hover { background: #6366f1; color: #fff; } }
  &.success { background: #dcfce7; color: #16a34a; &:hover { background: #22c55e; color: #fff; } }
}
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

.review-student-info { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid var(--border-light); }
.review-avatar { background: linear-gradient(135deg, var(--primary-500), var(--accent-500)); color: #fff; font-size: 16px; font-weight: 600; }
.review-student-name { font-weight: 600; color: var(--gray-800); font-size: 15px; }
.review-company { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.review-form :deep(.el-radio-button__inner) { display: flex; align-items: center; gap: 6px; padding: 10px 20px; }

@keyframes pulse-soft {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
