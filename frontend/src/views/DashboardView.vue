<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ArrowRight, ArrowUpRight } from "lucide-vue-next";
import BookingTable from "../components/BookingTable.vue";
import { request } from "../api";
import { user } from "../state";
import { hourText } from "../types";
import type { Booking, Dashboard } from "../types";

const admin = computed(() => user.value?.role === "ADMIN");
const bookings = ref<Booking[]>([]);
const dashboard = ref<Dashboard>();
const loading = ref(true);
const error = ref("");
const refreshError = ref("");
const now = ref(new Date());
let refreshVersion = 0;

const zone = "Asia/Shanghai";
const monthDay = new Intl.DateTimeFormat("zh-CN", {
  timeZone: zone,
  month: "long",
  day: "numeric",
});
const month = new Intl.DateTimeFormat("zh-CN", {
  timeZone: zone,
  month: "numeric",
});
const day = new Intl.DateTimeFormat("zh-CN", {
  timeZone: zone,
  day: "numeric",
});
const weekday = new Intl.DateTimeFormat("zh-CN", {
  timeZone: zone,
  weekday: "long",
});
const time = new Intl.DateTimeFormat("en-GB", {
  timeZone: zone,
  hour: "2-digit",
  minute: "2-digit",
});
const minute = 60_000;

function startsAt(booking: Booking) {
  return new Date(`${booking.date}T${hourText(booking.startHour)}:00+08:00`);
}

const schedule = computed(() =>
  bookings.value
    .map((booking) => ({ booking, start: startsAt(booking) }))
    .sort(
      (first, second) =>
        first.start.getTime() - second.start.getTime() ||
        first.booking.id - second.booking.id,
    ),
);
const next = computed(() =>
  schedule.value.find(
    ({ booking, start }) =>
      booking.status === "APPROVED" &&
      start.getTime() + booking.durationHours * 60 * minute >
        now.value.getTime(),
  ),
);
const pending = computed(() =>
  schedule.value.filter(
    ({ booking, start }) => booking.status === "PENDING" && start > now.value,
  ),
);
const recent = computed(() =>
  bookings.value
    .filter(({ status }) => status !== "PENDING" && status !== "APPROVED")
    .slice(0, 5),
);
const lastBookable = computed(
  () => new Date(now.value.getTime() + 14 * 24 * 60 * minute),
);

async function refresh(initial = false) {
  const version = ++refreshVersion;
  if (initial) {
    loading.value = true;
    error.value = "";
  }
  try {
    const data = admin.value
      ? await request<Dashboard>("/api/dashboard")
      : await request<Booking[]>("/api/bookings");
    if (version !== refreshVersion) return;
    if (Array.isArray(data)) bookings.value = data;
    else dashboard.value = data;
    now.value = new Date();
    refreshError.value = "";
  } catch (e) {
    if (version !== refreshVersion) return;
    if (initial) error.value = (e as Error).message;
    else refreshError.value = (e as Error).message;
  } finally {
    if (initial && version === refreshVersion) loading.value = false;
  }
}

function updated(booking: Booking) {
  if (admin.value && dashboard.value) {
    dashboard.value.recentBookings = dashboard.value.recentBookings.map(
      (item) => (item.id === booking.id ? booking : item),
    );
  } else {
    bookings.value = bookings.value.map((item) =>
      item.id === booking.id ? booking : item,
    );
  }
  void refresh();
}

onMounted(() => refresh(true));
</script>

