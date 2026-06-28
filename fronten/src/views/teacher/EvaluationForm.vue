<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">实习评价</h2>
        <p class="page-header-desc">对学生的实习表现进行评价反馈</p>
      </div>
    </div>

    <el-card class="form-card animate-slide-up">
      <el-form :model="form" label-position="top" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择实习" prop="internshipId">
              <el-select v-model="form.internshipId" placeholder="请选择要评价的实习" style="width: 100%">
                <el-option
                  v-for="item in internships"
                  :key="item.id"
                  :label="`${item.studentName} - ${getCompanyName(item)}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评价者类型" prop="evaluatorType">
              <el-radio-group v-model="form.evaluatorType">
                <el-radio-button value="TEACHER">导师评价</el-radio-button>
                <el-radio-button value="COMPANY">企业评价</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>能力评分</el-divider>

        <el-row :gutter="20">
          <el-col :span="12" v-for="field in scoreFields" :key="field.key">
            <el-form-item :label="field.label" :prop="field.key">
              <div class="score-slider">
                <el-slider v-model="form[field.key]" :min="0" :max="100" :step="5" show-stops show-input />
                <span class="score-hint">{{ getScoreHint(form[field.key]) }}</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>综合评价</el-divider>

        <el-form-item label="优点/亮点" prop="strengths">
          <el-input v-model="form.strengths" type="textarea" :rows="3" placeholder="请描述学生在实习中的优点和亮点" />
        </el-form-item>

        <el-form-item label="不足/待改进" prop="weaknesses">
          <el-input v-model="form.weaknesses" type="textarea" :rows="3" placeholder="请描述学生需要改进的地方" />
        </el-form-item>

        <el-form-item label="建议/期望" prop="suggestions">
          <el-input v-model="form.suggestions" type="textarea" :rows="3" placeholder="请给出针对性的建议和期望" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" @click="submit" :loading="submitting">
            <el-icon><CircleCheck /></el-icon> 提交评价
          </el-button>
          <el-button size="large" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { internshipApi, evaluationApi } from '@/api'
import { CircleCheck } from '@element-plus/icons-vue'

const formRef = ref<FormInstance>()
const internships = ref<any[]>([])
const submitting = ref(false)

const form = reactive({
  internshipId: null as number | null,
  evaluatorType: 'TEACHER',
  workPerformance: 80,
  learningAbility: 80,
  communication: 80,
  punctuality: 80,
  overall: 80,
  strengths: '',
  weaknesses: '',
  suggestions: '',
})

const scoreFields = [
  { key: 'workPerformance', label: '工作表现' },
  { key: 'learningAbility', label: '学习能力' },
  { key: 'communication', label: '沟通协作' },
  { key: 'punctuality', label: '出勤守时' },
  { key: 'overall', label: '综合评价' },
]

const rules: FormRules = {
  internshipId: [{ required: true, message: '请选择实习', trigger: 'change' }],
  evaluatorType: [{ required: true, message: '请选择评价者类型', trigger: 'change' }],
  overall: [{ required: true, message: '请给出综合评分', trigger: 'change' }],
}

const getCompanyName = (row: any): string => {
  if (!row) return '未知企业'
  const c = row.company
  if (typeof c === 'string') return c
  if (c && typeof c === 'object') return c.name || c.companyName || '未知企业'
  return row.companyName || '未知企业'
}

const getScoreHint = (score: number) => {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  if (score >= 60) return '及格'
  return '待改进'
}

const loadInternships = async () => {
  try {
    const res = await internshipApi.getTeacherInternships({ page: 0, size: 100 })
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) { internships.value = data }
      else if (data && Array.isArray(data.content)) { internships.value = data.content }
    }
  } catch (error) { console.error(error) }
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await evaluationApi.create({ ...form })
      if (res.code === 200) {
        ElMessage.success('评价提交成功')
        reset()
      }
    } catch (error) { console.error(error) }
    finally { submitting.value = false }
  })
}

const reset = () => {
  form.internshipId = null
  form.evaluatorType = 'TEACHER'
  form.workPerformance = 80
  form.learningAbility = 80
  form.communication = 80
  form.punctuality = 80
  form.overall = 80
  form.strengths = ''
  form.weaknesses = ''
  form.suggestions = ''
  formRef.value?.resetFields()
}

onMounted(() => loadInternships())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.form-card { border-radius: var(--radius-lg); max-width: 800px; }

.score-slider {
  .score-hint {
    font-size: 12px;
    color: var(--primary-500);
    margin-left: 8px;
  }
}
</style>
