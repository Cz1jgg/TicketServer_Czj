<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="card-title">用户管理</span>
        <el-button type="primary" class="btn-primary-gradient" @click="showCreateModal = true">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>

      <el-table 
        v-loading="loading"
        :data="users" 
        border
      >
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="roleName" label="角色" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <el-dialog title="添加用户" :visible.sync="showCreateModal" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" placeholder="请选择角色">
            <el-option label="普通员工" value="EMPLOYEE" />
            <el-option label="IT支持人员" value="IT_SUPPORT" />
            <el-option label="服务台主管" value="SUPERVISOR" />
            <el-option label="系统管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="编辑用户" :visible.sync="showEditModal" width="450px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="editForm.roleCode" placeholder="请选择角色">
            <el-option label="普通员工" value="EMPLOYEE" />
            <el-option label="IT支持人员" value="IT_SUPPORT" />
            <el-option label="服务台主管" value="SUPERVISOR" />
            <el-option label="系统管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const showCreateModal = ref(false)
const showEditModal = ref(false)

const formRef = ref(null)
const editFormRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleCode: ''
})

const editForm = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  roleCode: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const editRules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await adminApi.users()
    total.value = users.value.length
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleCreate = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await adminApi.createUser(form)
      ElMessage.success('创建成功')
      showCreateModal.value = false
      form.username = ''
      form.password = ''
      form.realName = ''
      form.email = ''
      form.phone = ''
      form.roleCode = ''
      fetchUsers()
    } catch (error) {
      ElMessage.error('创建失败: ' + error.message)
    }
  })
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.username = row.username
  editForm.realName = row.realName
  editForm.email = row.email
  editForm.phone = row.phone
  editForm.roleCode = row.roleCode
  showEditModal.value = true
}

const handleUpdate = async () => {
  if (!editFormRef.value) return
  editFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await adminApi.updateUser(editForm.id, editForm)
      ElMessage.success('更新成功')
      showEditModal.value = false
      fetchUsers()
    } catch (error) {
      ElMessage.error('更新失败: ' + error.message)
    }
  })
}

const handleDelete = async (id) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await adminApi.deleteUser(id)
      ElMessage.success('删除成功')
      fetchUsers()
    } catch (error) {
      ElMessage.error('删除失败: ' + error.message)
    }
  })
}

onMounted(() => {
  fetchUsers()
})
</script>