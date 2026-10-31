<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, ArrowUpRight } from 'lucide-vue-next'
import BookingTable from '../components/BookingTable.vue'
import { request } from '../api'
import { user } from '../state'
import type { Booking, BookingStatus } from '../types'
import { statusLabels } from '../types'
const bookings = ref<Booking[]>([]),
  status = ref('ALL'),
  query = ref(''),
  loading = ref(true),
  error = ref('')
const route = useRoute()
watch(
  () => route.query.status,
  (value) => {
    status.value = typeof value === 'string' && value in statusLabels ? value : 'ALL'
  },
  { immediate: true },
)
const filtered = computed(() =>
  bookings.value.filter(
    (b) =>
      (status.value === 'ALL' || b.status === status.value) &&
      `${b.venueName}${b.purpose}${b.userName}`.includes(query.value.trim()),
  ),
)
const tabs = computed(() => [
  { value: 'ALL', label: '全部预约' },
  ...Object.entries(statusLabels).map(([value, label]) => ({ value, label })),
])
async function load() {
  loading.value = true
  error.value = ''
  try {
    bookings.value = await request<Booking[]>('/api/bookings')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}
onMounted(load)
function updated(booking: Booking) {
  bookings.value = bookings.value.map((b) => (b.id === booking.id ? booking : b))
}
async function refresh() {
  try {
    bookings.value = await request<Booking[]>('/api/bookings')
  } catch (e) {
    error.value = (e as Error).message
  }
}
</script>
<template>
  <div class="page-heading">
    <div>
      <h1>{{ user?.role === 'ADMIN' ? '预约记录' : '我的预约' }}</h1>
    </div>
    <RouterLink v-if="user?.role === 'STUDENT'" to="/venues" class="button primary"
      >新建预约<ArrowUpRight :size="17"
    /></RouterLink>
  </div>
  <section class="panel bookings-panel">
    <div class="bookings-toolbar">
      <div class="search-field">
        <Search :size="17" /><input
          v-model="query"
          aria-label="搜索预约"
          placeholder="搜索场馆、用途或预约人…"
        />
      </div>
      <span class="muted">共 {{ filtered.length }} 条记录</span>
    </div>
    <div class="booking-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="{ active: status === tab.value }"
        @click="status = tab.value"
      >
        {{ tab.label
        }}<span>{{
          tab.value === 'ALL'
            ? bookings.length
            : bookings.filter((b) => b.status === (tab.value as BookingStatus)).length
        }}</span>
      </button>
    </div>
    <div v-if="error" class="error-state" role="alert">
      {{ error }}<button class="button secondary" @click="load">重新加载</button>
    </div>
    <div v-else-if="loading" class="loading-state">正在加载预约…</div>
    <BookingTable v-else :bookings="filtered" @updated="updated" @refresh="refresh" />
  </section>
  <p class="page-note">预约以审核结果为准；可在时段开始前取消。签到开放时间为开始前 15 分钟至时段结束前。</p>
</template>
