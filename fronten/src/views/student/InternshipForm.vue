<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <button class="back-btn" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <div>
          <h2 class="page-header-title">{{ isEdit ? '编辑实习信息' : '新增实习信息' }}</h2>
          <p class="page-header-desc">{{ isEdit ? '修改您的实习记录' : '填写您的实习详细信息' }}</p>
        </div>
      </div>
    </div>

    <el-card class="form-card animate-slide-up">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="modern-form">
        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="实习单位" prop="company">
              <el-input v-model="form.company" placeholder="请输入实习单位名称" :prefix-icon="OfficeBuilding" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="实习岗位">
              <el-input v-model="form.position" placeholder="请输入实习岗位" :prefix-icon="User" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="企业联系人">
              <el-input v-model="form.companyContact" placeholder="请输入企业联系人" :prefix-icon="UserFilled" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.companyPhone" placeholder="请输入联系电话" :prefix-icon="Phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="实习地址">
          <el-input v-model="form.address" placeholder="请输入实习地址" :prefix-icon="Location" />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="指导老师" prop="teacherId">
              <el-select v-model="form.teacherId" placeholder="请选择指导老师" style="width: 100%">
                <el-option v-for="teacher in teachers" :key="teacher.id"
                           :label="teacher.realName" :value="teacher.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="6">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期"
                              value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="6">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期"
                              value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="实习描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请描述实习内容" />
        </el-form-item>

        <el-form-item label="工作内容">
          <el-input v-model="form.workContent" type="textarea" :rows="4" placeholder="请详细描述工作内容" />
        </el-form-item>

        <div class="form-actions">
          <el-button size="large" @click="$router.back()">取消</el-button>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '保存修改' : '创建实习' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { internshipApi, userApi } from '@/api'
import { ArrowLeft, OfficeBuilding, User, UserFilled, Phone, Location, Check } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const teachers = ref<any[]>([])
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  company: '', position: '', address: '', companyContact: '', companyPhone: '',
  teacherId: null as number | null, startDate: '', endDate: '', description: '', workContent: '',
})

const rules = {
  company: [{ required: true, message: '请输入实习单位', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择指导老师', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}

const loadTeachers = async () => {
  try {
    const res = await userApi.getTeachers()
    if (res.code === 200) teachers.value = res.data
  } catch (error) { console.error(error) }
}

const loadInternship = async (id: number) => {
  try {
    const res = await internshipApi.get(id)
    if (res.code === 200) {
      const d = res.data
      Object.assign(form, {
        company: d.company, position: d.position, address: d.address,
        companyContact: d.companyContact, companyPhone: d.companyPhone,
        teacherId: d.teacherId, startDate: d.startDate, endDate: d.endDate,
        description: d.description, workContent: d.workContent,
      })
    }
  } catch (error) { console.error(error) }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    let res
    if (isEdit.value) res = await internshipApi.update(Number(route.params.id), form)
    else res = await internshipApi.create(form)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      router.push('/student/internships')
    }
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

onMounted(() => {
  loadTeachers()
  if (isEdit.value) loadInternship(Number(route.params.id))
})
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }

.page-header-left { display: flex; align-items: center; gap: 16px; }

.back-btn {
  width: 40px; height: 40px; border-radius: var(--radius-md);
  border: 1px solid var(--border-color); background: #ffffff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--gray-600);
  transition: all var(--transition-fast);
  &:hover { background: var(--gray-50); border-color: var(--primary-300); color: var(--primary-600); }
}

.form-card { margin-top: 4px; }

.modern-form {
  :deep(.el-form-item__label) {
    font-size: 13px; font-weight: 600; color: var(--gray-600); padding-bottom: 6px;
  }
  :deep(.el-input__wrapper) { padding: 4px 14px; height: 44px; }
  :deep(.el-textarea__inner) { padding: 10px 14px; border-radius: var(--radius-md); }
}

.form-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 32px; padding-top: 24px;
  border-top: 1px solid var(--border-light);

  .el-button { min-width: 120px; height: 44px; font-weight: 600; }
}
</style>
