<template>
  <div class="page-container">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon bg-primary">
          <el-icon :size="28"><Ticket /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ dailyData.total || 0 }}</p>
          <p class="stat-label">今日工单</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-blue">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ dailyData.pendingCount || 0 }}</p>
          <p class="stat-label">待处理</p>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon bg-warning">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ dailyData.overdueCount || 0 }}</p>
          <p class="stat-label">超时工单</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-success">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ dailyData.completedCount || 0 }}</p>
          <p class="stat-label">已完成</p>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="card chart-card">
        <div class="card-header">
          <span class="card-title">工单统计</span>
        </div>
        <div ref="chart1" class="chart-container"></div>
      </div>
      <div class="card chart-card">
        <div class="card-header">
          <span class="card-title">满意度分布</span>
        </div>
        <div ref="chart2" class="chart-container"></div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">团队工单概览</span>
      </div>
      <div v-if="teamData.length === 0" class="empty-state">
        <el-icon :size="48" class="empty-icon"><Users /></el-icon>
        <span class="empty-text">暂无团队数据</span>
      </div>
      <el-table v-else :data="teamData" border>
        <el-table-column prop="userName" label="姓名" />
        <el-table-column prop="totalCount" label="总工单数" />
        <el-table-column prop="pendingCount" label="待处理" />
        <el-table-column prop="completedCount" label="已完成" />
        <el-table-column prop="completionRate" label="完成率">
          <template #default="scope">
            <el-progress :percentage="scope.row.completionRate || 0" :width="100" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">工单数据导出</span>
        <el-button type="primary" class="btn-primary-gradient" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
      </div>
      <el-form :model="exportForm" inline>
        <el-form-item label="开始日期">
          <el-date-picker v-model="exportForm.startDate" type="date" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="exportForm.endDate" type="date" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Ticket, Clock, Warning, CircleCheck, User, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { reportsApi } from '@/api'
import { ElMessage } from 'element-plus'

const dailyData = reactive({})
const teamData = ref([])
const exportForm = reactive({
  startDate: '',
  endDate: ''
})

const chart1 = ref(null)
const chart2 = ref(null)
let chart1Instance = null
let chart2Instance = null

const initCharts = () => {
  if (chart1.value) {
    chart1Instance = echarts.init(chart1.value)
    chart1Instance.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255,255,255,0.95)',
        borderColor: '#ebf0f5',
        borderWidth: 1,
        textStyle: { color: '#303133' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        axisLine: { lineStyle: { color: '#ebf0f5' } },
        axisLabel: { color: '#909399' }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisLabel: { color: '#909399' }
      },
      series: [
        {
          name: '新建',
          type: 'bar',
          data: [120, 132, 101, 134, 190, 230, 210],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#FF7D3B' },
              { offset: 1, color: '#FFA940' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        },
        {
          name: '完成',
          type: 'bar',
          data: [80, 92, 81, 94, 140, 180, 160],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#25A4E9' },
              { offset: 1, color: '#60A5FA' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        }
      ]
    })
  }

  if (chart2.value) {
    chart2Instance = echarts.init(chart2.value)
    chart2Instance.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255,255,255,0.95)',
        borderColor: '#ebf0f5',
        borderWidth: 1,
        textStyle: { color: '#303133' }
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '50%'],
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 18, fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: [
            { value: 45, name: '5星', itemStyle: { color: '#FF7D3B' } },
            { value: 30, name: '4星', itemStyle: { color: '#25A4E9' } },
            { value: 15, name: '3星', itemStyle: { color: '#67C23A' } },
            { value: 8, name: '2星', itemStyle: { color: '#E6A23C' } },
            { value: 2, name: '1星', itemStyle: { color: '#F56C6C' } }
          ]
        }
      ]
    })
  }
}

const fetchData = async () => {
  try {
    const [daily, team] = await Promise.all([
      reportsApi.daily(),
      reportsApi.team()
    ])
    Object.assign(dailyData, daily)
    teamData.value = team
  } catch (error) {
    console.error('获取报表数据失败:', error)
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

const handleResize = () => {
  chart1Instance?.resize()
  chart2Instance?.resize()
}

onMounted(() => {
  fetchData()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart1Instance?.dispose()
  chart2Instance?.dispose()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, #FF7D3B, #25A4E9);
  }
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  &.warning::before {
    background: linear-gradient(180deg, #E6A23C, #F56C6C);
  }
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  
  &.bg-primary {
    background: linear-gradient(135deg, #FF7D3B, #FFA940);
  }
  
  &.bg-blue {
    background: linear-gradient(135deg, #25A4E9, #60A5FA);
  }
  
  &.bg-warning {
    background: linear-gradient(135deg, #E6A23C, #FBBF24);
  }
  
  &.bg-success {
    background: linear-gradient(135deg, #67C23A, #84CC16);
  }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  min-height: 350px;
}

.chart-container {
  height: 280px;
}
</style>