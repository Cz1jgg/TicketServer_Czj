import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else if (res.code === 401) {
      handleTokenExpired()
      return Promise.reject(new Error(res.msg || '未授权'))
    } else {
      ElMessage({
        message: res.msg || '请求失败',
        type: 'error',
        duration: 3000
      })
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      handleTokenExpired()
    } else {
      ElMessage({
        message: error.message || '网络异常',
        type: 'error',
        duration: 3000
      })
    }
    return Promise.reject(error)
  }
)

const handleTokenExpired = () => {
  ElMessageBox.confirm(
    '登录已过期，请重新登录',
    '提示',
    {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const authStore = useAuthStore()
    authStore.logout()
    window.location.href = '/login'
  }).catch(() => {
    const authStore = useAuthStore()
    authStore.logout()
    window.location.href = '/login'
  })
}

export default request