<template>
  <div class="page-heading">
    <h1>工作台</h1>
    <RouterLink
      :to="admin ? '/bookings?status=PENDING' : '/venues'"
      class="button primary"
    >
      {{ admin ? "审核预约" : "预约场馆"
      }}<ArrowUpRight :size="17" aria-hidden="true" />
    </RouterLink>
  </div>
  <div v-if="error" class="error-state" role="alert">
    {{ error
    }}<button class="button secondary" @click="refresh(true)">重新加载</button>
  </div>
  <div v-else-if="loading" class="loading-state" role="status">
    正在加载预约…
  </div>
  <template v-else>
    <p v-if="refreshError" class="form-error" role="status">
      刷新预约失败。{{ refreshError }}
    </p>
    <BookingTable
      :bookings="admin ? (dashboard?.recentBookings ?? []) : recent"
      :show-actions="admin"
      @updated="updated"
      @refresh="refresh()"
    >
      <template #summary="{ busy, act }">
        <section
          v-if="admin && dashboard"
          class="workbench-admin"
          aria-labelledby="review-summary"
        >
          <h2 id="review-summary">预约处理</h2>
          <dl>
            <div>
              <dt>待审核</dt>
              <dd>{{ dashboard.pendingBookings }} 条</dd>
            </div>
            <div>
              <dt>今日有效预约</dt>
              <dd>{{ dashboard.todayBookings }} 条</dd>
            </div>
            <div>
              <dt>开放场馆</dt>
              <dd>{{ dashboard.openVenues }} / {{ dashboard.venueCount }}</dd>
            </div>
            <div>
              <dt>累计预约</dt>
              <dd>{{ dashboard.totalBookings }} 条</dd>
            </div>
          </dl>
        </section>
        <div v-else-if="!admin" class="workbench-grid">
          <div>
            <section aria-labelledby="next-booking">
              <h2 id="next-booking">下一场预约</h2>
              <div v-if="next" class="next-booking">
                <time class="next-date" :datetime="next.booking.date">
                  <span>{{ month.format(next.start) }}</span>
                  <strong>{{
                    day.format(next.start).replace("日", "")
                  }}</strong>
                  <span>{{ weekday.format(next.start) }}</span>
                </time>
                <div class="next-details">
                  <div class="next-time-row">
                    <strong class="next-time"
                      >{{ hourText(next.booking.startHour) }}–{{
                        hourText(
                          next.booking.startHour + next.booking.durationHours,
                        )
                      }}</strong
                    >
                    <span class="status-badge APPROVED">已通过</span>
                  </div>
                  <h3>{{ next.booking.venueName }}</h3>
                  <p>
                    {{ next.booking.purpose }} ·
                    {{ next.booking.participants }} 人
                  </p>
                  <div class="next-actions">
                    <button
                      v-if="next.booking.availableActions.includes('check-in')"
                      type="button"
                      class="button primary"
                      :disabled="busy !== null"
                      @click="act(next.booking, 'check-in')"
                    >
                      立即签到
                    </button>
                    <span v-else class="check-in-note">
                      {{
                        time.format(next.start.getTime() - 15 * minute)
                      }}
                      开始签到
                    </span>
                    <button
                      v-if="next.booking.availableActions.includes('cancel')"
                      type="button"
                      class="plain-action"
                      :disabled="busy !== null"
                      @click="act(next.booking, 'cancel')"
                    >
                      取消预约
                    </button>
                  </div>
                </div>
              </div>
              <p v-else class="workbench-empty">暂无已通过的预约。</p>
            </section>
            <section
              class="workbench-pending"
              aria-labelledby="pending-bookings"
            >
              <div class="workbench-section-title">
                <h2 id="pending-bookings">待审核</h2>
                <span class="muted">{{ pending.length }} 条</span>
              </div>
              <ul v-if="pending.length" class="pending-list">
                <li
                  v-for="{ booking, start } in pending.slice(0, 3)"
                  :key="booking.id"
                >
                  <div>
                    <strong>{{ booking.venueName }}</strong>
                    <p>
                      {{ monthDay.format(start) }}
                      {{ hourText(booking.startHour) }}–{{
                        hourText(booking.startHour + booking.durationHours)
                      }}
                    </p>
                  </div>
                  <span class="pending-expiry"
                    >{{ hourText(booking.startHour) }} 前未审核将过期</span
                  >
                  <span class="status-badge PENDING">待审核</span>
                </li>
              </ul>
              <p v-else class="workbench-empty">暂无待审核预约。</p>
              <RouterLink
                v-if="pending.length > 3"
                to="/bookings?status=PENDING"
                class="workbench-more"
              >
                查看全部待审核预约 <ArrowRight :size="16" aria-hidden="true" />
              </RouterLink>
            </section>
          </div>
          <aside class="workbench-rules" aria-labelledby="booking-rules">
            <h2 id="booking-rules">预约须知</h2>
            <dl>
              <div>
                <dt>开放预约日期</dt>
                <dd class="bookable-dates">
                  {{ monthDay.format(now) }} –
                  {{ monthDay.format(lastBookable) }}
                </dd>
              </div>
              <div>
                <dt>签到时间</dt>
                <dd>开始前 15 分钟至结束前</dd>
              </div>
              <div>
                <dt>取消预约</dt>
                <dd>预约开始前可取消</dd>
              </div>
            </dl>
          </aside>
        </div>
        <section
          class="workbench-history-heading"
          aria-labelledby="recent-bookings"
        >
          <h2 id="recent-bookings">{{ admin ? "最近预约" : "近期记录" }}</h2>
          <RouterLink to="/bookings" class="workbench-more">
            全部记录 <ArrowRight :size="16" aria-hidden="true" />
          </RouterLink>
        </section>
      </template>
    </BookingTable>
  </template>
</template>
