<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  ArrowUpRight,
  ArrowRight,
  Building2,
  CalendarCheck2,
  Clock3,
  Activity,
  MapPin,
} from 'lucide-vue-next'
import { venueLocation } from '../types'
import VenueArt from '../components/VenueArt.vue'
import BookingTable from '../components/BookingTable.vue'
import { request } from '../api'
import { user } from '../state'
import type { Dashboard, Venue, Booking } from '../types'
const data = ref<Dashboard>(),
  venues = ref<Venue[]>([]),
  loading = ref(true),
  error = ref('')
async function load() {
  loading.value = true
  error.value = ''
  try {
    ;[data.value, venues.value] = await Promise.all([
      request<Dashboard>('/api/dashboard'),
      request<Venue[]>('/api/venues'),
    ])
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}
let refreshVersion = 0
async function refresh() {
  const version = ++refreshVersion
  try {
    const result = await request<Dashboard>('/api/dashboard')
    if (version === refreshVersion) data.value = result
  } catch (e) {
    if (version === refreshVersion) error.value = (e as Error).message
  }
}
function updated(booking: Booking) {
  if (data.value)
    data.value.recentBookings = data.value.recentBookings.map((b) => (b.id === booking.id ? booking : b))
  void refresh()
}
onMounted(load)
const max = computed(() => Math.max(1, ...(data.value?.trend.map((d) => d.count) || [1])))
const stats = computed(() => [
  {
    title: '开放场馆',
    value: data.value?.openVenues ?? 0,
    unit: '处',
    sub: `共 ${data.value?.venueCount ?? 0} 个场馆`,
    icon: Building2,
    color: 'green',
  },
  {
    title: user.value?.role === 'ADMIN' ? '今日有效预约' : '我的今日预约',
    value: data.value?.todayBookings ?? 0,
    unit: '单',
    sub: '待审核、已通过与已签到',
    icon: CalendarCheck2,
    color: 'blue',
  },
  {
    title: '待审核预约',
    value: data.value?.pendingBookings ?? 0,
    unit: '单',
    sub: user.value?.role === 'ADMIN' ? '待处理申请' : '等待管理员审核',
    icon: Clock3,
    color: 'orange',
  },
  {
    title: '累计预约',
    value: data.value?.totalBookings ?? 0,
    unit: '单',
    sub: user.value?.role === 'ADMIN' ? '全部预约记录' : '我的全部预约记录',
    icon: Activity,
    color: 'purple',
  },
])
</script>
<template>
  <div class="page-heading">
    <div>
      <h1>工作台</h1>
    </div>
    <RouterLink :to="user?.role === 'ADMIN' ? '/bookings?status=PENDING' : '/venues'" class="button primary"
      >{{ user?.role === 'ADMIN' ? '审核预约' : '预约场馆' }}<ArrowUpRight :size="17"
    /></RouterLink>
  </div>
  <div v-if="error" class="error-state" role="alert">
    {{ error }}<button class="button secondary" @click="load">重新加载</button>
  </div>
  <div v-else-if="loading" class="loading-state">加载中…</div>
  <template v-else-if="data">
    <section class="stats-grid" aria-label="预约概况">
      <div v-for="s in stats" :key="s.title" class="stat-card">
        <div class="stat-top">
          <span>{{ s.title }}</span
          ><span class="stat-icon" :class="s.color"><component :is="s.icon" :size="18" /></span>
        </div>
        <div class="stat-value">
          {{ s.value }}<span>{{ s.unit }}</span>
        </div>
        <p>{{ s.sub }}</p>
      </div>
    </section>
    <div class="dashboard-middle">
      <section class="panel trend-panel">
        <div class="section-heading">
          <div>
            <h2>预约统计</h2>
            <p>{{ user?.role === 'ADMIN' ? '近 7 天全校有效预约' : '近 7 天我的有效预约' }}</p>
          </div>
          <span class="chip">近 7 天</span>
        </div>
        <div class="chart">
          <div v-for="point in data.trend" :key="point.date" class="chart-column">
            <span class="chart-number">{{ point.count }}</span>
            <div class="bar-track">
              <div class="bar" :style="{ height: `${(point.count / max) * 100}%` }"></div>
            </div>
            <span class="chart-label">{{ point.date.slice(5).replace('-', '/') }}</span>
          </div>
        </div>
        <div class="chart-foot"><span class="online-dot"></span>按使用日期统计 · 不含已取消及未通过预约</div>
      </section>
      <section class="panel quick-panel">
        <div class="section-heading">
          <div>
            <h2>开放场馆</h2>
          </div>
          <RouterLink to="/venues" class="icon-button" aria-label="查看全部场馆"
            ><ArrowUpRight :size="20"
          /></RouterLink>
        </div>
        <RouterLink
          v-for="v in venues.filter((v) => v.enabled).slice(0, 3)"
          :key="v.id"
          :to="`/venues?venue=${v.id}`"
          class="quick-venue"
          ><div class="quick-art"><VenueArt :theme="v.theme" :category="v.category" /></div>
          <div class="quick-info">
            <strong>{{ v.name }}</strong
            ><span><MapPin :size="12" />{{ venueLocation(v) }}</span>
          </div>
          <ArrowUpRight :size="17" class="muted" /></RouterLink
        ><RouterLink to="/venues" class="quick-all"
          >全部场馆（{{ data.venueCount }}）<ArrowRight :size="15"
        /></RouterLink>
      </section>
    </div>
    <section class="panel recent-panel">
      <div class="section-heading">
        <div>
          <h2>最近预约</h2>
        </div>
        <RouterLink to="/bookings" class="text-link">全部记录<ArrowRight :size="15" /></RouterLink>
      </div>
      <BookingTable :bookings="data.recentBookings" compact @updated="updated" @refresh="refresh" />
    </section>
  </template>
</template>
