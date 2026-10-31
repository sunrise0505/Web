<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search, MapPin, Users, Clock3, ArrowUpRight, SlidersHorizontal } from 'lucide-vue-next'
import VenueArt from '../components/VenueArt.vue'
import BookingModal from '../components/BookingModal.vue'
import type { Venue } from '../types'
import { categories, hourText } from '../types'
import { request } from '../api'
import { user } from '../state'
const venues = ref<Venue[]>([]),
  query = ref(''),
  category = ref('全部场馆'),
  selected = ref<Venue | null>(null),
  loading = ref(true),
  error = ref(''),
  onlyOpen = ref(false),
  route = useRoute()
const filtered = computed(() =>
  venues.value.filter(
    (v) =>
      (category.value === '全部场馆' || v.category === category.value) &&
      (!onlyOpen.value || v.enabled) &&
      `${v.name}${v.location}${v.category}`.includes(query.value.trim()),
  ),
)
async function load() {
  loading.value = true
  error.value = ''
  try {
    venues.value = await request<Venue[]>('/api/venues')
    const id = Number(route.query.venue)
    selected.value =
      venues.value.find((v) => v.id === id && (v.enabled || user.value?.role === 'ADMIN')) ?? null
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>
<template>
  <div class="page-heading">
    <div>
      <h1>{{ user?.role === 'ADMIN' ? '场馆浏览' : '场馆预约' }}</h1>
    </div>
    <span class="venue-total">共 {{ venues.length }} 个场馆</span>
  </div>
  <div class="venue-filters">
    <div class="search-field">
      <Search :size="18" /><input v-model="query" aria-label="搜索场馆" placeholder="搜索场馆名称或位置…" />
    </div>
    <label class="filter-toggle"
      ><SlidersHorizontal :size="16" /><input v-model="onlyOpen" type="checkbox" />仅看开放场馆</label
    >
  </div>
  <div class="category-tabs" aria-label="按运动类型筛选">
    <button
      v-for="item in ['全部场馆', ...categories]"
      :key="item"
      :class="{ active: category === item }"
      @click="category = item"
    >
      {{ item }}</button
    ><span>{{ filtered.length }} 个结果</span>
  </div>
  <div v-if="error" class="error-state" role="alert">
    {{ error }}<button class="button secondary" @click="load">重新加载</button>
  </div>
  <div v-else-if="loading" class="loading-state">加载中…</div>
  <div v-else-if="!filtered.length" class="empty-state">
    <Search :size="30" /><strong>未找到匹配场馆</strong>
  </div>
  <div v-else class="venues-grid">
    <article v-for="v in filtered" :key="v.id" class="venue-card">
      <div class="venue-cover">
        <VenueArt :theme="v.theme" :category="v.category" /><span class="cover-category">{{
          v.category
        }}</span>
      </div>
      <div class="venue-card-body">
        <div class="venue-card-heading">
          <h2>{{ v.name }}</h2>
          <span class="availability" :class="{ closed: !v.enabled }"
            ><i></i>{{ v.enabled ? '开放预约' : '暂未开放' }}</span
          >
        </div>
        <p class="venue-location"><MapPin :size="14" />{{ v.location }}</p>
        <p class="card-description">{{ v.description }}</p>
        <div class="venue-meta">
          <span><Users :size="14" />{{ v.capacity }} 人以内</span
          ><span><Clock3 :size="14" />{{ hourText(v.openHour) }}–{{ hourText(v.closeHour) }}</span>
        </div>
        <button class="venue-reserve" :disabled="!v.enabled && user?.role !== 'ADMIN'" @click="selected = v">
          {{ user?.role === 'ADMIN' ? '查看时段' : v.enabled ? '选择时段' : '暂停预约'
          }}<ArrowUpRight :size="19" />
        </button>
      </div>
    </article>
  </div>
  <BookingModal v-if="selected" :venue="selected" @close="selected = null" />
</template>
