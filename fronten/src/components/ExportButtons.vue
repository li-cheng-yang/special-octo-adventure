<template>
  <div class="export-buttons">
    <el-dropdown @command="handleExport">
      <el-button type="primary" plain size="small">
        <el-icon><Download /></el-icon>
        导出数据
        <el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="checkins">
            <el-icon><Calendar /></el-icon> 导出签到记录 (Excel)
          </el-dropdown-item>
          <el-dropdown-item command="reports">
            <el-icon><Document /></el-icon> 导出汇报记录 (Excel)
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Download, ArrowDown, Calendar, Document } from '@element-plus/icons-vue'
import { exportApi } from '@/api'

const handleExport = async (command: string) => {
  try {
    ElMessage.info('正在生成文件，请稍候...')
    let blob: Blob
    let filename: string

    if (command === 'checkins') {
      blob = await exportApi.exportCheckins()
      filename = `签到记录_${new Date().toLocaleDateString()}.csv`
    } else if (command === 'reports') {
      blob = await exportApi.exportReports()
      filename = `汇报记录_${new Date().toLocaleDateString()}.csv`
    } else {
      return
    }

    // 下载文件
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('[Export] 导出失败:', error)
    ElMessage.error('导出失败')
  }
}
</script>

<style scoped lang="scss">
.export-buttons {
  display: inline-block;
}
</style>
