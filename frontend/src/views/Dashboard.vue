<template>
  <div class="page-container">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon bg-primary">
          <el-icon :size="28"><Ticket /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.total }}</p>
          <p class="stat-label">工单总量</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-blue">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.pending }}</p>
          <p class="stat-label">待处理工单</p>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon bg-warning">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.overdue }}</p>
          <p class="stat-label">超时工单</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-success">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.completed }}</p>
          <p class="stat-label">已完成工单</p>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="card chart-card">
        <div class="card-header">
          <span class="card-title">工单趋势</span>
        </div>
        <div ref="trendChart" class="chart-container"></div>
      </div>
      <div class="card chart-card">
        <div class="card-header">
          <span class="card-title">工单类型占比</span>
        </div>
        <div ref="pieChart" class="chart-container"></div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">SLA超时预警</span>
      </div>
      <div v-if="overdueTickets.length === 0" class="empty-state">
        <el-icon :size="48" class="empty-icon"><CircleCheck /></el-icon>
        <span class="empty-text">暂无超时工单</span>
      </div>
      <el-table v-else :data="overdueTickets" border>
        <el-table-column prop="ticketNo" label="工单号" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="typeName" label="类型" />
        <el-table-column prop="priority" label="优先级" :formatter="formatPriority" />
        <el-table-column prop="overdueTime" label="超时时间" />
        <el-table-column prop="assigneeName" label="处理人" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="goToDetail(scope.row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Ticket, Clock, Warning, CircleCheck } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { reportsApi, ticketApi } from '@/api'

const router = useRouter()

const stats = reactive({
  total: 0,
  pending: 0,
  overdue: 0,
  completed: 0
})

const overdueTickets = ref([])
const trendChart = ref(null)
const pieChart = ref(null)

let trendChartInstance = null
let pieChartInstance = null

const formatPriority = (row) => {
  const priorityMap = {
    1: '低',
    2: '中',
    3: '高',
    6: '紧急',
    9: '严重'
  }
  return priorityMap[row.priority] || row.priority
}

const goToDetail = (id) => {
  router.push(`/tickets/${id}`)
}

const initCharts = () => {
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    trendChartInstance.setOption({
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
          name: '新建工单',
          type: 'line',
          smooth: true,
          data: [120, 132, 101, 134, 190, 230, 210],
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(255, 125, 59, 0.3)' },
              { offset: 1, color: 'rgba(255, 125, 59, 0.05)' }
            ])
          },
          lineStyle: {
            color: '#FF7D3B',
            width: 3
          },
          itemStyle: { color: '#FF7D3B' }
        },
        {
          name: '已完成',
          type: 'line',
          smooth: true,
          data: [80, 92, 81, 94, 140, 180, 160],
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(37, 164, 233, 0.3)' },
              { offset: 1, color: 'rgba(37, 164, 233, 0.05)' }
            ])
          },
          lineStyle: {
            color: '#25A4E9',
            width: 3
          },
          itemStyle: { color: '#25A4E9' }
        }
      ]
    })
  }

  if (pieChart.value) {
    pieChartInstance = echarts.init(pieChart.value)
    pieChartInstance.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255,255,255,0.95)',
        borderColor: '#ebf0f5',
        borderWidth: 1,
        textStyle: { color: '#303133' }
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center',
        textStyle: { color: '#606266' }
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['40%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 18,
              fontWeight: 'bold'
            }
          },
          labelLine: { show: false },
          data: [
            { value: 35, name: '硬件故障', itemStyle: { color: '#FF7D3B' } },
            { value: 25, name: '软件问题', itemStyle: { color: '#25A4E9' } },
            { value: 20, name: '网络异常', itemStyle: { color: '#67C23A' } },
            { value: 15, name: '系统配置', itemStyle: { color: '#E6A23C' } },
            { value: 5, name: '其他', itemStyle: { color: '#909399' } }
          ]
        }
      ]
    })
  }
}

const fetchData = async () => {
  try {
    const [dailyData, evaluateData] = await Promise.all([
      reportsApi.daily(),
      reportsApi.evaluate()
    ])
    
    stats.total = dailyData.total || 0
    stats.pending = dailyData.pendingCount || 0
    stats.overdue = dailyData.overdueCount || 0
    stats.completed = dailyData.completedCount || 0

    const ticketList = await ticketApi.list({ status: 'PROCESSING', page: 0, size: 10 })
    overdueTickets.value = ticketList.content?.filter(t => t.isOverdue) || []
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const handleResize = () => {
  trendChartInstance?.resize()
  pieChartInstance?.resize()
}

onMounted(() => {
  fetchData()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance?.dispose()
  pieChartInstance?.dispose()
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

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>