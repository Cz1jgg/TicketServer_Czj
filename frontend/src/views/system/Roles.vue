<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">角色权限配置</span>
      </div>

      <el-table 
        v-loading="loading"
        :data="roles" 
        border
      >
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="roleName" label="角色名称" />
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
const roles = ref([])

const fetchRoles = async () => {
  loading.value = true
  try {
    roles.value = await adminApi.roles()
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRoles()
})
</script>