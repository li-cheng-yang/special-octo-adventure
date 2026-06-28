<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">实习管理</h2>
        <p class="page-header-desc">查看系统中所有实习记录</p>
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
      <el-table :data="internships" style="width: 100%" :header-cell-style="headerStyle" empty-text="暂无实习记录">
        <el-table-column prop="studentName" label="学生" width="100">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar :size="28" class="name-avatar">{{ row.studentName?.charAt(0) }}</el-avatar>
              <span>{{ row.studentName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="110">
          <template #default="{ row }"><span class="student-no">{{ row.studentNo }}</span></template>
        </el-table-column>
        <el-table-column prop="company" label="实习单位" min-width="140">
          <template #default="{ row }">
            <div class="company-cell">
              <div class="company-icon-mini" :style="{ background: stringToColor(getCompanyName(row)) }">
                {{ getCompanyName(row).charAt(0) }}
              </div>
              <span>{{ getCompanyName(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="岗位" width="100" />
        <el-table-column prop="teacherName" label="导师" width="100">
          <template #default="{ row }">
            <div class="teacher-tag"><el-icon><School /></el-icon>{{ row.teacherName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            <div class="date-cell"><el-icon><Calendar /></el-icon><span>{{ row.startDate }}~{{ row.endDate }}</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <div class="status-badge" :class="getStatusClass(row.status)">
              <span class="status-pulse"></span>{{ statusLabel(row.status) }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { internshipApi } from '@/api'
import { School, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)
const internships = ref<any[]>([])
const pageError = ref('')
const pagination = reactive({ page: 1, size: 10, total: 0 })

const statusLabel = (status: string) => ({ DRAFT: '草稿', PENDING: '待审核', ONGOING: '进行中', COMPLETED: '已完成', REJECTED: '已驳回' }[status] || status)

const getCompanyName = (row: any): string => {
  if (!row) return '未知企业'
  const c = row.company
  if (typeof c === 'string') return c
  if (c && typeof c === 'object') return c.name || c.companyName || '未知企业'
  return row.companyName || '未知企业'
}

const getStatusClass = (status: any): string => {
  if (!status) return ''
  return String(status).toLowerCase()
}

const stringToColor = (str: string) => {
  const s = str || '?'
  const colors = ['#6366f1', '#22c55e', '#f59e0b', '#f43f5e', '#0ea5e9', '#8b5cf6', '#ec4899']
  let hash = 0
  for (let i = 0; i < s.length; i++) hash = s.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}
const headerStyle = () => ({ background: '#f8fafc', fontWeight: 600, color: '#64748b', fontSize: '12px', letterSpacing: '0.5px', textTransform: 'uppercase' })

const loadInternships = async () => {
  pageError.value = ''
  loading.value = true
  try {
    const res = await internshipApi.getAll()
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) {
        internships.value = data
        pagination.total = data.length
      } else {
        internships.value = []
        pagination.total = 0
      }
    } else {
      ElMessage.error(res.message || '加载数据失败')
      internships.value = []
    }
  } catch (error: any) {
    console.error('[Admin/Internships] 加载失败:', error)
    pageError.value = error.message || '加载实习数据失败，请检查网络连接'
    internships.value = []
  } finally {
    loading.value = false
  }
}

const retryLoad = () => { pageError.value = ''; loadInternships() }

onMounted(() => loadInternships())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.error-fallback { padding: 40px; }

.name-cell { display: flex; align-items: center; gap: 10px; }
.name-avatar { background: linear-gradient(135deg, var(--primary-400), var(--accent-500)); color: #fff; font-size: 12px; font-weight: 600; }
.student-no { font-family: var(--mono); font-weight: 600; color: var(--gray-700); font-size: 13px; }
.company-cell { display: flex; align-items: center; gap: 10px; }
.company-icon-mini { width: 28px; height: 28px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; font-weight: 700; }
.teacher-tag { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--gray-700); }
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

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

@keyframes pulse-soft {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
