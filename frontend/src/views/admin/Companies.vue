<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">企业管理</h2>
        <p class="page-header-desc">管理系统合作企业信息</p>
      </div>
      <el-button type="primary" size="large" @click="showDialog = true" class="btn-gradient">
        <el-icon><Plus /></el-icon>
        新增企业
      </el-button>
    </div>

    <div class="filters-bar animate-fade-in" style="animation-delay: 0.05s">
      <el-radio-group v-model="filterStatus" @change="loadCompanies" size="large">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="ACTIVE">正常合作</el-radio-button>
        <el-radio-button value="INACTIVE">暂停合作</el-radio-button>
        <el-radio-button value="BLACKLISTED">黑名单</el-radio-button>
      </el-radio-group>
      <el-input v-model="keyword" placeholder="搜索企业名称/行业/城市" style="width: 280px" @keyup.enter="loadCompanies" clearable :prefix-icon="Search" />
    </div>

    <el-card class="table-card animate-slide-up" style="animation-delay: 0.1s" v-loading="loading">
      <el-table :data="companies" style="width: 100%" :header-cell-style="headerStyle">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="name" label="企业名称" min-width="160">
          <template #default="{ row }">
            <div class="company-cell">
              <div class="company-icon" :style="{ background: stringToColor(row.name) }">
                {{ row.name.charAt(0) }}
              </div>
              <div class="company-info">
                <div class="company-name">{{ row.name }}</div>
                <div class="company-industry">{{ row.industry || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="100">
          <template #default="{ row }">
            <div class="location-cell">
              <el-icon><Location /></el-icon>
              <span>{{ row.city || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120">
          <template #default="{ row }">
            <div class="contact-cell">
              <el-icon><User /></el-icon>
              <span>{{ row.contactName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="130">
          <template #default="{ row }">
            <div class="phone-cell">
              <el-icon><Phone /></el-icon>
              <span>{{ row.contactPhone || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="实习人数" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.studentCount > 0 ? 'success' : 'info'" effect="light" size="small">
              {{ row.studentCount }} 人
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status.toLowerCase()">
              {{ statusLabel(row.status) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn edit" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn delete" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
                       :total="pagination.total" :page-sizes="[10, 20, 50]"
                       layout="total, sizes, prev, pager, next"
                       @size-change="loadCompanies" @current-change="loadCompanies" />
      </div>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑企业' : '新增企业'" width="680px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="modern-form">
        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="企业名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入企业名称" :prefix-icon="OfficeBuilding" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="所属行业">
              <el-input v-model="form.industry" placeholder="如: 互联网、金融等" :prefix-icon="Collection" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="所在省份">
              <el-input v-model="form.province" placeholder="省份" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="所在城市">
              <el-input v-model="form.city" placeholder="城市" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址">
          <el-input v-model="form.address" placeholder="企业详细地址" :prefix-icon="Location" />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" placeholder="企业联系人姓名" :prefix-icon="User" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="联系人电话" :prefix-icon="Phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" placeholder="邮箱地址" :prefix-icon="Message" />
        </el-form-item>

        <el-form-item label="企业官网">
          <el-input v-model="form.website" placeholder="http://" :prefix-icon="Link" />
        </el-form-item>

        <el-form-item label="企业描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="企业简介" />
        </el-form-item>

        <el-form-item label="状态" v-if="isEdit">
          <el-radio-group v-model="form.status">
            <el-radio-button value="ACTIVE">正常合作</el-radio-button>
            <el-radio-button value="INACTIVE">暂停合作</el-radio-button>
            <el-radio-button value="BLACKLISTED">黑名单</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          <el-icon><Check /></el-icon>
          {{ isEdit ? '保存修改' : '创建企业' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { companyApi } from '@/api'
import { Plus, Search, Location, User, Phone, Message, OfficeBuilding, Collection, Link, Edit, Delete, Check } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const companies = ref<any[]>([])
const showDialog = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const keyword = ref('')
const filterStatus = ref('')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = reactive({
  name: '', industry: '', address: '', city: '', province: '',
  contactName: '', contactPhone: '', contactEmail: '',
  description: '', logoUrl: '', website: '', status: 'ACTIVE'
})

const rules = {
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
}

const headerStyle = () => ({
  background: '#f8fafc', fontWeight: 600, color: '#64748b', fontSize: '12px',
  letterSpacing: '0.5px', textTransform: 'uppercase'
})

const statusLabel = (status: string) => {
  const labels: Record<string, string> = { ACTIVE: '正常合作', INACTIVE: '暂停合作', BLACKLISTED: '黑名单' }
  return labels[status] || status
}

const stringToColor = (str: string) => {
  const colors = ['#6366f1', '#22c55e', '#f59e0b', '#f43f5e', '#0ea5e9', '#8b5cf6', '#ec4899']
  let hash = 0
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

const loadCompanies = async () => {
  loading.value = true
  try {
    const res = await companyApi.search({
      keyword: keyword.value || undefined,
      status: filterStatus.value || undefined,
      page: pagination.page - 1, size: pagination.size
    })
    if (res.code === 200) {
      companies.value = res.data.content
      pagination.total = res.data.totalElements
    }
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const resetForm = () => {
  form.name = ''; form.industry = ''; form.address = ''; form.city = ''; form.province = ''
  form.contactName = ''; form.contactPhone = ''; form.contactEmail = ''
  form.description = ''; form.logoUrl = ''; form.website = ''; form.status = 'ACTIVE'
  isEdit.value = false
  editingId.value = null
}

const handleEdit = (row: any) => {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, industry: row.industry, address: row.address, city: row.city, province: row.province,
    contactName: row.contactName, contactPhone: row.contactPhone, contactEmail: row.contactEmail,
    description: row.description, logoUrl: row.logoUrl, website: row.website, status: row.status
  })
  showDialog.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    let res
    if (isEdit.value && editingId.value) {
      res = await companyApi.update(editingId.value, form)
    } else {
      res = await companyApi.create(form)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      showDialog.value = false
      resetForm()
      loadCompanies()
    }
  } catch (error) { console.error(error) }
  finally { submitLoading.value = false }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除企业 "${row.name}" 吗？`, '警告', { type: 'warning' })
    const res = await companyApi.delete(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadCompanies() }
  } catch (error) { console.error(error) }
}

onMounted(() => loadCompanies())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }
.btn-gradient { background: linear-gradient(135deg, var(--primary-500), var(--primary-600)); border: none; box-shadow: 0 4px 14px rgba(99, 102, 241, 0.3); }

.filters-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; gap: 16px; flex-wrap: wrap; }

.company-cell { display: flex; align-items: center; gap: 12px; }
.company-icon { width: 38px; height: 38px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 16px; font-weight: 700; flex-shrink: 0; }
.company-info { min-width: 0; }
.company-name { font-weight: 600; color: var(--gray-800); font-size: 14px; }
.company-industry { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.location-cell, .contact-cell, .phone-cell { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--gray-600); .el-icon { color: var(--gray-400); } }

.status-badge { display: inline-flex; align-items: center; justify-content: center; padding: 4px 12px; border-radius: var(--radius-full); font-size: 12px; font-weight: 600;
  &.active { background: #dcfce7; color: #16a34a; }
  &.inactive { background: #fef3c7; color: #d97706; }
  &.blacklisted { background: #ffe4e6; color: #e11d48; }
}

.action-group { display: flex; gap: 6px; justify-content: center; }
.action-btn { width: 32px; height: 32px; border-radius: var(--radius-sm); border: none; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all var(--transition-fast); font-size: 14px;
  &.edit { background: #fef3c7; color: #d97706; &:hover { background: #f59e0b; color: #fff; } }
  &.delete { background: #ffe4e6; color: #e11d48; &:hover { background: #f43f5e; color: #fff; } }
}
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

.modern-form {
  :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: var(--gray-600); padding-bottom: 6px; }
  :deep(.el-input__wrapper) { padding: 4px 14px; height: 44px; }
  :deep(.el-textarea__inner) { padding: 10px 14px; border-radius: var(--radius-md); }
}
</style>
