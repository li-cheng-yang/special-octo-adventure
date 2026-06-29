<template>
  <div class="page-wrapper">
    <div class="page-header-modern animate-fade-in">
      <div class="page-header-left">
        <h2 class="page-header-title">数据仪表盘</h2>
        <p class="page-header-desc">实习汇报系统数据可视化分析</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid animate-fade-in" style="animation-delay: 0.05s">
      <div class="stat-card" v-for="stat in statsList" :key="stat.key">
        <div class="stat-icon" :style="{ background: stat.bg }">
          <el-icon :size="22" :style="{ color: stat.color }">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats[stat.key] || 0 }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <el-card class="chart-card animate-slide-up" style="animation-delay: 0.1s">
        <template #header>
          <div class="chart-header">
            <el-icon><PieChart /></el-icon>
            <span>实习状态分布</span>
          </div>
        </template>
        <div ref="pieChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="chart-card animate-slide-up" style="animation-delay: 0.15s">
        <template #header>
          <div class="chart-header">
            <el-icon><TrendCharts /></el-icon>
            <span>评分趋势</span>
          </div>
        </template>
        <div ref="lineChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="chart-card animate-slide-up" style="animation-delay: 0.2s">
        <template #header>
          <div class="chart-header">
            <el-icon><MapLocation /></el-icon>
            <span>打卡热力图</span>
          </div>
        </template>
        <div ref="barChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="chart-card animate-slide-up" style="animation-delay: 0.25s">
        <template #header>
          <div class="chart-header">
            <el-icon><OfficeBuilding /></el-icon>
            <span>企业排名 TOP10</span>
          </div>
        </template>
        <div ref="rankChartRef" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { dashboardApi } from '@/api'
import { User, Document, Edit, OfficeBuilding, PieChart, TrendCharts, MapLocation, Bell } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref<any>({})
const pieChartRef = ref<HTMLDivElement>()
const lineChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
const rankChartRef = ref<HTMLDivElement>()

let pieChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let rankChart: echarts.ECharts | null = null

const statsList = [
  { key: 'totalStudents', label: '学生总数', icon: User, bg: '#eef2ff', color: '#6366f1' },
  { key: 'totalTeachers', label: '导师总数', icon: User, bg: '#fef3c7', color: '#f59e0b' },
  { key: 'totalInternships', label: '实习总数', icon: Document, bg: '#dcfce7', color: '#22c55e' },
  { key: 'totalReports', label: '汇报总数', icon: Edit, bg: '#fce7f3', color: '#ec4899' },
  { key: 'totalCompanies', label: '企业总数', icon: OfficeBuilding, bg: '#cffafe', color: '#06b6d4' },
  { key: 'totalCheckins', label: '打卡总数', icon: MapLocation, bg: '#f3e8ff', color: '#8b5cf6' },
]

const loadData = async () => {
  try {
    const res = await dashboardApi.getStats()
    if (res.code === 200) stats.value = res.data || {}

    const [pieRes, lineRes, barRes, rankRes] = await Promise.all([
      dashboardApi.getInternshipDistribution(),
      dashboardApi.getScoreTrend(),
      dashboardApi.getCheckinHeatmap(),
      dashboardApi.getCompanyRanking(),
    ])

    if (pieRes.code === 200) initPieChart(pieRes.data || [])
    if (lineRes.code === 200) initLineChart(lineRes.data || [])
    if (barRes.code === 200) initBarChart(barRes.data || [])
    if (rankRes.code === 200) initRankChart(rankRes.data || [])
  } catch (error) {
    console.error('[Dashboard] 加载数据失败:', error)
  }
}

const initPieChart = (data: any[]) => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.length > 0 ? data : [
        { name: '草稿', value: 0 },
        { name: '待审核', value: 0 },
        { name: '进行中', value: 0 },
        { name: '已完成', value: 0 },
        { name: '已驳回', value: 0 },
      ],
    }]
  })
}

const initLineChart = (data: any[]) => {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.date), axisLabel: { rotate: 45 } },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [{
      data: data.map(d => d.score),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(99, 102, 241, 0.3)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0.05)' },
        ])
      },
      itemStyle: { color: '#6366f1' },
    }]
  })
}

const initBarChart = (data: any[]) => {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  const recentData = data.slice(-14)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: recentData.map(d => d.date.slice(5)), axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [{
      data: recentData.map(d => d.count),
      type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#22c55e' },
          { offset: 1, color: '#16a34a' },
        ]),
        borderRadius: [4, 4, 0, 0],
      },
    }]
  })
}

const initRankChart = (data: any[]) => {
  if (!rankChartRef.value) return
  rankChart = echarts.init(rankChartRef.value)
  rankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: data.map(d => d.company).reverse(),
      axisLabel: { width: 100, overflow: 'truncate' },
    },
    series: [{
      data: data.map(d => d.count).reverse(),
      type: 'bar',
      itemStyle: { color: '#6366f1', borderRadius: [0, 4, 4, 0] },
    }]
  })
}

onMounted(() => {
  nextTick(() => loadData())
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
  barChart?.dispose()
  rankChart?.dispose()
})

const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
  barChart?.resize()
  rankChart?.resize()
}
</script>

<style scoped lang="scss">
.page-wrapper { padding: 4px; }
.page-header-modern { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header-title { font-size: 22px; font-weight: 700; color: var(--gray-800); margin: 0; }
.page-header-desc { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #ffffff; border-radius: var(--radius-lg); padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); border: 1px solid var(--border-light); }
.stat-icon { width: 48px; height: 48px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--gray-800); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--text-muted); margin-top: 2px; }

.charts-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.chart-card { border-radius: var(--radius-lg); }
.chart-header { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--gray-800); }
.chart-container { width: 100%; height: 300px; }

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
}
</style>
