<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <button class="back-btn" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <div>
          <h2 class="page-header-title">{{ isEdit ? '编辑汇报' : '提交汇报' }}</h2>
          <p class="page-header-desc">{{ isEdit ? '修改您的汇报内容' : '填写实习汇报详细信息' }}</p>
        </div>
      </div>
    </div>

    <el-card class="form-card animate-slide-up">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="modern-form">
        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="实习项目" prop="internshipId">
              <el-select v-model="form.internshipId" placeholder="选择实习项目" :disabled="isEdit"
                         @change="handleInternshipChange" style="width: 100%">
                <el-option v-for="item in internships" :key="item.id"
                           :label="`${item.company} - ${item.position || '未知岗位'}`" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="汇报类型" prop="reportType">
              <el-select v-model="form.reportType" placeholder="选择汇报类型" style="width: 100%">
                <el-option label="周报" value="WEEKLY">
                  <div class="type-option"><el-icon><Calendar /></el-icon><span>周报</span></div>
                </el-option>
                <el-option label="月报" value="MONTHLY">
                  <div class="type-option"><el-icon><DataLine /></el-icon><span>月报</span></div>
                </el-option>
                <el-option label="总结报告" value="FINAL">
                  <div class="type-option"><el-icon><DocumentChecked /></el-icon><span>总结报告</span></div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="汇报日期" prop="reportDate">
              <el-date-picker v-model="form.reportDate" type="date" placeholder="选择汇报日期"
                              value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="周次" v-if="form.reportType === 'WEEKLY'">
              <el-input-number v-model="form.weekNumber" :min="1" :max="52" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="汇报标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入汇报标题" />
        </el-form-item>

        <el-form-item label="汇报内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入汇报内容" />
        </el-form-item>

        <el-form-item label="工作总结">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入本周/本月工作总结" />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-form-item label="遇到的问题">
              <el-input v-model="form.problem" type="textarea" :rows="3" placeholder="请描述遇到的问题" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="解决方案">
              <el-input v-model="form.solution" type="textarea" :rows="3" placeholder="请描述解决方案" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="下周计划">
          <el-input v-model="form.plan" type="textarea" :rows="3" placeholder="请输入下周工作计划" />
        </el-form-item>

        <div class="form-actions">
          <el-button size="large" @click="$router.back()">取消</el-button>
          <el-button type="success" size="large" :loading="loading" v-if="!isEdit" @click="handleSubmitAndSubmit">
            <el-icon><Upload /></el-icon> 创建并提交
          </el-button>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            <el-icon><Check /></el-icon> {{ isEdit ? '保存修改' : '保存草稿' }}
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
import { reportApi, internshipApi } from '@/api'
import { ArrowLeft, Calendar, DataLine, DocumentChecked, Check, Upload } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const internships = ref<any[]>([])
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  internshipId: null as number | null, title: '', reportType: 'WEEKLY',
  content: '', summary: '', problem: '', solution: '', plan: '', reportDate: '', weekNumber: null as number | null,
})

const rules = {
  internshipId: [{ required: true, message: '请选择实习项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入汇报标题', trigger: 'blur' }],
  reportType: [{ required: true, message: '请选择汇报类型', trigger: 'change' }],
  reportDate: [{ required: true, message: '请选择汇报日期', trigger: 'change' }],
}

const loadInternships = async () => {
  try {
    const res = await internshipApi.getStudentInternships({ page: 0, size: 100 })
    if (res.code === 200) {
      internships.value = res.data.content.filter((item: any) => item.status === 'ONGOING')
      if (route.query.internshipId) form.internshipId = Number(route.query.internshipId)
    }
  } catch (error) { console.error(error) }
}

const loadReport = async (id: number) => {
  try {
    const res = await reportApi.get(id)
    if (res.code === 200) {
      const d = res.data
      Object.assign(form, {
        internshipId: d.internshipId, title: d.title, reportType: d.reportType,
        content: d.content, summary: d.summary, problem: d.problem,
        solution: d.solution, plan: d.plan, reportDate: d.reportDate, weekNumber: d.weekNumber,
      })
    }
  } catch (error) { console.error(error) }
}

const handleInternshipChange = () => {}

const handleSubmit = async (andSubmit = false) => {
  await formRef.value.validate()
  loading.value = true
  try {
    let res
    if (isEdit.value) res = await reportApi.update(Number(route.params.id), form)
    else res = await reportApi.create(form)
    if (res.code === 200) {
      if (andSubmit && !isEdit.value) {
        await reportApi.submit(res.data.id)
        ElMessage.success('创建并提交成功')
      } else {
        ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      }
      router.push('/student/reports')
    }
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const handleSubmitAndSubmit = () => handleSubmit(true)

onMounted(() => {
  loadInternships()
  if (isEdit.value) loadReport(Number(route.params.id))
})
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-left { display: flex; align-items: center; gap: 16px; }
.back-btn {
  width: 40px; height: 40px; border-radius: var(--radius-md); border: 1px solid var(--border-color);
  background: #ffffff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--gray-600); transition: all var(--transition-fast);
  &:hover { background: var(--gray-50); border-color: var(--primary-300); color: var(--primary-600); }
}
.form-card { margin-top: 4px; }
.modern-form {
  :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: var(--gray-600); padding-bottom: 6px; }
  :deep(.el-input__wrapper) { padding: 4px 14px; height: 44px; }
  :deep(.el-textarea__inner) { padding: 10px 14px; border-radius: var(--radius-md); }
}
.type-option { display: flex; align-items: center; gap: 8px; }
.form-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--border-light);
  .el-button { min-width: 120px; height: 44px; font-weight: 600; }
}
</style>
