<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LayoutDashboard, Compass, CalendarDays, Building2, LogOut, Menu, X } from 'lucide-vue-next'
import { user, logout, notify } from '../state'
import { request } from '../api'
const route = useRoute(),
  router = useRouter(),
  menuOpen = ref(false),
  demo = ref(false)
const links = computed(() => [
  { path: '/', title: '工作台', icon: LayoutDashboard },
  { path: '/venues', title: user.value?.role === 'ADMIN' ? '场馆浏览' : '场馆预约', icon: Compass },
  { path: '/bookings', title: '预约记录', icon: CalendarDays },
  { path: '/manage', title: '场馆管理', icon: Building2 },
])
const date = new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
}).format(new Date())
onMounted(async () => {
  try {
    demo.value = (await request<{ demo: boolean }>('/api/system')).demo
  } catch (e) {
    notify((e as Error).message, true)
  }
})
async function signout() {
  try {
    await logout()
    await router.replace('/login')
  } catch (e) {
    notify((e as Error).message, true)
  }
}
</script>
<template>
  <div class="app-shell">
    <div v-if="menuOpen" class="sidebar-scrim" @click="menuOpen = false"></div>
    <aside class="sidebar" :class="{ shown: menuOpen }">
      <RouterLink to="/" class="brand"
        ><span class="brand-icon"><Building2 :size="23" /></span><span>场馆预约</span></RouterLink
      >
      <button class="mobile-close icon-button" aria-label="关闭菜单" @click="menuOpen = false">
        <X :size="20" />
      </button>
      <nav class="main-nav" aria-label="主导航">
        <template v-for="link in links" :key="link.path"
          ><RouterLink
            v-if="link.path !== '/manage' || user?.role === 'ADMIN'"
            :to="link.path"
            :class="{ active: route.path === link.path }"
            @click="menuOpen = false"
            ><component :is="link.icon" :size="20" /><span>{{ link.title }}</span></RouterLink
          ></template
        >
      </nav>
      <div class="sidebar-bottom">
        <div class="sidebar-foot">
          <span class="online-dot"></span>山东大学场馆预约管理系统<span>v0.1</span>
        </div>
      </div>
    </aside>
    <div class="main-shell">
      <header class="topbar">
        <div class="breadcrumb">
          <button class="mobile-toggle icon-button" aria-label="打开菜单" @click="menuOpen = true">
            <Menu :size="21" /></button
          ><span>山东大学</span><span class="slash">/</span
          ><strong>{{
            route.path === '/venues' && user?.role === 'ADMIN' ? '场馆浏览' : route.meta.title
          }}</strong
          ><span v-if="demo" class="demo-badge">本地演示</span>
        </div>
        <div class="topbar-right">
          <span class="topbar-date">{{ date }}</span
          ><span class="header-divider"></span>
          <div class="account">
            <span class="avatar">{{ user?.displayName.slice(0, 1) }}</span>
            <div>
              {{ user?.displayName }}<small>{{ user?.role === 'ADMIN' ? '管理员' : '学生' }}</small>
            </div>
          </div>
          <button
            class="icon-button logout"
            title="退出登录 / 切换账号"
            aria-label="退出登录或切换账号"
            @click="signout"
          >
            <LogOut :size="18" />
          </button>
        </div>
      </header>
      <main class="page"><RouterView /></main>
    </div>
  </div>
</template>
