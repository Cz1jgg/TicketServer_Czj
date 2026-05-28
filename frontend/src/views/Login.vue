<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand-section">
          <div class="brand-icon">
            <el-icon :size="48"><Ticket /></el-icon>
          </div>
          <h1 class="brand-title">企业IT服务工单管理系统</h1>
          <p class="brand-desc">高效管理，优质服务</p>
        </div>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon :size="24"><Lock /></el-icon>
            <span>安全可靠</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><Lightning /></el-icon>
            <span>快速响应</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><PieChart /></el-icon>
            <span>数据可视</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-wrapper">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-subtitle">请输入账号密码</p>
          <el-form ref="loginForm" :model="form" :rules="rules" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="用户名"
                prefix-icon="User"
                :disabled="loading"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                :disabled="loading"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                class="btn-primary-gradient login-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="tips">
            <span>测试账号：admin / admin</span>
            <span>itsupport1 / admin</span>
            <span>employee1 / admin</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Ticket, Lock, Lightning, PieChart, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在6-50个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  loading.value = true
  try {
    const data = await authApi.login(form)
    authStore.login(data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  overflow: hidden;
}

.login-left {
  width: 45%;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  color: white;
}

.brand-section {
  text-align: center;
  margin-bottom: 40px;
}

.brand-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #FF7D3B, #25A4E9);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 10px;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.login-right {
  width: 55%;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form-wrapper {
  width: 100%;
  max-width: 320px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
}

.login-form {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.tips {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.el-input__wrapper {
  border-radius: 10px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>