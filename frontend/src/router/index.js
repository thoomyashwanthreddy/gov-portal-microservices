import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'requests',
    component: () => import('@/views/RequestListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/submit',
    name: 'submit-request',
    component: () => import('@/views/SubmitRequestView.vue'),
    meta: { requiresAuth: true, requiresRole: 'citizen' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    return true
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresRole === 'citizen' && !authStore.isCitizen) {
    return { name: 'requests' }
  }

  return true
})

export default router
