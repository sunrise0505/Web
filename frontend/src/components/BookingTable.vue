<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { CalendarDays, Check, X, Ban, LogIn, CircleAlert } from 'lucide-vue-next'
import type { Booking, BookingAction } from '../types'
import { hourText, statusLabels } from '../types'
import { user, notify } from '../state'
import { request } from '../api'
withDefaults(defineProps<{ bookings: Booking[]; compact?: boolean }>(), { compact: false })
const emit = defineEmits<{ updated: [booking: Booking]; refresh: [] }>()
const busy = ref<number | null>(null)
const confirmDialog = ref<HTMLDialogElement>()
const pendingAction = ref<{ booking: Booking; action: 'cancel' | 'reject' } | null>(null)
const refreshTimer = setInterval(() => {
  if (!document.hidden && busy.value === null && !pendingAction.value) {
    emit('refresh')
  }
}, 30_000)
onUnmounted(() => clearInterval(refreshTimer))
const allowed = (b: Booking, action: BookingAction) => b.availableActions?.includes(action) ?? false
function act(booking: Booking, action: BookingAction) {
  if (busy.value !== null) return
  if (action === 'cancel' || action === 'reject') {
    pendingAction.value = { booking, action }
    confirmDialog.value?.showModal()
  } else void perform(booking, action)
}
async function perform(booking: Booking, action: BookingAction) {
  busy.value = booking.id
  try {
    const result = await request<Booking>(`/api/bookings/${booking.id}/${action}`, { method: 'POST' })
    emit('updated', result)
    notify(
      { approve: '预约已通过', reject: '预约已拒绝', cancel: '预约已取消', 'check-in': '签到成功' }[action],
    )
  } catch (e) {
    notify((e as Error).message, true)
    emit('refresh')
  } finally {
    busy.value = null
  }
}
function confirm() {
  const pending = pendingAction.value
  confirmDialog.value?.close()
  pendingAction.value = null
  if (pending) void perform(pending.booking, pending.action)
}
</script>
<template>
  <div v-if="!bookings.length" class="empty-state">
    <CalendarDays :size="32" /><strong>暂无预约记录</strong>
    <RouterLink v-if="user?.role === 'STUDENT'" to="/venues" class="text-link">预约场馆</RouterLink>
  </div>
  <div v-else class="table-scroll">
    <table class="data-table booking-table" :aria-busy="busy !== null">
      <thead>
        <tr>
          <th>预约场馆</th>
          <th>预约时间</th>
          <th v-if="user?.role === 'ADMIN'">预约人</th>
          <th>状态</th>
          <th class="align-right">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="b in bookings" :key="b.id">
          <td class="booking-name">
            <strong>{{ b.venueName }}</strong
            ><small>{{ compact ? b.location : b.purpose }}</small>
          </td>
          <td class="booking-time">
            <span>{{ b.date }}</span
            ><small
              >{{ hourText(b.startHour) }} – {{ hourText(b.startHour + b.durationHours) }} ·
              {{ b.participants }} 人</small
            >
          </td>
          <td v-if="user?.role === 'ADMIN'" class="booking-person">{{ b.userName }}</td>
          <td class="booking-status">
            <span class="status-badge" :class="b.status"><i></i>{{ statusLabels[b.status] }}</span>
          </td>
          <td class="align-right booking-actions">
            <div class="row-actions">
              <button v-if="allowed(b, 'approve')" :disabled="busy !== null" @click="act(b, 'approve')">
                <Check :size="14" />通过
              </button>
              <button
                v-if="allowed(b, 'reject')"
                class="danger"
                :disabled="busy !== null"
                @click="act(b, 'reject')"
              >
                <X :size="14" />拒绝
              </button>
              <button
                v-if="b.status === 'APPROVED' && user?.role === 'STUDENT'"
                :disabled="busy !== null || !allowed(b, 'check-in')"
                :title="allowed(b, 'check-in') ? '签到' : '签到时间：开始前 15 分钟至时段结束前'"
                @click="act(b, 'check-in')"
              >
                <LogIn :size="14" />签到
              </button>
              <button
                v-if="allowed(b, 'cancel')"
                class="subtle"
                :disabled="busy !== null"
                @click="act(b, 'cancel')"
              >
                <Ban :size="13" />取消
              </button>
              <span
                v-if="!b.availableActions?.length && !(b.status === 'APPROVED' && user?.role === 'STUDENT')"
                class="muted"
                >—</span
              >
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <dialog ref="confirmDialog" class="confirm-dialog" @close="pendingAction = null">
    <div class="modal-header">
      <CircleAlert :size="25" /><button
        class="icon-button"
        aria-label="关闭确认"
        @click="confirmDialog?.close()"
      >
        <X :size="19" />
      </button>
    </div>
    <h2>确认{{ pendingAction?.action === 'cancel' ? '取消' : '拒绝' }}该预约？</h2>
    <p class="confirm-booking-name">{{ pendingAction?.booking.venueName }}</p>
    <p v-if="pendingAction" class="muted">
      {{ pendingAction.booking.date }} {{ hourText(pendingAction.booking.startHour) }}–{{
        hourText(pendingAction.booking.startHour + pendingAction.booking.durationHours)
      }}
      · {{ pendingAction.booking.userName }}
    </p>
    <p class="muted">操作后将释放该时段。</p>
    <div class="modal-footer">
      <button class="button secondary" @click="confirmDialog?.close()">返回</button
      ><button class="button primary" @click="confirm">
        确认{{ pendingAction?.action === 'cancel' ? '取消' : '拒绝' }}
      </button>
    </div>
  </dialog>
</template>
