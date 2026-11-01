import { createRouter, createWebHashHistory } from 'vue-router'
import { restoreSession, user } from './state'
import LoginView from './views/LoginView.vue'
import AppLayout from './components/AppLayout.vue'
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '',
          component: () => import('./views/DashboardView.vue'),
          meta: { title: '工作台' },
        },
        {
          path: 'venues',
          component: () => import('./views/VenuesView.vue'),
          meta: { title: '场馆预约' },
        },
        {
          path: 'bookings',
          component: () => import('./views/BookingsView.vue'),
          meta: { title: '预约记录' },
        },
        {
          path: 'manage',
          component: () => import('./views/ManageView.vue'),
          meta: { title: '场馆管理', admin: true },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})
router.beforeEach(async (to) => {
  await restoreSession()
  if (!user.value && to.path !== '/login') return '/login'
  if (user.value && to.path === '/login') return '/'
  if (to.meta.admin && user.value?.role !== 'ADMIN') return '/'
})
window.addEventListener('session-expired', () => {
  user.value = null
  void router.replace('/login')
})
router.afterEach((to) => {
  const title = to.path === '/venues' && user.value?.role === 'ADMIN' ? '场馆浏览' : to.meta.title || '登录'
  document.title = `${title} - 山东大学场馆预约管理系统`
})
export default router
