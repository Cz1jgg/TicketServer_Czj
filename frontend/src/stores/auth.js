import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')
  const roleCode = ref(localStorage.getItem('roleCode') || '')
  const roleName = ref(localStorage.getItem('roleName') || '')

  const login = (data) => {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    realName.value = data.realName
    roleCode.value = data.roleCode
    roleName.value = data.roleName
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    localStorage.setItem('realName', data.realName)
    localStorage.setItem('roleCode', data.roleCode)
    localStorage.setItem('roleName', data.roleName)
  }

  const logout = () => {
    token.value = ''
    userId.value = ''
    username.value = ''
    realName.value = ''
    roleCode.value = ''
    roleName.value = ''
    
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('roleCode')
    localStorage.removeItem('roleName')
  }

  const isAdmin = () => roleCode.value === 'ADMIN'
  const isSupervisor = () => roleCode.value === 'SUPERVISOR' || roleCode.value === 'ADMIN'
  const isItSupport = () => roleCode.value === 'IT_SUPPORT' || roleCode.value === 'ADMIN' || roleCode.value === 'SUPERVISOR'
  const isEmployee = () => roleCode.value === 'EMPLOYEE'

  return {
    token,
    userId,
    username,
    realName,
    roleCode,
    roleName,
    login,
    logout,
    isAdmin,
    isSupervisor,
    isItSupport,
    isEmployee
  }
})