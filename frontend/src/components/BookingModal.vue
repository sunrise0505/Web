<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { X, MapPin, Users, Clock3, Check, ArrowRight } from 'lucide-vue-next'
import VenueArt from './VenueArt.vue'
import type { Venue, Slot } from '../types'
import { hourText, localDate, bookingPurposes } from '../types'
import { request } from '../api'
import { notify, user } from '../state'
const canBook = computed(() => user.value?.role === 'STUDENT')
const props = defineProps<{ venue: Venue }>()
const emit = defineEmits<{ close: []; booked: [] }>()
const dialog = ref<HTMLDialogElement>(),
  date = ref(localDate()),
  hour = ref<number | null>(null),
  participants = ref(1),
  purpose = ref(''),
  durationHours = ref(1),
  slots = ref<Slot[]>([]),
  loading = ref(true),
  busy = ref(false),
  error = ref('')
const maxDate = computed(() => {
  const d = new Date(`${localDate()}T12:00:00+08:00`)
  d.setDate(d.getDate() + 14)
  return localDate(d)
})
const displayedSlots = computed(() =>
  slots.value.filter((s) => !canBook.value || s.hour + durationHours.value <= props.venue.closeHour),
)
let loadId = 0
async function loadSlots() {
  const id = ++loadId
  loading.value = true
  error.value = ''
  hour.value = null
  try {
    const result = await request<Slot[]>(`/api/venues/${props.venue.id}/slots?date=${date.value}`)
    if (id === loadId) slots.value = result
  } catch (e) {
    if (id === loadId) error.value = (e as Error).message
  } finally {
    if (id === loadId) loading.value = false
  }
}
watch(date, loadSlots)
watch(durationHours, () => {
  hour.value = null
})
function selectable(start: number) {
  return Array.from({ length: durationHours.value }, (_, index) => start + index).every((h) =>
    slots.value.some((s) => s.hour === h && s.status === 'AVAILABLE'),
  )
}
function label(s: Slot) {
  if (!canBook.value || s.status !== 'AVAILABLE') return slotLabel[s.status]
  return selectable(s.hour) ? '可预约' : '连续时段不足'
}
onMounted(() => {
  dialog.value?.showModal()
  void loadSlots()
})
async function submit() {
  if (!canBook.value || hour.value === null) return
  busy.value = true
  error.value = ''
  try {
    await request('/api/bookings', {
      method: 'POST',
      body: JSON.stringify({
        venueId: props.venue.id,
        date: date.value,
        startHour: hour.value,
        durationHours: durationHours.value,
        participants: participants.value,
        purpose: purpose.value,
      }),
    })
    notify('预约已提交，等待管理员审核')
    emit('booked')
    emit('close')
  } catch (e) {
    const message = (e as Error).message
    await loadSlots()
    error.value = message
  } finally {
    busy.value = false
  }
}
const slotLabel = { AVAILABLE: '可预约', BUSY: '已占用', EXPIRED: '已过时', CLOSED: '未开放' }
</script>
<template>
  <dialog
    ref="dialog"
    class="booking-dialog"
    @cancel.prevent="emit('close')"
    @click="
      (e) => {
        if (e.target === dialog) emit('close')
      }
    "
  >
    <div class="modal-header">
      <div>
        <h2>{{ canBook ? '预约场馆' : '场馆时段' }}</h2>
      </div>
      <button class="icon-button" aria-label="关闭预约窗口" @click="emit('close')"><X :size="21" /></button>
    </div>
    <div class="modal-venue">
      <div class="modal-art"><VenueArt :theme="venue.theme" :category="venue.category" /></div>
      <div>
        <h3>{{ venue.name }}</h3>
        <p><MapPin :size="13" />{{ venue.location }}</p>
        <p>
          <Users :size="13" />最多 {{ venue.capacity }} 人<span>·</span><Clock3 :size="13" />{{
            hourText(venue.openHour)
          }}–{{ hourText(venue.closeHour) }}
        </p>
      </div>
    </div>
    <p class="venue-description">{{ venue.description }}</p>
    <form @submit.prevent="submit">
      <label class="form-field"
        >选择日期<input v-model="date" type="date" :min="localDate()" :max="maxDate" required
      /></label>
      <label v-if="canBook" class="form-field"
        >预约时长
        <select v-model.number="durationHours" :disabled="busy">
          <option :value="1">1 小时</option>
          <option :value="2">2 小时</option>
        </select>
      </label>
      <div class="field-heading">
        <span>{{ canBook ? '选择时段' : '时段状态' }}</span
        ><span
          ><i class="legend-square"></i>{{ canBook ? `连续 ${durationHours} 小时` : '每格 1 小时' }}</span
        >
      </div>
      <div v-if="loading" class="inline-loading">正在查询可用时段…</div>
      <div v-else class="slots-grid">
        <button
          v-for="s in displayedSlots"
          :key="s.hour"
          type="button"
          class="slot"
          :class="{
            selected: hour === s.hour,
            readonly: !canBook,
            available: s.status === 'AVAILABLE' && (!canBook || selectable(s.hour)),
          }"
          :disabled="!canBook || !selectable(s.hour) || busy"
          :aria-pressed="hour === s.hour"
          @click="hour = s.hour"
        >
          <strong>{{ hourText(s.hour) }}–{{ hourText(s.hour + (canBook ? durationHours : 1)) }}</strong
          ><span><Check v-if="hour === s.hour" :size="12" />{{ label(s) }}</span>
        </button>
      </div>
      <div v-if="canBook" class="booking-form-row">
        <label class="form-field"
          >参与人数<input
            v-model.number="participants"
            type="number"
            min="1"
            :max="venue.capacity"
            required /></label
        ><label class="form-field"
          >预约用途<select v-model="purpose" required>
            <option disabled value="">请选择用途</option>
            <option v-for="item in bookingPurposes" :key="item" :value="item">{{ item }}</option>
          </select></label
        >
      </div>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <div v-if="canBook" class="booking-rules">
        可提前 14 天预约；待审核申请也会占用时段。<br />审核通过后，可在开始前 15 分钟至时段结束前签到。
      </div>
      <div class="modal-footer">
        <button type="button" class="button secondary" :disabled="busy" @click="emit('close')">
          {{ canBook ? '取消' : '关闭' }}</button
        ><button
          v-if="canBook"
          class="button primary"
          :disabled="hour === null || loading || busy || !venue.enabled"
        >
          {{ busy ? '正在提交…' : '确认预约' }}<ArrowRight :size="16" />
        </button>
      </div>
    </form>
  </dialog>
</template>
