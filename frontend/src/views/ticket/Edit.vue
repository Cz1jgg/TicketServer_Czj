<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">编辑工单</span>
        <div class="back-btn">
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" />
        </el-form-item>

        <el-form-item label="工单类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择工单类型">
            <el-option v-for="type in ticketTypes" :key="type.id" :label="type.typeName" :value="type.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="form.urgency">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="影响范围" prop="impactScope">
          <el-radio-group v-model="form.impactScope">
            <el-radio :label="1">个人</el-radio>
            <el-radio :label="2">部门</el-radio>
            <el-radio :label="3">全公司</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <div ref="editorRef" class="editor-container"></div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="btn-primary-gradient" :loading="loading" @click="handleSubmit">
            保存修改
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ticketApi, adminApi } from '@/api'
import { ElMessage } from 'element-plus'
import E from 'wangeditor'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const formRef = ref(null)
const editorRef = ref(null)
let editor = null

const form = reactive({
  title: '',
  typeId: '',
  urgency: 1,
  impactScope: 1,
  description: ''
})

const rules = {
  title: [
    { required: true, message: '请输入工单标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: '请选择工单类型', trigger: 'change' }
  ]
}

const ticketTypes = ref([])

const handleSubmit = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    form.description = editor.getText()
    loading.value = true
    try {
      await ticketApi.update(route.params.id, form)
      ElMessage.success('修改成功')
      router.push(`/tickets/${route.params.id}`)
    } catch (error) {
      ElMessage.error('修改失败: ' + error.message)
    } finally {
      loading.value = false
    }
  })
}

const goBack = () => {
  router.back()
}

const fetchTicketDetail = async () => {
  try {
    const data = await ticketApi.detail(route.params.id)
    form.title = data.title
    form.typeId = data.typeId
    form.urgency = data.urgency
    form.impactScope = data.impactScope
    form.description = data.description
    if (editor) {
      editor.txt.html(data.description)
    }
  } catch (error) {
    ElMessage.error('获取工单详情失败')
  }
}

const fetchTicketTypes = async () => {
  try {
    ticketTypes.value = await adminApi.roles()
  } catch (error) {
    console.error('获取工单类型失败')
  }
}

onMounted(() => {
  fetchTicketTypes()
  if (editorRef.value) {
    editor = new E(editorRef.value)
    editor.config.height = 200
    editor.create()
    fetchTicketDetail()
  }
})

onUnmounted(() => {
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.editor-container {
  border: 1px solid #ebf0f5;
  border-radius: 8px;
}

.back-btn {
  margin-left: auto;
}
</style>