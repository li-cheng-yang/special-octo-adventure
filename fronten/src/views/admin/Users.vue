<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">用户管理</h2>
        <p class="page-header-desc">管理系统中的所有用户</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-bar animate-fade-in" style="animation-delay: 0.05s">
      <el-radio-group v-model="filterRole" @change="loadUsers" size="large">
        <el-radio-button value="STUDENT"><el-icon><UserFilled /></el-icon> 学生</el-radio-button>
        <el-radio-button value="TEACHER"><el-icon><School /></el-icon> 导师</el-radio-button>
        <el-radio-button value="ADMIN"><el-icon><Monitor /></el-icon> 管理员</el-radio-button>
      </el-radio-group>
      <el-input v-model="keyword" placeholder="搜索姓名/学号" style="width: 240px" @keyup.enter="loadUsers" clearable :prefix-icon="Search" />
    </div>

    <el-card class="table-card animate-slide-up" style="animation-delay: 0.1s" v-loading="loading">
      <el-table :data="users" style="width: 100%" :header-cell-style="headerStyle">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="studentNo" label="学号/工号" width="120">
          <template #default="{ row }">
            <span class="student-no">{{ row.studentNo || row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar :size="28" class="name-avatar">{{ row.realName?.charAt(0) }}</el-avatar>
              <span>{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="院系" width="140" />
        <el-table-column prop="major" label="专业" width="120" />
        <el-table-column prop="className" label="班级" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160">
          <template #default="{ row }">
            <div class="email-cell"><el-icon><Message /></el-icon><span>{{ row.email || '-' }}</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120">
          <template #default="{ row }">
            <div class="phone-cell"><el-icon><Phone /></el-icon><span>{{ row.phone || '-' }}</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status === 'ACTIVE' ? 'active' : 'inactive'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ACTIVE"
              inactive-value="INACTIVE"
              @change="(val: any) => changeStatus(row, val)"
              style="--el-switch-on-color: #22c55e; --el-switch-off-color: #f43f5e"
            />
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
                       :total="pagination.total" :page-sizes="[10, 20, 50]"
                       layout="total, sizes, prev, pager, next"
                       @size-change="loadUsers" @current-change="loadUsers" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import { Search, UserFilled, School, Monitor, Message, Phone } from '@element-plus/icons-vue'

const loading = ref(false)
const users = ref<any[]>([])
const filterRole = ref('STUDENT')
const keyword = ref('')
const pagination = reactive({ page: 1, size: 10, total: 0 })

const headerStyle = () => ({ background: '#f8fafc', fontWeight: 600, color: '#64748b', fontSize: '12px', letterSpacing: '0.5px', textTransform: 'uppercase' })

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.getUsers({
      role: filterRole.value, keyword: keyword.value,
      page: pagination.page - 1, size: pagination.size,
    })
    if (res.code === 200) { users.value = res.data.content; pagination.total = res.data.totalElements }
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const changeStatus = async (row: any, status: string) => {
  try {
    await ElMessageBox.confirm(`确定要${status === 'ACTIVE' ? '启用' : '禁用'}用户 "${row.realName}" 吗？`, '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
    })
    const res = await userApi.updateUserStatus(row.id, status)
    if (res.code === 200) { ElMessage.success('操作成功'); loadUsers() }
  } catch (error) {
    row.status = status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    console.error(error)
  }
}

onMounted(() => loadUsers())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { margin-bottom: 20px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.filters-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; flex-wrap: wrap; }

.student-no { font-family: var(--mono); font-weight: 600; color: var(--gray-700); font-size: 13px; }
.name-cell { display: flex; align-items: center; gap: 10px; }
.name-avatar { background: linear-gradient(135deg, var(--primary-400), var(--accent-500)); color: #fff; font-size: 12px; font-weight: 600; }
.email-cell, .phone-cell { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--gray-600);
  .el-icon { color: var(--gray-400); font-size: 14px; }
}
.status-badge {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 4px 12px; border-radius: var(--radius-full); font-size: 12px; font-weight: 600;
  &.active { background: #dcfce7; color: #16a34a; }
  &.inactive { background: #ffe4e6; color: #e11d48; }
}
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
