<template>
  <div class="page-wrapper">
    <!-- 页面标题 -->
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">学生管理</h2>
        <p class="page-header-desc">
          查看您指导的学生信息
          <el-tag v-if="userStore.user?.department" size="small" type="info" class="dept-tag">
            {{ userStore.user.department }}
          </el-tag>
        </p>
      </div>
      <div class="page-header-actions">
        <el-tooltip content="刷新数据" placement="bottom">
          <el-button circle :icon="Refresh" @click="loadStudents" :loading="loading" />
        </el-tooltip>
        <el-tooltip content="切换视图" placement="bottom">
          <el-button circle :icon="viewMode === 'table' ? Grid : List" @click="toggleViewMode" />
        </el-tooltip>
        <el-button type="primary" :icon="Download" @click="exportStudents" :loading="exporting">
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar animate-fade-in" style="animation-delay: 0.05s">
      <div class="stat-pill">
        <el-icon :size="18" color="#6366f1"><UserFilled /></el-icon>
        <span class="stat-pill-value">{{ students.length }}</span>
        <span class="stat-pill-label">名学生</span>
      </div>
      <div class="stat-pill" v-if="departmentFilterEnabled">
        <el-icon :size="18" color="#10b981"><Filter /></el-icon>
        <span class="stat-pill-value">{{ filteredStudents.length }}</span>
        <span class="stat-pill-label">院系匹配</span>
      </div>
      <div class="stat-pill">
        <el-icon :size="18" color="#f59e0b"><Check /></el-icon>
        <span class="stat-pill-value">{{ activeCount }}</span>
        <span class="stat-pill-label">正常</span>
      </div>
      <el-switch
        v-model="departmentFilterEnabled"
        active-text="只看本院系"
        inactive-text="全部学生"
        inline-prompt
        class="dept-switch"
        @change="onDepartmentFilterChange"
      />
    </div>

    <!-- 查询表单 -->
    <el-card class="search-card animate-slide-up" style="animation-delay: 0.08s" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="学号">
          <el-input
            v-model="searchForm.studentNo"
            placeholder="请输入学号"
            clearable
            :prefix-icon="Document"
            @keyup.enter="handleSearch"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入姓名"
            clearable
            :prefix-icon="User"
            @keyup.enter="handleSearch"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="专业">
          <el-input
            v-model="searchForm.major"
            placeholder="请输入专业"
            clearable
            :prefix-icon="Collection"
            @keyup.enter="handleSearch"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="班级">
          <el-input
            v-model="searchForm.className"
            placeholder="请输入班级"
            clearable
            :prefix-icon="School"
            @keyup.enter="handleSearch"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Delete" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格视图 -->
    <el-card v-if="viewMode === 'table'" class="table-card animate-slide-up" style="animation-delay: 0.1s" v-loading="loading" shadow="never">
      <el-table
        :data="paginatedStudents"
        style="width: 100%"
        :header-cell-style="headerStyle"
        row-key="id"
        highlight-current-row
      >
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="studentNo" label="学号" width="130">
          <template #default="{ row }">
            <span class="student-no">{{ row.studentNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="110">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar :size="32" class="name-avatar">{{ row.realName?.charAt(0) }}</el-avatar>
              <div class="name-info">
                <span class="name-text">{{ row.realName }}</span>
                <span v-if="row.username" class="name-sub">@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="院系" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.department" size="small" effect="plain" type="info">{{ row.department }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="major" label="专业" width="140" />
        <el-table-column prop="className" label="班级" width="120">
          <template #default="{ row }">
            <span class="class-tag">{{ row.className || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160">
          <template #default="{ row }">
            <div class="email-cell">
              <el-icon><Message /></el-icon>
              <span>{{ row.email || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120">
          <template #default="{ row }">
            <div class="phone-cell">
              <el-icon><Phone /></el-icon>
              <span>{{ row.phone || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status === 'ACTIVE' ? 'active' : 'inactive'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredStudents.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <!-- 卡片视图 -->
    <div v-else class="card-view animate-slide-up" style="animation-delay: 0.1s" v-loading="loading">
      <!-- 按状态分组折叠面板 -->
      <el-collapse v-model="activeGroups" class="group-collapse">
        <!-- 正常状态组 -->
        <el-collapse-item name="active" v-if="groupedStudents.active.length > 0">
          <template #title>
            <div class="group-title">
              <el-icon color="#16a34a"><CircleCheck /></el-icon>
              <span>正常状态</span>
              <el-tag size="small" type="success" effect="dark">{{ groupedStudents.active.length }}</el-tag>
            </div>
          </template>
          <div class="card-grid">
            <el-card
              v-for="student in groupedStudents.active"
              :key="student.id"
              class="student-card"
              shadow="hover"
              :body-style="{ padding: '16px' }"
            >
              <div class="card-header">
                <el-avatar :size="48" class="card-avatar">{{ student.realName?.charAt(0) }}</el-avatar>
                <div class="card-header-info">
                  <div class="card-name">{{ student.realName }}</div>
                  <div class="card-no">{{ student.studentNo || '无学号' }}</div>
                </div>
                <div class="card-status-badge active">正常</div>
              </div>
              <el-divider style="margin: 12px 0" />
              <div class="card-body">
                <div class="card-item">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span class="card-label">院系</span>
                  <span class="card-value">{{ student.department || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Collection /></el-icon>
                  <span class="card-label">专业</span>
                  <span class="card-value">{{ student.major || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><School /></el-icon>
                  <span class="card-label">班级</span>
                  <span class="card-value">{{ student.className || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Message /></el-icon>
                  <span class="card-label">邮箱</span>
                  <span class="card-value">{{ student.email || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Phone /></el-icon>
                  <span class="card-label">手机</span>
                  <span class="card-value">{{ student.phone || '-' }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </el-collapse-item>

        <!-- 禁用状态组 -->
        <el-collapse-item name="inactive" v-if="groupedStudents.inactive.length > 0">
          <template #title>
            <div class="group-title">
              <el-icon color="#e11d48"><CircleClose /></el-icon>
              <span>禁用状态</span>
              <el-tag size="small" type="danger" effect="dark">{{ groupedStudents.inactive.length }}</el-tag>
            </div>
          </template>
          <div class="card-grid">
            <el-card
              v-for="student in groupedStudents.inactive"
              :key="student.id"
              class="student-card inactive"
              shadow="hover"
              :body-style="{ padding: '16px' }"
            >
              <div class="card-header">
                <el-avatar :size="48" class="card-avatar inactive-avatar">{{ student.realName?.charAt(0) }}</el-avatar>
                <div class="card-header-info">
                  <div class="card-name">{{ student.realName }}</div>
                  <div class="card-no">{{ student.studentNo || '无学号' }}</div>
                </div>
                <div class="card-status-badge inactive">禁用</div>
              </div>
              <el-divider style="margin: 12px 0" />
              <div class="card-body">
                <div class="card-item">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span class="card-label">院系</span>
                  <span class="card-value">{{ student.department || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Collection /></el-icon>
                  <span class="card-label">专业</span>
                  <span class="card-value">{{ student.major || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><School /></el-icon>
                  <span class="card-label">班级</span>
                  <span class="card-value">{{ student.className || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Message /></el-icon>
                  <span class="card-label">邮箱</span>
                  <span class="card-value">{{ student.email || '-' }}</span>
                </div>
                <div class="card-item">
                  <el-icon><Phone /></el-icon>
                  <span class="card-label">手机</span>
                  <span class="card-value">{{ student.phone || '-' }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </el-collapse-item>
      </el-collapse>

      <!-- 空状态 -->
      <el-empty v-if="filteredStudents.length === 0" description="暂无学生数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api'
import { ElMessage } from 'element-plus'
import {
  UserFilled, Message, Phone, Refresh, Grid, List,
  Download, Search, Delete, Document, User, Collection,
  School, Filter, Check, CircleCheck, CircleClose, OfficeBuilding
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const exporting = ref(false)
const students = ref<any[]>([])
const viewMode = ref<'table' | 'card'>('table')
const currentPage = ref(1)
const pageSize = ref(10)
const departmentFilterEnabled = ref(false)
const activeGroups = ref(['active', 'inactive'])

// 查询表单
const searchForm = reactive({
  studentNo: '',
  realName: '',
  major: '',
  className: ''
})

// 是否有查询条件
const hasSearchCriteria = computed(() => {
  return searchForm.studentNo || searchForm.realName || searchForm.major || searchForm.className
})

// 表格表头样式
const headerStyle = () => ({
  background: '#f8fafc',
  fontWeight: 600,
  color: '#64748b',
  fontSize: '12px',
  letterSpacing: '0.5px',
  textTransform: 'uppercase' as const
})

// 活跃学生数
const activeCount = computed(() => {
  return filteredStudents.value.filter(s => s.status === 'ACTIVE').length
})

// 客户端筛选 + 院系过滤
const filteredStudents = computed(() => {
  let result = students.value

  // 院系过滤
  if (departmentFilterEnabled.value && userStore.user?.department) {
    result = result.filter(s => s.department === userStore.user?.department)
  }

  // 学号过滤
  if (searchForm.studentNo) {
    const kw = searchForm.studentNo.toLowerCase()
    result = result.filter(s => s.studentNo?.toLowerCase().includes(kw))
  }
  // 姓名过滤
  if (searchForm.realName) {
    const kw = searchForm.realName.toLowerCase()
    result = result.filter(s => s.realName?.toLowerCase().includes(kw))
  }
  // 专业过滤
  if (searchForm.major) {
    const kw = searchForm.major.toLowerCase()
    result = result.filter(s => s.major?.toLowerCase().includes(kw))
  }
  // 班级过滤
  if (searchForm.className) {
    const kw = searchForm.className.toLowerCase()
    result = result.filter(s => s.className?.toLowerCase().includes(kw))
  }

  return result
})

// 分页数据
const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

// 按状态分组的学生（卡片视图用）
const groupedStudents = computed(() => {
  const active = filteredStudents.value.filter(s => s.status === 'ACTIVE')
  const inactive = filteredStudents.value.filter(s => s.status !== 'ACTIVE')
  return { active, inactive }
})

// 切换视图
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'table' ? 'card' : 'table'
}

// 加载学生数据
const loadStudents = async () => {
  loading.value = true
  try {
    let res
    if (departmentFilterEnabled.value) {
      res = await userApi.getStudentsByTeacherDepartment(userStore.user!.id)
    } else {
      res = await userApi.getStudentsByTeacher(userStore.user!.id)
    }
    if (res.code === 200) {
      students.value = res.data || []
    }
  } catch (error) {
    console.error('加载学生失败:', error)
    ElMessage.error('加载学生数据失败')
  } finally {
    loading.value = false
  }
}

// 院系过滤切换
const onDepartmentFilterChange = () => {
  loadStudents()
}

// 查询
const handleSearch = () => {
  currentPage.value = 1
  if (hasSearchCriteria.value) {
    searchStudentsViaApi()
  }
}

// 后端搜索
const searchStudentsViaApi = async () => {
  loading.value = true
  try {
    const res = await userApi.searchStudents(
      userStore.user!.id,
      {
        studentNo: searchForm.studentNo || undefined,
        realName: searchForm.realName || undefined,
        major: searchForm.major || undefined,
        className: searchForm.className || undefined,
        useDepartmentFilter: departmentFilterEnabled.value
      }
    )
    if (res.code === 200) {
      students.value = res.data || []
    }
  } catch (error) {
    console.error('搜索学生失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetSearch = () => {
  searchForm.studentNo = ''
  searchForm.realName = ''
  searchForm.major = ''
  searchForm.className = ''
  currentPage.value = 1
  loadStudents()
}

// 导出学生数据
const exportStudents = () => {
  exporting.value = true
  try {
    const data = filteredStudents.value
    if (data.length === 0) {
      ElMessage.warning('没有数据可导出')
      exporting.value = false
      return
    }

    // 构建CSV内容
    const BOM = '\uFEFF'
    const headers = ['学号', '姓名', '院系', '专业', '班级', '邮箱', '手机号', '状态']
    const rows = data.map(s => [
      s.studentNo || '',
      s.realName || '',
      s.department || '',
      s.major || '',
      s.className || '',
      s.email || '',
      s.phone || '',
      s.status === 'ACTIVE' ? '正常' : '禁用'
    ])

    const csvContent = BOM + [headers, ...rows]
      .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
      .join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const timestamp = new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')
    link.href = URL.createObjectURL(blob)
    link.download = `学生数据_${userStore.user?.realName || '导师'}_${timestamp}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)

    ElMessage.success(`成功导出 ${data.length} 条学生数据`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => loadStudents())
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
  display: flex;
  align-items: center;
  gap: 8px;
}
.dept-tag {
  font-size: 11px;
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
  margin-bottom: 16px;
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
.dept-switch {
  margin-left: auto;
}

// 查询表单
.search-card {
  margin-bottom: 16px;
  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 12px;
  }
}

// 表格
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}
.student-no {
  font-family: var(--mono);
  font-weight: 600;
  color: var(--gray-700);
  font-size: 13px;
}
.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.name-avatar {
  background: linear-gradient(135deg, var(--primary-400), var(--accent-500));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}
.name-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.name-text {
  font-weight: 600;
  font-size: 13px;
}
.name-sub {
  font-size: 11px;
  color: var(--gray-400);
}
.class-tag {
  font-size: 12px;
  color: var(--gray-600);
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}
.email-cell, .phone-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--gray-600);
  .el-icon {
    color: var(--gray-400);
    font-size: 14px;
  }
}
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  &.active {
    background: #dcfce7;
    color: #16a34a;
  }
  &.inactive {
    background: #ffe4e6;
    color: #e11d48;
  }
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
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

// 学生卡片
.student-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  &.inactive {
    opacity: 0.85;
    .card-avatar {
      background: #e2e8f0;
      color: #94a3b8;
    }
  }
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-avatar {
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  flex-shrink: 0;
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
.card-status-badge {
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  &.active {
    background: #dcfce7;
    color: #16a34a;
  }
  &.inactive {
    background: #ffe4e6;
    color: #e11d48;
  }
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
  .search-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-right: 0;
    }
  }
  .stats-bar {
    .dept-switch {
      margin-left: 0;
      width: 100%;
    }
  }
}
</style>
