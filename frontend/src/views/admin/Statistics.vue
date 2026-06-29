<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">系统统计</h2>
        <p class="page-header-desc">查看系统整体运行数据</p>
      </div>
    </div>

    <!-- Main stats -->
    <div class="stats-grid stagger-children">
      <div v-for="(stat, index) in mainStats" :key="index" class="stat-card-premium" :style="{ animationDelay: `${index * 60}ms` }">
        <div class="stat-card-bg" :style="{ background: stat.gradient }"></div>
        <div class="stat-card-content">
          <div class="stat-icon-wrap" :style="{ background: stat.iconBg, color: stat.iconColor }">
            <el-icon :size="22"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-name">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'neutral'">
            <el-icon v-if="stat.trend > 0"><ArrowUp /></el-icon>
            <span>{{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card animate-fade-in" style="animation-delay: 0.3s">
          <template #header>
            <div class="chart-header">
              <div class="chart-title">
                <div class="chart-icon" style="background: linear-gradient(135deg, #6366f1, #818cf8);">
                  <el-icon><PieChart /></el-icon>
                </div>
                实习状态统计
              </div>
            </div>
          </template>
          <div class="modern-chart">
            <div v-for="(item, index) in internshipChartData" :key="index" class="chart-row">
              <div class="chart-label">
                <span class="chart-dot" :style="{ background: item.color }"></span>
                <span>{{ item.label }}</span>
              </div>
              <div class="chart-bar-wrapper">
                <div class="chart-bar-bg">
                  <div class="chart-bar-fill" :style="{
                    width: item.percent + '%',
                    background: `linear-gradient(90deg, ${item.color}, ${item.colorLight})`,
                  }">
                    <div class="bar-shimmer"></div>
                  </div>
                </div>
              </div>
              <div class="chart-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="chart-card animate-fade-in" style="animation-delay: 0.4s">
          <template #header>
            <div class="chart-header">
              <div class="chart-title">
                <div class="chart-icon" style="background: linear-gradient(135deg, #22c55e, #4ade80);">
                  <el-icon><DataAnalysis /></el-icon>
                </div>
                汇报状态统计
              </div>
            </div>
          </template>
          <div class="modern-chart">
            <div v-for="(item, index) in reportChartData" :key="index" class="chart-row">
              <div class="chart-label">
                <span class="chart-dot" :style="{ background: item.color }"></span>
                <span>{{ item.label }}</span>
              </div>
              <div class="chart-bar-wrapper">
                <div class="chart-bar-bg">
                  <div class="chart-bar-fill" :style="{
                    width: item.percent + '%',
                    background: `linear-gradient(90deg, ${item.color}, ${item.colorLight})`,
                  }">
                    <div class="bar-shimmer"></div>
                  </div>
                </div>
              </div>
              <div class="chart-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Summary row -->
    <el-card class="summary-card animate-slide-up" style="animation-delay: 0.5s">
      <div class="summary-grid">
        <div class="summary-item">
          <div class="summary-icon" style="background: linear-gradient(135deg, #eef2ff, #e0e7ff); color: #6366f1;">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-value">{{ stats.totalStudents + stats.totalTeachers + stats.totalAdmins }}</div>
            <div class="summary-label">系统总用户数</div>
          </div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-icon" style="background: linear-gradient(135deg, #dcfce7, #d1fae5); color: #22c55e;">
            <el-icon :size="24"><DocumentChecked /></el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-value">{{ stats.completedInternships }}</div>
            <div class="summary-label">已完成实习</div>
          </div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-icon" style="background: linear-gradient(135deg, #fef3c7, #fde68a); color: #f59e0b;">
            <el-icon :size="24"><EditPen /></el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-value">{{ stats.reviewedReports }}</div>
            <div class="summary-label">已批阅汇报</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { statsApi } from '@/api'
import {
  UserFilled, School, Timer, DocumentChecked,
  ArrowUp, PieChart, DataAnalysis, EditPen,
} from '@element-plus/icons-vue'

const stats = ref({
  totalStudents: 0, totalTeachers: 0, totalAdmins: 0,
  draftInternships: 0, pendingInternships: 0, ongoingInternships: 0, completedInternships: 0,
  internshipStudents: 0,
  draftReports: 0, submittedReports: 0, reviewedReports: 0, returnedReports: 0,
})

const mainStats = computed(() => [
  { icon: UserFilled, value: stats.value.totalStudents, label: '学生总数', gradient: 'linear-gradient(135deg, #6366f1, #818cf8)', iconBg: '#eef2ff', iconColor: '#6366f1', trend: 12 },
  { icon: School, value: stats.value.totalTeachers, label: '导师总数', gradient: 'linear-gradient(135deg, #f59e0b, #fbbf24)', iconBg: '#fef3c7', iconColor: '#f59e0b', trend: 5 },
  { icon: Timer, value: stats.value.internshipStudents, label: '实习学生数', gradient: 'linear-gradient(135deg, #22c55e, #4ade80)', iconBg: '#dcfce7', iconColor: '#22c55e', trend: 8 },
  { icon: DocumentChecked, value: stats.value.reviewedReports, label: '已批阅汇报', gradient: 'linear-gradient(135deg, #0ea5e9, #38bdf8)', iconBg: '#e0f2fe', iconColor: '#0ea5e9', trend: 15 },
])

const internshipChartData = computed(() => {
  const total = stats.value.draftInternships + stats.value.pendingInternships + stats.value.ongoingInternships + stats.value.completedInternships
  if (total === 0) return []
  return [
    { label: '草稿', value: stats.value.draftInternships, color: '#94a3b8', colorLight: '#cbd5e1', percent: (stats.value.draftInternships / total) * 100 },
    { label: '待审核', value: stats.value.pendingInternships, color: '#f59e0b', colorLight: '#fbbf24', percent: (stats.value.pendingInternships / total) * 100 },
    { label: '进行中', value: stats.value.ongoingInternships, color: '#6366f1', colorLight: '#818cf8', percent: (stats.value.ongoingInternships / total) * 100 },
    { label: '已完成', value: stats.value.completedInternships, color: '#22c55e', colorLight: '#4ade80', percent: (stats.value.completedInternships / total) * 100 },
  ]
})

const reportChartData = computed(() => {
  const total = stats.value.draftReports + stats.value.submittedReports + stats.value.reviewedReports + stats.value.returnedReports
  if (total === 0) return []
  return [
    { label: '草稿', value: stats.value.draftReports, color: '#94a3b8', colorLight: '#cbd5e1', percent: (stats.value.draftReports / total) * 100 },
    { label: '已提交', value: stats.value.submittedReports, color: '#f59e0b', colorLight: '#fbbf24', percent: (stats.value.submittedReports / total) * 100 },
    { label: '已批阅', value: stats.value.reviewedReports, color: '#22c55e', colorLight: '#4ade80', percent: (stats.value.reviewedReports / total) * 100 },
    { label: '已退回', value: stats.value.returnedReports, color: '#f43f5e', colorLight: '#fb7185', percent: (stats.value.returnedReports / total) * 100 },
  ]
})

const loadStats = async () => {
  try {
    const res = await statsApi.getOverview()
    if (res.code === 200) stats.value = res.data
  } catch (error) { console.error(error) }
}

onMounted(() => loadStats())
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card-premium {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  &:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
}

.stat-card-bg {
  position: absolute;
  inset: 0;
  opacity: 0.08;
}

.stat-card-content {
  position: relative;
  padding: 20px;
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--gray-900); line-height: 1.2; }
.stat-name { font-size: 12px; color: var(--text-muted); font-weight: 500; margin-top: 2px; }

.stat-trend {
  display: flex; align-items: center; gap: 2px;
  font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: var(--radius-full);
  &.up { color: #16a34a; background: #dcfce7; }
  &.neutral { color: #64748b; background: #f1f5f9; }
}

// Charts
.charts-row { margin-bottom: 20px; }

.chart-card { height: 100%; }
.chart-header { display: flex; align-items: center; }
.chart-title { display: flex; align-items: center; gap: 10px; font-weight: 600; font-size: 15px; color: var(--gray-700); }
.chart-icon { width: 32px; height: 32px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; color: #fff; }

.modern-chart { display: flex; flex-direction: column; gap: 20px; }
.chart-row { display: flex; align-items: center; gap: 14px; }
.chart-label { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 500; color: var(--gray-600); width: 70px; flex-shrink: 0; }
.chart-dot { width: 8px; height: 8px; border-radius: 50%; }
.chart-bar-wrapper { flex: 1; }
.chart-bar-bg { width: 100%; height: 10px; background: var(--gray-100); border-radius: var(--radius-full); overflow: hidden; }
.chart-bar-fill { height: 100%; border-radius: var(--radius-full); transition: width 1s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden; }
.bar-shimmer { position: absolute; inset: 0; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent); animation: shimmer 2s infinite; }
.chart-value { font-size: 16px; font-weight: 700; color: var(--gray-800); width: 40px; text-align: right; flex-shrink: 0; }

// Summary
.summary-card { margin-top: 4px; }
.summary-grid { display: flex; align-items: center; gap: 0; padding: 8px; }
.summary-item { flex: 1; display: flex; align-items: center; gap: 16px; padding: 12px 24px; }
.summary-divider { width: 1px; height: 50px; background: var(--border-color); }
.summary-icon { width: 52px; height: 52px; border-radius: var(--radius-lg); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.summary-info { flex: 1; }
.summary-value { font-size: 24px; font-weight: 700; color: var(--gray-900); }
.summary-label { font-size: 13px; color: var(--text-muted); font-weight: 500; }

@media (max-width: 768px) {
  .summary-grid { flex-direction: column; }
  .summary-divider { width: 100%; height: 1px; }
  .summary-item { width: 100%; }
}
</style>
