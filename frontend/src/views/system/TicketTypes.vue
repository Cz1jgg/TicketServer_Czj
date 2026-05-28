<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">工单类型配置</span>
      </div>

      <el-table 
        v-loading="loading"
        :data="types" 
        border
      >
        <el-table-column prop="typeName" label="类型名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const types = ref([])

const fetchTypes = async () => {
  loading.value = true
  try {
    types.value = await adminApi.roles()
  } catch (error) {
    ElMessage.error('获取工单类型失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTypes()
})
</script>