<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">工单详情</span>
        <div class="ticket-header-info">
          <span class="ticket-no">{{ ticket.ticketNo }}</span>
          <span :class="['status-tag', `status-${ticket.status?.toLowerCase()}`]">
            {{ formatStatus(ticket.status) }}
          </span>
          <span :class="['priority-tag', `priority-${ticket.priority}`]">
            {{ formatPriority(ticket.priority) }}
          </span>
        </div>
      </div>

      <div class="detail-content">
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>标题</label>
              <span>{{ ticket.title }}</span>
            </div>
            <div class="info-item">
              <label>类型</label>
              <span>{{ ticket.typeName }}</span>
            </div>
            <div class="info-item">
              <label>发起人</label>
              <span>{{ ticket.requesterName }}</span>
            </div>
            <div class="info-item">
              <label>处理人</label>
              <span>{{ ticket.assigneeName || '-' }}</span>
            </div>
            <div class="info-item">
              <label>紧急程度</label>
              <span>{{ formatUrgency(ticket.urgency) }}</span>
            </div>
            <div class="info-item">
              <label>影响范围</label>
              <span>{{ formatImpactScope(ticket.impactScope) }}</span>
            </div>
            <div class="info-item">
              <label>创建时间</label>
              <span>{{ ticket.createTime }}</span>
            </div>
            <div class="info-item">
              <label>更新时间</label>
              <span>{{ ticket.updateTime }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">问题描述</h3>
          <div class="description-content" v-html="ticket.description"></div>
        </div>

        <div class="detail-section" v-if="ticket.attachments?.length > 0">
          <h3 class="section-title">附件</h3>
          <div class="attachment-list">
            <div v-for="att in ticket.attachments" :key="att.id" class="attachment-item">
              <el-icon :size="18"><Document /></el-icon>
              <span>{{ att.fileName }}</span>
              <el-button size="small" @click="downloadAttachment(att)">下载</el-button>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">状态流转</h3>
          <div v-if="processes.length === 0" class="empty-state">
            <span class="empty-text">暂无状态流转记录</span>
          </div>
          <div v-else class="process-timeline">
            <div v-for="(item, index) in processes" :key="item.id" class="process-item">
              <div class="process-dot" :class="{ active: index === 0 }"></div>
              <div class="process-info">
                <div class="process-header">
                  <span class="process-status">{{ formatStatus(item.toStatus) }}</span>
                  <span class="process-time">{{ item.createTime }}</span>
                </div>
                <p class="process-operator">{{ item.operatorName }}操作</p>
                <p class="process-reason" v-if="item.reason">{{ item.reason }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">审计日志</h3>
          <div v-if="logs.length === 0" class="empty-state">
            <span class="empty-text">暂无审计日志</span>
          </div>
          <el-table v-else :data="logs" border>
            <el-table-column prop="createTime" label="时间" />
            <el-table-column prop="operatorName" label="操作人" />
            <el-table-column prop="action" label="操作" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </div>

        <div v-if="ticket.status === 'CONFIRMING' && !ticket.evaluate" class="evaluate-section">
          <h3 class="section-title">满意度评价</h3>
          <div class="evaluate-form">
            <div class="star-rating">
              <span 
                v-for="star in 5" 
                :key="star" 
                class="star"
                :class="{ active: star <= evaluateForm.score }"
                @click="evaluateForm.score = star"
              >★</span>
            </div>
            <el-input 
              v-model="evaluateForm.comment" 
              type="textarea" 
              placeholder="请输入评价内容（选填）"
              rows="3"
            />
            <el-button type="primary" class="btn-primary-gradient" @click="handleEvaluate">
              提交评价
            </el-button>
          </div>
        </div>
      </div>

      <div class="action-bar">
        <el-button v-if="canClaim" @click="handleClaim">认领工单</el-button>
        <el-button v-if="canProcess" type="primary" @click="showProcessModal = true">处理工单</el-button>
        <el-button v-if="canSuspend" type="warning" @click="showSuspendModal = true">挂起工单</el-button>
        <el-button v-if="canResume" type="success" @click="showResumeModal = true">恢复工单</el-button>
        <el-button v-if="canComplete" type="primary" @click="showCompleteModal = true">完成处理</el-button>
        <el-button v-if="canReject" type="danger" @click="showRejectModal = true">驳回工单</el-button>
        <el-button v-if="canCancel" type="danger" @click="showCancelModal = true">取消工单</el-button>
        <el-button v-if="canClose" type="success" @click="handleClose">确认关闭</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <el-dialog title="处理工单" :visible.sync="showProcessModal" width="500px">
      <el-form :model="processForm">
        <el-form-item label="处理内容">
          <el-textarea v-model="processForm.content" rows="5" placeholder="请输入处理内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProcessModal = false">取消</el-button>
        <el-button type="primary" @click="confirmProcess">确认处理</el-button>
      </template>
    </el-dialog>

    <el-dialog title="挂起工单" :visible.sync="showSuspendModal" width="500px">
      <el-form :model="suspendForm">
        <el-form-item label="挂起原因">
          <el-textarea v-model="suspendForm.reason" rows="3" placeholder="请输入挂起原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSuspendModal = false">取消</el-button>
        <el-button type="primary" @click="confirmSuspend">确认挂起</el-button>
      </template>
    </el-dialog>

    <el-dialog title="恢复工单" :visible.sync="showResumeModal" width="500px">
      <el-form :model="resumeForm">
        <el-form-item label="恢复说明">
          <el-textarea v-model="resumeForm.reason" rows="3" placeholder="请输入恢复说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResumeModal = false">取消</el-button>
        <el-button type="primary" @click="confirmResume">确认恢复</el-button>
      </template>
    </el-dialog>

    <el-dialog title="完成处理" :visible.sync="showCompleteModal" width="500px">
      <el-form :model="completeForm">
        <el-form-item label="处理结果">
          <el-textarea v-model="completeForm.content" rows="5" placeholder="请输入处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCompleteModal = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确认完成</el-button>
      </template>
    </el-dialog>

    <el-dialog title="驳回工单" :visible.sync="showRejectModal" width="500px">
      <el-form :model="rejectForm">
        <el-form-item label="驳回原因">
          <el-textarea v-model="rejectForm.reason" rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRejectModal = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog title="取消工单" :visible.sync="showCancelModal" width="500px">
      <el-form :model="cancelForm">
        <el-form-item label="取消原因">
          <el-textarea v-model="cancelForm.reason" rows="3" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCancelModal = false">取消</el-button>
        <el-button type="danger" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ticketApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const ticket = ref({})
const processes = ref([])
const logs = ref([])

const showProcessModal = ref(false)
const showSuspendModal = ref(false)
const showResumeModal = ref(false)
const showCompleteModal = ref(false)
const showRejectModal = ref(false)
const showCancelModal = ref(false)

const processForm = reactive({ content: '' })
const suspendForm = reactive({ reason: '' })
const resumeForm = reactive({ reason: '' })
const completeForm = reactive({ content: '' })
const rejectForm = reactive({ reason: '' })
const cancelForm = reactive({ reason: '' })

const evaluateForm = reactive({
  score: 0,
  comment: ''
})

const canClaim = computed(() => {
  return ticket.value.status === 'PENDING' && authStore.isItSupport()
})

const canProcess = computed(() => {
  return ticket.value.status === 'PROCESSING' && 
         authStore.isItSupport() &&
         ticket.value.assigneeId === authStore.userId
})

const canSuspend = computed(() => {
  return ticket.value.status === 'PROCESSING' && 
         authStore.isItSupport() &&
         ticket.value.assigneeId === authStore.userId
})

const canResume = computed(() => {
  return ticket.value.status === 'SUSPENDED' && 
         authStore.isItSupport() &&
         ticket.value.assigneeId === authStore.userId
})

const canComplete = computed(() => {
  return ticket.value.status === 'PROCESSING' && 
         authStore.isItSupport() &&
         ticket.value.assigneeId === authStore.userId
})

const canReject = computed(() => {
  return ticket.value.status === 'CONFIRMING' && 
         ticket.value.requesterId === authStore.userId
})

const canCancel = computed(() => {
  return ticket.value.status === 'PENDING' && 
         ticket.value.requesterId === authStore.userId
})

const canClose = computed(() => {
  return ticket.value.status === 'CONFIRMING' && 
         ticket.value.requesterId === authStore.userId
})

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

const formatUrgency = (urgency) => {
  const map = { 1: '低', 2: '中', 3: '高' }
  return map[urgency] || urgency
}

const formatImpactScope = (scope) => {
  const map = { 1: '个人', 2: '部门', 3: '全公司' }
  return map[scope] || scope
}

const fetchTicketDetail = async () => {
  try {
    const id = route.params.id
    ticket.value = await ticketApi.detail(id)
    processes.value = ticket.value.processes || []
    logs.value = await ticketApi.logs(id)
  } catch (error) {
    ElMessage.error('获取工单详情失败')
  }
}

const goBack = () => {
  router.back()
}

const downloadAttachment = (att) => {
  window.open(att.filePath, '_blank')
}

const handleClaim = async () => {
  ElMessageBox.confirm('确定要认领此工单吗？', '提示').then(async () => {
    try {
      await ticketApi.claim(route.params.id)
      ElMessage.success('认领成功')
      fetchTicketDetail()
    } catch (error) {
      if (error.message?.includes('并发')) {
        ElMessage.warning('该工单已被其他人认领')
      } else {
        ElMessage.error('认领失败: ' + error.message)
      }
    }
  })
}

const confirmProcess = async () => {
  if (!processForm.content.trim()) {
    ElMessage.warning('请输入处理内容')
    return
  }
  try {
    await ticketApi.complete(route.params.id, { content: processForm.content })
    ElMessage.success('处理成功')
    showProcessModal.value = false
    processForm.content = ''
    fetchTicketDetail()
  } catch (error) {
    ElMessage.error('处理失败: ' + error.message)
  }
}

const confirmSuspend = async () => {
  if (!suspendForm.reason.trim()) {
    ElMessage.warning('请输入挂起原因')
    return
  }
  try {
    await ticketApi.suspend(route.params.id, { reason: suspendForm.reason })
    ElMessage.success('挂起成功')
    showSuspendModal.value = false
    suspendForm.reason = ''
    fetchTicketDetail()
  } catch (error) {
    ElMessage.error('挂起失败: ' + error.message)
  }
}

const confirmResume = async () => {
  try {
    await ticketApi.resume(route.params.id, { reason: resumeForm.reason || '用户补充信息完成' })
    ElMessage.success('恢复成功')
    showResumeModal.value = false
    resumeForm.reason = ''
    fetchTicketDetail()
  } catch (error) {
    ElMessage.error('恢复失败: ' + error.message)
  }
}

const confirmComplete = async () => {
  if (!completeForm.content.trim()) {
    ElMessage.warning('请输入处理结果')
    return
  }
  try {
    await ticketApi.complete(route.params.id, { content: completeForm.content })
    ElMessage.success('完成处理成功')
    showCompleteModal.value = false
    completeForm.content = ''
    fetchTicketDetail()
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await ticketApi.reject(route.params.id, { reason: rejectForm.reason })
    ElMessage.success('驳回成功')
    showRejectModal.value = false
    rejectForm.reason = ''
    fetchTicketDetail()
  } catch (error) {
    ElMessage.error('驳回失败: ' + error.message)
  }
}

const confirmCancel = async () => {
  if (!cancelForm.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  try {
    await ticketApi.cancel(route.params.id, { reason: cancelForm.reason })
    ElMessage.success('取消成功')
    showCancelModal.value = false
    cancelForm.reason = ''
    fetchTicketDetail()
  } catch (error) {
    ElMessage.error('取消失败: ' + error.message)
  }
}

const handleClose = async () => {
  ElMessageBox.confirm('确定要关闭此工单吗？', '提示').then(async () => {
    try {
      await ticketApi.close(route.params.id)
      ElMessage.success('关闭成功')
      fetchTicketDetail()
    } catch (error) {
      ElMessage.error('关闭失败: ' + error.message)
    }
  })
}

const handleEvaluate = async () => {
  if (evaluateForm.score === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  try {
    await ticketApi.evaluate(route.params.id, {
      score: evaluateForm.score,
      comment: evaluateForm.comment
    })
    ElMessage.success('评价成功')
    evaluateForm.score = 0
    evaluateForm.comment = ''
    fetchTicketDetail()
  } catch (error) {
    ElMessage.error('评价失败: ' + error.message)
  }
}

onMounted(() => {
  fetchTicketDetail()
})
</script>

<style scoped>
.ticket-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ticket-no {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.detail-content {
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebf0f5;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.info-item {
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
  
  label {
    font-size: 12px;
    color: #909399;
    display: block;
    margin-bottom: 4px;
  }
  
  span {
    font-size: 14px;
    color: #303133;
  }
}

.description-content {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  min-height: 100px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.process-timeline {
  padding-left: 20px;
  border-left: 2px solid #ebf0f5;
}

.process-item {
  position: relative;
  padding-bottom: 24px;
  
  &:last-child {
    padding-bottom: 0;
  }
}

.process-dot {
  position: absolute;
  left: -29px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d9d9d9;
  
  &.active {
    background: linear-gradient(135deg, #FF7D3B, #25A4E9);
  }
}

.process-info {
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
}

.process-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.process-status {
  font-weight: 600;
  color: #303133;
}

.process-time {
  font-size: 12px;
  color: #909399;
}

.process-operator {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.process-reason {
  font-size: 12px;
  color: #909399;
  background: white;
  padding: 8px;
  border-radius: 4px;
}

.evaluate-section {
  background: linear-gradient(135deg, rgba(255, 125, 59, 0.05), rgba(37, 164, 233, 0.05));
  padding: 20px;
  border-radius: 12px;
  margin-top: 24px;
}

.evaluate-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.star-rating {
  display: flex;
  gap: 8px;
  
  .star {
    font-size: 32px;
    color: #d9d9d9;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &.active {
      color: #FFD700;
    }
    
    &:hover {
      transform: scale(1.1);
    }
  }
}

.action-bar {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #ebf0f5;
}

@media (max-width: 1200px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>