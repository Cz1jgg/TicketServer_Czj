<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">工单列表</span>
        <el-button type="primary" class="btn-primary-gradient" @click="goToCreate">
          <el-icon><Plus /></el-icon>
          创建工单
        </el-button>
      </div>
      
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="工单号">
            <el-input v-model="searchForm.ticketNo" placeholder="请输入工单号" class="search-input" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题" class="search-input" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" class="search-input">
              <el-option label="全部" value="" />
              <el-option label="待分配" value="PENDING" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="挂起" value="SUSPENDED" />
              <el-option label="待确认" value="CONFIRMING" />
              <el-option label="已转交" value="TRANSFERRED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="searchForm.typeId" placeholder="请选择类型" class="search-input">
              <el-option label="全部" value="" />
              <el-option v-for="type in ticketTypes" :key="type.id" :label="type.typeName" :value="type.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-actions" v-if="authStore.isSupervisor()">
        <el-button type="success" @click="handleBatchAssign">批量分配</el-button>
        <el-button type="warning" @click="handleExport">导出Excel</el-button>
      </div>

      <el-table 
        v-loading="loading"
        :data="tickets" 
        border
        :row-key="(row) => row.id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
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
        <el-table-column prop="assigneeName" label="处理人" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="goToDetail(scope.row.id)">查看</el-button>
            <el-button 
              v-if="canClaim(scope.row)" 
              size="small" 
              type="primary" 
              @click="handleClaim(scope.row.id)"
            >
              认领
            </el-button>
            <el-button 
              v-if="canCancel(scope.row)" 
              size="small" 
              type="danger" 
              @click="handleCancel(scope.row.id)"
            >
              取消
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

    <el-dialog title="批量分配" :visible.sync="assignDialogVisible" width="400px">
      <el-form :model="assignForm">
        <el-form-item label="分配给">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择处理人">
            <el-option v-for="user in itSupportUsers" :key="user.id" :label="user.realName" :value="user.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchAssign">确定分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ticketApi, adminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const tickets = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  ticketNo: '',
  title: '',
  status: '',
  typeId: ''
})

const selectedTickets = ref([])
const assignDialogVisible = ref(false)
const assignForm = reactive({
  assigneeId: ''
})

const ticketTypes = ref([])
const itSupportUsers = ref([])

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

const canClaim = (ticket) => {
  return ticket.status === 'PENDING' && authStore.isItSupport()
}

const canCancel = (ticket) => {
  return ticket.status === 'PENDING' && ticket.requesterId === authStore.userId
}

const goToCreate = () => {
  router.push('/tickets/create')
}

const goToDetail = (id) => {
  router.push(`/tickets/${id}`)
}

const handleSearch = () => {
  currentPage.value = 1
  fetchTickets()
}

const resetSearch = () => {
  searchForm.ticketNo = ''
  searchForm.title = ''
  searchForm.status = ''
  searchForm.typeId = ''
  currentPage.value = 1
  fetchTickets()
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
      ticketNo: searchForm.ticketNo || undefined,
      title: searchForm.title || undefined,
      status: searchForm.status || undefined,
      typeId: searchForm.typeId || undefined
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
  ElMessageBox.confirm('确定要认领此工单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await ticketApi.claim(id)
      ElMessage.success('认领成功')
      fetchTickets()
    } catch (error) {
      if (error.message?.includes('并发')) {
        ElMessage.warning('该工单已被其他人认领，请刷新页面重试')
      } else {
        ElMessage.error('认领失败: ' + error.message)
      }
    }
  })
}

const handleCancel = async (id) => {
  ElMessageBox.confirm('确定要取消此工单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await ticketApi.cancel(id, { reason: '用户取消' })
      ElMessage.success('取消成功')
      fetchTickets()
    } catch (error) {
      ElMessage.error('取消失败: ' + error.message)
    }
  })
}

const handleSelectionChange = (val) => {
  selectedTickets.value = val
}

const handleBatchAssign = () => {
  if (selectedTickets.value.length === 0) {
    ElMessage.warning('请选择要分配的工单')
    return
  }
  assignDialogVisible.value = true
}

const confirmBatchAssign = async () => {
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择处理人')
    return
  }
  
  try {
    for (const ticket of selectedTickets.value) {
      await ticketApi.claim(ticket.id)
    }
    ElMessage.success('批量分配成功')
    assignDialogVisible.value = false
    selectedTickets.value = []
    fetchTickets()
  } catch (error) {
    ElMessage.error('批量分配失败')
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

const fetchTicketTypes = async () => {
  try {
    ticketTypes.value = await adminApi.roles()
  } catch (error) {
    console.error('获取工单类型失败')
  }
}

const fetchItSupportUsers = async () => {
  try {
    const users = await adminApi.users()
    itSupportUsers.value = users.filter(u => u.roleCode === 'IT_SUPPORT')
  } catch (error) {
    console.error('获取IT人员失败')
  }
}

onMounted(() => {
  fetchTickets()
  fetchTicketTypes()
  fetchItSupportUsers()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
}

.search-input {
  width: 180px;
}

.table-actions {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.el-table {
  --el-table-header-text-color: #606266;
}
</style>