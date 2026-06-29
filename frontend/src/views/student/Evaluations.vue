<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">我的评价</h2>
        <p class="page-header-desc">查看导师和企业对您实习表现的评价</p>
      </div>
    </div>

    <div v-if="pageError" class="error-fallback">
      <el-result icon="error" title="加载失败" :sub-title="pageError">
        <template #extra>
          <el-button type="primary" @click="retryLoad">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="evaluations.length === 0 && !loading" class="empty-state animate-fade-in">
      <el-empty description="暂无评价记录" />
    </div>

    <div v-else class="evaluation-list">
      <el-card v-for="item in evaluations" :key="item.id" class="evaluation-card animate-slide-up">
        <div class="evaluation-header">
          <div class="evaluation-info">
            <el-avatar :size="36" class="evaluator-avatar">{{ item.evaluatorName?.charAt(0) }}</el-avatar>
            <div>
              <div class="evaluator-name">{{ item.evaluatorName }} <el-tag size="small" :type="item.evaluatorType === 'TEACHER' ? 'primary' : 'success'">{{ item.evaluatorType === 'TEACHER' ? '导师' : '企业' }}</el-tag></div>
              <div class="evaluation-meta">{{ item.company }} · {{ item.position }}</div>
            </div>
          </div>
          <div class="evaluation-score">
            <div class="score-value">{{ item.overall }}</div>
            <div class="score-label">综合评分</div>
          </div>
        </div>

        <el-divider />

        <div class="score-grid">
          <div class="score-item" v-for="field in scoreFields" :key="field.key">
            <div class="score-item-label">{{ field.label }}</div>
            <el-progress :percentage="item[field.key]" :color="getScoreColor(item[field.key])" :stroke-width="8" />
          </div>
        </div>

        <el-divider />

        <div class="evaluation-content">
          <div class="content-section" v-if="item.strengths">
            <div class="content-title"><el-icon><CircleCheck /></el-icon> 优点/亮点</div>
            <p>{{ item.strengths }}</p>
          </div>
          <div class="content-section" v-if="item.weaknesses">
            <div class="content-title"><el-icon><Warning /></el-icon> 不足/待改进</div>
            <p>{{ item.weaknesses }}</p>
          </div>
          <div class="content-section" v-if="item.suggestions">
            <div class="content-title"><el-icon><Edit /></el-icon> 建议/期望</div>
            <p>{{ item.suggestions }}</p>
          </div>
        </div>

        <div class="evaluation-footer">
          <span class="evaluation-time">{{ item.createdAt }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { evaluationApi } from '@/api'
import { CircleCheck, Warning, Edit } from '@element-plus/icons-vue'

const loading = ref(false)
const evaluations = ref<any[]>([])
const pageError = ref('')

const scoreFields = [
  { key: 'workPerformance', label: '工作表现' },
  { key: 'learningAbility', label: '学习能力' },
  { key: 'communication', label: '沟通协作' },
  { key: 'punctuality', label: '出勤守时' },
]

const getScoreColor = (score: number) => {
  if (score >= 90) return '#22c55e'
  if (score >= 80) return '#6366f1'
  if (score >= 70) return '#f59e0b'
  if (score >= 60) return '#f97316'
  return '#f43f5e'
}

const loadEvaluations = async () => {
  loading.value = true
  pageError.value = ''
  try {
    const res = await evaluationApi.getStudentEvaluations()
    if (res.code === 200) {
      evaluations.value = res.data || []
    }
  } catch (error: any) {
    console.error('[Evaluations] 加载失败:', error)
    pageError.value = error.message || '加载评价数据失败'
  } finally { loading.value = false }
}

const retryLoad = () => { pageError.value = ''; loadEvaluations() }

onMounted(() => loadEvaluations())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.error-fallback { padding: 40px; }
.empty-state { padding: 60px 0; }

.evaluation-list { display: flex; flex-direction: column; gap: 16px; }

.evaluation-card { border-radius: var(--radius-lg); }

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.evaluation-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.evaluator-avatar {
  background: linear-gradient(135deg, var(--primary-500), var(--accent-500));
  color: #fff;
  font-weight: 600;
}

.evaluator-name {
  font-weight: 600;
  color: var(--gray-800);
  display: flex;
  align-items: center;
  gap: 8px;
}

.evaluation-meta {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 2px;
}

.evaluation-score {
  text-align: center;
}

.score-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--primary-600);
  line-height: 1;
}

.score-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}

.score-item-label {
  font-size: 13px;
  color: var(--gray-600);
  margin-bottom: 4px;
}

.evaluation-content {
  .content-section {
    margin-bottom: 16px;
    &:last-child { margin-bottom: 0; }
  }
  .content-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    font-size: 14px;
    color: var(--gray-700);
    margin-bottom: 8px;
    .el-icon { color: var(--primary-500); }
  }
  p {
    margin: 0;
    font-size: 13px;
    color: var(--gray-600);
    line-height: 1.7;
  }
}

.evaluation-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
  text-align: right;
}

.evaluation-time {
  font-size: 12px;
  color: var(--gray-400);
}
</style>
