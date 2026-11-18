<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ArrowUpRight, Search } from "lucide-vue-next";
import BookingModal from "../components/BookingModal.vue";
import type { Venue } from "../types";
import { categories, hourText, campuses, venueLocation } from "../types";
import { request } from "../api";
import { user } from "../state";

const route = useRoute();
const admin = computed(() => user.value?.role === "ADMIN");
const venues = ref<Venue[]>([]);
const query = ref("");
const category = ref("全部场馆");
const campus = ref("全部校区");
const selected = ref<Venue | null>(null);
const loading = ref(true);
const error = ref("");
const onlyOpen = ref(false);
const filtered = computed(() =>
  venues.value.filter(
    (venue) =>
      (campus.value === "全部校区" || venue.campus === campus.value) &&
      (category.value === "全部场馆" || venue.category === category.value) &&
      (!onlyOpen.value || venue.enabled) &&
      `${venue.name}${venue.campus}${venue.location}${venue.category}`.includes(
        query.value.trim(),
      ),
  ),
);

function selectLinkedVenue() {
  const id = Number(route.query.venue);
  selected.value =
    venues.value.find(
      (venue) => venue.id === id && (venue.enabled || admin.value),
    ) ?? null;
}
watch(() => route.query.venue, selectLinkedVenue);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    venues.value = await request<Venue[]>("/api/venues");
    selectLinkedVenue();
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    loading.value = false;
  }
}
onMounted(load);
</script>

<template>
  <div class="page-heading directory-heading">
    <h1>{{ admin ? "场馆浏览" : "场馆预约" }}</h1>
    <label class="directory-search">
      <Search :size="19" aria-hidden="true" />
      <input
        v-model="query"
        type="search"
        aria-label="搜索场馆"
        placeholder="搜索场馆名称或位置"
      />
    </label>
  </div>
  <div class="directory">
    <aside class="directory-filters" aria-labelledby="directory-filters-title">
      <h2 id="directory-filters-title">筛选场馆</h2>
      <label class="directory-campus">
        校区
        <select v-model="campus" aria-label="筛选校区">
          <option>全部校区</option>
          <option v-for="item in campuses" :key="item">{{ item }}</option>
        </select>
      </label>
      <fieldset class="directory-sports">
        <legend>运动类型</legend>
        <label
          v-for="item in ['全部场馆', ...categories]"
          :key="item"
          :class="{ active: category === item }"
        >
          <input
            v-model="category"
            type="radio"
            name="venue-category"
            :value="item"
          />
          <span>{{ item }}</span>
        </label>
      </fieldset>
      <label class="directory-open-filter">
        <input v-model="onlyOpen" type="checkbox" />
        <span class="directory-switch" aria-hidden="true"></span>
        仅看开放场馆
      </label>
    </aside>

    <section
      class="directory-results"
      aria-labelledby="directory-results-title"
    >
      <div class="directory-results-title">
        <h2 id="directory-results-title">{{ category }}</h2>
        <span v-if="!loading && !error" class="muted" role="status"
          >{{ filtered.length }} 个场馆</span
        >
      </div>
      <div v-if="error" class="error-state" role="alert">
        {{ error
        }}<button class="button secondary" @click="load">重新加载</button>
      </div>
      <div v-else-if="loading" class="loading-state" role="status">
        正在加载场馆…
      </div>
      <div v-else-if="!filtered.length" class="empty-state">未找到匹配场馆</div>
      <div v-else>
        <div class="directory-row directory-columns" aria-hidden="true">
          <span>场馆</span><span>开放时段</span><span>容量</span
          ><span>状态</span>
        </div>
        <ul class="directory-list">
          <li v-for="venue in filtered" :key="venue.id" class="directory-row">
            <div class="directory-name">
              <strong>{{ venue.name }}</strong>
              <span>{{ venueLocation(venue) }}</span>
            </div>
            <span class="directory-hours"
              >{{ hourText(venue.openHour) }}–{{
                hourText(venue.closeHour)
              }}</span
            >
            <span class="directory-capacity">{{ venue.capacity }} 人以内</span>
            <span
              class="directory-status"
              :class="{ 'is-open': venue.enabled }"
            >
              {{ venue.enabled ? "开放预约" : "暂未开放" }}
            </span>
            <button
              type="button"
              class="directory-action"
              :disabled="!venue.enabled && !admin"
              @click="selected = venue"
            >
              {{ admin ? "查看时段" : venue.enabled ? "选择时段" : "暂停预约" }}
              <ArrowUpRight
                v-if="venue.enabled || admin"
                :size="15"
                aria-hidden="true"
              />
            </button>
          </li>
        </ul>
      </div>
    </section>
  </div>
  <BookingModal v-if="selected" :venue="selected" @close="selected = null" />
</template>
