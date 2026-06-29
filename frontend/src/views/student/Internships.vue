<template>
  <div class="page-wrapper">
    <!-- Page header -->
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">实习信息</h2>
        <p class="page-header-desc">管理您的实习记录与状态</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/student/internships/create')" class="btn-gradient">
        <el-icon><Plus /></el-icon>
        新增实习信息
      </el-button>
    </div>

    <!-- Status filter chips -->
    <div class="filter-chips animate-fade-in" style="animation-delay: 0.05s">
      <div
        v-for="chip in filterChips"
        :key="chip.value"
        :class="['filter-chip', { active: activeFilter === chip.value }]"
        @click="activeFilter = chip.value"
      >
        <span class="chip-dot" :style="{ background: chip.color }"></span>
        {{ chip.label }}
        <span class="chip-count">{{ chip.count }}</span>
      </div>
    </div>

    <!-- Table card -->
    <el-card class="table-card animate-slide-up" style="animation-delay: 0.1s" v-loading="loading">
      <el-table :data="filteredInternships" style="width: 100%" :header-cell-style="headerStyle">
        <el-table-column prop="company" label="实习单位" min-width="160">
          <template #default="{ row }">
            <div class="company-cell">
              <div class="company-icon" :style="{ background: stringToColor(getCompanyName(row)) }">
                {{ getCompanyName(row).charAt(0) }}
              </div>
              <div class="company-info">
                <div class="company-name">{{ getCompanyName(row) }}</div>
                <div class="company-position">{{ row.position }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="指导老师" width="110">
          <template #default="{ row }">
            <div class="teacher-cell">
              <el-avatar :size="24" class="mini-avatar">{{ row.teacherName?.charAt(0) }}</el-avatar>
              <span>{{ row.teacherName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="实习时间" width="190">
          <template #default="{ row }">
            <div class="date-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ row.startDate }} ~ {{ row.endDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status.toLowerCase()">
              <span class="status-pulse"></span>
              {{ statusLabel(row.status) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn view" @click="viewDetail(row)">
                <el-icon><View /></el-icon>
              </button>
              <button class="action-btn edit" v-if="row.status === 'DRAFT'" @click="editInternship(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn submit" v-if="row.status === 'DRAFT'" @click="submitInternship(row)">
                <el-icon><Upload /></el-icon>
              </button>
              <button class="action-btn complete" v-if="row.status === 'ONGOING'" @click="completeInternship(row)">
                <el-icon><CircleCheck /></el-icon>
              </button>
              <button class="action-btn delete" v-if="row.status === 'DRAFT'" @click="deleteInternship(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadInternships"
          @current-change="loadInternships"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="实习详情" width="640px" class="detail-dialog" destroy-on-close>
      <div class="detail-content" v-if="currentInternship">
        <div class="detail-header">
          <div class="detail-company-icon" :style="{ background: stringToColor(currentInternship.company) }">
            {{ currentInternship.company.charAt(0) }}
          </div>
          <div class="detail-header-info">
            <h3>{{ currentInternship.company }}</h3>
            <p>{{ currentInternship.position }}</p>
          </div>
          <div class="status-badge" :class="currentInternship.status.toLowerCase()">
            {{ statusLabel(currentInternship.status) }}
          </div>
        </div>

        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="实习地址">{{ currentInternship.address || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="企业联系人">{{ currentInternship.companyContact || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentInternship.companyPhone || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="指导老师">{{ currentInternship.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ currentInternship.startDate }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ currentInternship.endDate }}</el-descriptions-item>
          <el-descriptions-item label="导师评语" :span="2">
            <div class="content-text">{{ currentInternship.teacherComment || '暂无评语' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="实习描述" :span="2">
            <div class="content-text">{{ currentInternship.description || '暂无描述' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="工作内容" :span="2">
            <div class="content-text">{{ currentInternship.workContent || '暂无内容' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" v-if="currentInternship?.status === 'ONGOING'"
                   @click="$router.push(`/student/reports/create?internshipId=${currentInternship?.id}`)">
          <el-icon><Edit /></el-icon>
          提交汇报
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { internshipApi } from '@/api'
import { Plus, Calendar, View, Edit, Upload, CircleCheck, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const internships = ref<any[]>([])
const detailVisible = ref(false)
const currentInternship = ref<any>(null)
const activeFilter = ref('ALL')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const filterChips = computed(() => {
  const counts: Record<string, number> = { ALL: internships.value.length }
  internships.value.forEach(i => {
    counts[i.status] = (counts[i.status] || 0) + 1
  })
  return [
    { value: 'ALL', label: '全部', color: '#6366f1', count: counts.ALL || 0 },
    { value: 'DRAFT', label: '草稿', color: '#94a3b8', count: counts.DRAFT || 0 },
    { value: 'PENDING', label: '待审核', color: '#f59e0b', count: counts.PENDING || 0 },
    { value: 'ONGOING', label: '进行中', color: '#6366f1', count: counts.ONGOING || 0 },
    { value: 'COMPLETED', label: '已完成', color: '#22c55e', count: counts.COMPLETED || 0 },
    { value: 'REJECTED', label: '已驳回', color: '#f43f5e', count: counts.REJECTED || 0 },
  ]
})

const filteredInternships = computed(() => {
  if (activeFilter.value === 'ALL') return internships.value
  return internships.value.filter(i => i.status === activeFilter.value)
})

const statusLabel = (status: string) => {
  const labels: Record<string, string> = {
    DRAFT: '草稿', PENDING: '待审核', ONGOING: '进行中', COMPLETED: '已完成', REJECTED: '已驳回',
  }
  return labels[status] || status
}

const getCompanyName = (row: any): string => {
  if (!row) return '未知企业'
  const c = row.company
  if (typeof c === 'string') return c
  if (c && typeof c === 'object') return c.name || c.companyName || '未知企业'
  return row.companyName || '未知企业'
}

const stringToColor = (str: string) => {
  const colors = ['#6366f1', '#22c55e', '#f59e0b', '#f43f5e', '#0ea5e9', '#8b5cf6', '#ec4899']
  let hash = 0
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

const headerStyle = () => ({
  background: '#f8fafc',
  fontWeight: 600,
  color: '#64748b',
  fontSize: '12px',
  textTransform: 'uppercase',
  letterSpacing: '0.5px',
})

const loadInternships = async () => {
  loading.value = true
  try {
    const res = await internshipApi.getStudentInternships({
      page: pagination.page - 1, size: pagination.size,
    })
    if (res.code === 200) {
      const data = res.data
      let list: any[] = []
      let total = 0

      if (Array.isArray(data)) {
        list = data
        total = data.length
      } else if (data && Array.isArray(data.content)) {
        list = data.content
        total = data.totalElements || list.length
      } else if (data && Array.isArray(data.list)) {
        list = data.list
        total = data.total || list.length
      } else {
        console.warn('[Internships] 未识别的数据结构:', data)
        list = []
        total = 0
      }

      internships.value = list
      pagination.total = total
      console.log(`[Internships] 加载成功: ${list.length} 条记录`)
    } else {
      ElMessage.error(res.message || '加载数据失败')
      internships.value = []
    }
  } catch (error) {
    console.error('[Internships] 加载失败:', error)
    ElMessage.error('加载实习数据失败，请检查网络连接')
    internships.value = []
  } finally {
    loading.value = false
  }
}

const viewDetail = (row: any) => { currentInternship.value = row; detailVisible.value = true }
const editInternship = (row: any) => { router.push(`/student/internships/${row.id}/edit`) }

const submitInternship = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要提交此实习信息吗？提交后将等待导师审核。', '提示', { type: 'warning' })
    const res = await internshipApi.submit(row.id)
    if (res.code === 200) { ElMessage.success('提交成功'); loadInternships() }
  } catch (error) { console.error(error) }
}

const completeInternship = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要标记此实习为已完成吗？', '提示', { type: 'warning' })
    const res = await internshipApi.complete(row.id)
    if (res.code === 200) { ElMessage.success('操作成功'); loadInternships() }
  } catch (error) { console.error(error) }
}

const deleteInternship = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除此实习信息吗？', '警告', { type: 'warning' })
    const res = await internshipApi.delete(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadInternships() }
  } catch (error) { console.error(error) }
}

onMounted(() => { loadInternships() })
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }

.page-header-modern {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.btn-gradient {
  background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
  border: none;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.3);
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  color: var(--gray-600);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover { border-color: var(--primary-300); color: var(--primary-600); }

  &.active {
    background: linear-gradient(135deg, var(--primary-500), var(--primary-600));
    color: #ffffff;
    border-color: transparent;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    .chip-dot { background: #ffffff !important; }
    .chip-count { background: rgba(255,255,255,0.2); color: #ffffff; }
  }
}

.chip-dot { width: 8px; height: 8px; border-radius: 50%; }
.chip-count {
  padding: 2px 8px;
  background: var(--gray-100);
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
}

.company-cell { display: flex; align-items: center; gap: 12px; }
.company-icon {
  width: 38px; height: 38px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 16px; font-weight: 700; flex-shrink: 0;
}
.company-info { min-width: 0; }
.company-name { font-weight: 600; color: var(--gray-800); font-size: 14px; }
.company-position { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.teacher-cell { display: flex; align-items: center; gap: 8px; }
.mini-avatar { background: linear-gradient(135deg, var(--primary-400), var(--accent-500)); color: #fff; font-size: 10px; }

.date-cell { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--gray-600);
  .el-icon { color: var(--gray-400); }
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;

  &.draft { background: #f1f5f9; color: #64748b; }
  &.pending { background: #fef3c7; color: #d97706; .status-pulse { background: #f59e0b; } }
  &.ongoing { background: #eef2ff; color: #4338ca; .status-pulse { background: #6366f1; } }
  &.completed { background: #dcfce7; color: #16a34a; .status-pulse { background: #22c55e; } }
  &.rejected { background: #ffe4e6; color: #e11d48; .status-pulse { background: #f43f5e; } }

  .status-pulse {
    width: 6px; height: 6px; border-radius: 50%;
    animation: pulse-soft 2s ease-in-out infinite;
  }
}

.action-group { display: flex; gap: 6px; }
.action-btn {
  width: 32px; height: 32px;
  border-radius: var(--radius-sm);
  border: none;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: 14px;

  &.view { background: #eef2ff; color: #6366f1; &:hover { background: #6366f1; color: #fff; } }
  &.edit { background: #fef3c7; color: #d97706; &:hover { background: #f59e0b; color: #fff; } }
  &.submit { background: #dcfce7; color: #16a34a; &:hover { background: #22c55e; color: #fff; } }
  &.complete { background: #dbeafe; color: #2563eb; &:hover { background: #3b82f6; color: #fff; } }
  &.delete { background: #ffe4e6; color: #e11d48; &:hover { background: #f43f5e; color: #fff; } }
}

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

// Detail dialog
.detail-content { padding: 4px; }
.detail-header {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 24px; padding-bottom: 20px;
  border-bottom: 1px solid var(--border-light);
}
.detail-company-icon {
  width: 56px; height: 56px; border-radius: var(--radius-lg);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px; font-weight: 700; flex-shrink: 0;
}
.detail-header-info { flex: 1;
  h3 { margin: 0 0 4px; font-size: 18px; font-weight: 700; color: var(--gray-800); }
  p { margin: 0; font-size: 14px; color: var(--text-muted); }
}
.content-text { white-space: pre-wrap; line-height: 1.6; color: var(--gray-600); font-size: 13px; }

:deep(.detail-descriptions) {
  .el-descriptions__label { font-weight: 600; color: var(--gray-600); background: var(--gray-50); }
}
</style>
