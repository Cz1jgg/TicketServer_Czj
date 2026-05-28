import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: '/tickets',
        name: 'TicketList',
        component: () => import('@/views/ticket/List.vue')
      },
      {
        path: '/tickets/create',
        name: 'TicketCreate',
        component: () => import('@/views/ticket/Create.vue')
      },
      {
        path: '/tickets/:id',
        name: 'TicketDetail',
        component: () => import('@/views/ticket/Detail.vue')
      },
      {
        path: '/tickets/:id/edit',
        name: 'TicketEdit',
        component: () => import('@/views/ticket/Edit.vue')
      },
      {
        path: '/personal/todo',
        name: 'PersonalTodo',
        component: () => import('@/views/personal/Todo.vue')
      },
      {
        path: '/personal/created',
        name: 'PersonalCreated',
        component: () => import('@/views/personal/Created.vue')
      },
      {
        path: '/personal/processed',
        name: 'PersonalProcessed',
        component: () => import('@/views/personal/Processed.vue')
      },
      {
        path: '/system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/Users.vue')
      },
      {
        path: '/system/roles',
        name: 'SystemRoles',
        component: () => import('@/views/system/Roles.vue')
      },
      {
        path: '/system/ticket-types',
        name: 'SystemTicketTypes',
        component: () => import('@/views/system/TicketTypes.vue')
      },
      {
        path: '/system/sla-rules',
        name: 'SystemSlaRules',
        component: () => import('@/views/system/SlaRules.vue')
      },
      {
        path: '/system/assign-strategies',
        name: 'SystemAssignStrategies',
        component: () => import('@/views/system/AssignStrategies.vue')
      },
      {
        path: '/reports',
        name: 'Reports',
        component: () => import('@/views/reports/Index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const token = authStore.token
  
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router