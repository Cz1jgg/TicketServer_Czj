<template>
  <div class="layout-container">
    <aside class="sidebar" :class="{ 'collapsed': sidebarCollapsed }">
      <div class="logo">
        <span class="logo-text" v-if="!sidebarCollapsed">IT工单系统</span>
        <span class="logo-icon" v-else>IT</span>
      </div>
      <nav class="menu">
        <el-menu :default-active="activeMenu" mode="vertical" class="el-menu-vertical">
          <template v-for="item in menuItems" :key="item.path">
            <el-menu-item
              v-if="!item.children && hasPermission(item.permission)"
              :index="item.path"
              @click="handleMenuClick(item)"
            >
              <el-icon :size="18" class="menu-icon">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.label }}</span>
            </el-menu-item>
            <el-sub-menu v-else-if="hasPermission(item.permission)" :index="item.path">
              <template #title>
                <el-icon :size="18" class="menu-icon">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.label }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
                @click="handleMenuClick(child)"
              >
                {{ child.label }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </nav>
    </aside>
    <div class="main-content">
      <header class="header">
        <div class="header-left">
          <button class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
            <el-icon :size="20"><Menu /></el-icon>
          </button>
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <span class="user-info">
            <el-icon :size="18"><User /></el-icon>
            {{ authStore.realName }}
            <span class="role-tag">{{ authStore.roleName }}</span>
          </span>
          <button class="logout-btn" @click="handleLogout">
            <el-icon :size="18"><TurnOff /></el-icon>
            退出登录
          </button>
        </div>
      </header>
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>import { ref, computed, markRaw } from 'vue';
import { Menu, User, TurnOff, HomeFilled, Ticket, Setting, PieChart, FolderOpened, Clock, CircleCheck } from '@element-plus/icons-vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const sidebarCollapsed = ref(false);
const menuItems = computed(() => [
 {
 path: '/dashboard',
 label: '数据看板',
 icon: markRaw(HomeFilled),
 permission: ['EMPLOYEE', 'IT_SUPPORT', 'SUPERVISOR', 'ADMIN']
 },
 {
 path: '/tickets',
 label: '工单管理',
 icon: markRaw(Ticket),
 permission: ['EMPLOYEE', 'IT_SUPPORT', 'SUPERVISOR', 'ADMIN']
 },
 {
 path: '/personal',
 label: '个人中心',
 icon: markRaw(User),
 permission: ['EMPLOYEE', 'IT_SUPPORT', 'SUPERVISOR', 'ADMIN'],
 children: [
 { path: '/personal/todo', label: '我的待办' },
 { path: '/personal/created', label: '我发起的' },
 { path: '/personal/processed', label: '我处理的' }
 ]
 },
 {
 path: '/system',
 label: '系统配置',
 icon: markRaw(Setting),
 permission: ['SUPERVISOR', 'ADMIN'],
 children: [
 { path: '/system/users', label: '用户管理' },
 { path: '/system/roles', label: '角色权限' },
 { path: '/system/ticket-types', label: '工单类型' },
 { path: '/system/sla-rules', label: 'SLA规则' },
 { path: '/system/assign-strategies', label: '分配策略' }
 ]
 },
 {
 path: '/reports',
 label: '报表中心',
 icon: markRaw(PieChart),
 permission: ['SUPERVISOR', 'ADMIN']
 }
]);
const activeMenu = computed(() => {
 const path = route.path;
 const found = menuItems.value.find(item => {
 if (item.children) {
 return item.children.some(child => child.path === path);
 }
 return item.path === path;
 });
 return found ? found.path : path;
});
const currentPageTitle = computed(() => {
 const found = menuItems.value.find(item => {
 if (item.children) {
 return item.children.some(child => child.path === route.path);
 }
 return item.path === route.path;
 });
 if (found) {
 if (found.children) {
 const child = found.children.find(c => c.path === route.path);
 return child ? child.label : found.label;
 }
 return found.label;
 }
 return '工单系统';
});
const hasPermission = (permissions) => {
 if (!permissions || permissions.length === 0)
 return true;
 return permissions.includes(authStore.roleCode);
};
const handleMenuClick = (item) => {
 if (item.path) {
 router.push(item.path);
 }
};
const handleLogout = () => {
 authStore.logout();
 router.push('/login');
};
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  transition: width 0.3s ease;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed {
  width: 60px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF7D3B, #25A4E9);
}

.logo-text {
  transition: opacity 0.3s ease;
}

.logo-icon {
  font-size: 20px;
}

.sidebar.collapsed .logo-text {
  display: none;
}

.sidebar:not(.collapsed) .logo-icon {
  display: none;
}

.menu {
  flex: 1;
  padding-top: 20px;
}

.menu-icon {
  margin-right: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

.el-menu-vertical {
  background: transparent;
  border-right: none;
}
/* 穿透所有层级的菜单文字，强制变成纯白色 */
/* 1. 所有一级菜单文字 */
.el-menu-item {
  color: #ffffff !important;
  opacity: 1 !important;
}

/* 2. 所有二级/三级父菜单标题（个人中心、系统配置） */
.el-submenu__title {
  color: #ffffff !important;
  opacity: 1 !important;
}

/* 3. 嵌套在子菜单里的所有子项（用户管理、角色权限等） */
.el-submenu .el-menu-item {
  color: #14a148 !important;
  opacity: 1 !important;
}

/* 4. 专门针对你说的“个人中心/系统配置下的栏目”，再补一层穿透 */
.el-menu--dark .el-submenu .el-menu-item,
.el-menu--dark .el-submenu__title {
  color: #ffffff !important;
  opacity: 1 !important;
}
.el-menu-item,
.el-sub-menu__title {
  color: #ffffff !important;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 125, 59, 0.2) !important;
    color: #ffffff !important;
  }
}

.el-menu-item.is-active,
.el-sub-menu.is-active > .el-sub-menu__title {
  background: linear-gradient(135deg, rgba(255, 125, 59, 0.4), rgba(37, 164, 233, 0.4)) !important;
  color: #ffffff !important;
}

.el-sub-menu .el-menu-item,
.el-sub-menu .el-sub-menu__title {
  color: #ffffff !important;
  background: transparent !important;
  
  &:hover {
    background: rgba(255, 125, 59, 0.2) !important;
    color: #ffffff !important;
  }
  
  &.is-active {
    background: rgba(255, 125, 59, 0.3) !important;
    color: #ffffff !important;
  }
}

:deep(.el-menu--inline .el-menu-item),
:deep(.el-menu--inline .el-sub-menu__title),
:deep(.el-menu-item-group__title) {
  color: #af5031 !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #ffffff !important;
}

:deep(.el-sub-menu .el-menu .el-button) {
  color: #006400 !important;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 125, 59, 0.1);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF7D3B;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 125, 59, 0.2);
    transform: rotate(90deg);
  }
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255, 125, 59, 0.08);
  border-radius: 20px;
  font-size: 14px;
}

.role-tag {
  padding: 2px 8px;
  background: linear-gradient(135deg, #FF7D3B, #25A4E9);
  color: white;
  border-radius: 10px;
  font-size: 12px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border: none;
  background: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(245, 108, 108, 0.2);
  }
}

.content {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
}
</style>