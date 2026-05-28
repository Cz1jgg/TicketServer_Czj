<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">我的待办工单</span>
      </div>

      <el-table 
        v-loading="loading"
        :data="tickets" 
        border
      >
        <el-table-column prop="ticketNo" label="工单号" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="typeName" label="类型" />
        <el-table-column prop="priority" label="优先级">
          <template #default="scope">
            <span :class="['priority-tag', `priority-${scope.row.priority}`]">
              {{ formatPriority(scope.row.priority) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <span :class="['status-tag', `status-${scope.row.status.toLowerCase()}`]">
              {{ formatStatus(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="requesterName" label="发起人" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="goToDetail(scope.row.id)">查看</el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              size="small" 
              type="primary" 
              @click="handleClaim(scope.row.id)"
            >
              认领
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        layout="prev, pager, next, jumper, ->, total"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ticketApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const tickets = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const formatStatus = (status) => {
  const statusMap = {
    PENDING: '待分配',
    PROCESSING: '处理中',
    SUSPENDED: '挂起',
    CONFIRMING: '待确认',
    TRANSFERRED: '已转交',
    CANCELLED: '已取消',
    CLOSED: '已关闭'
  }
  return statusMap[status] || status
}

const formatPriority = (priority) => {
  const priorityMap = {
    1: '低',
    2: '中',
    3: '高',
    6: '紧急',
    9: '严重'
  }
  return priorityMap[priority] || priority
}

const goToDetail = (id) => {
  router.push(`/tickets/${id}`)
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchTickets()
}

const fetchTickets = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      assigneeId: '',
      status: 'PENDING,PROCESSING,SUSPENDED'
    }
    const result = await ticketApi.list(params)
    tickets.value = result.content || []
    total.value = result.totalElements || 0
  } catch (error) {
    ElMessage.error('获取工单列表失败')
  } finally {
    loading.value = false
  }
}

const handleClaim = async (id) => {
  ElMessageBox.confirm('确定要认领此工单吗？', '提示').then(async () => {
    try {
      await ticketApi.claim(id)
      ElMessage.success('认领成功')
      fetchTickets()
    } catch (error) {
      if (error.message?.includes('并发')) {
        ElMessage.warning('该工单已被其他人认领')
      } else {
        ElMessage.error('认领失败: ' + error.message)
      }
    }
  })
}

onMounted(() => {
  fetchTickets()
})
</script>