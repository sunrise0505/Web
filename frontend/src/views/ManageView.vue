<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Pencil, X, Building2 } from 'lucide-vue-next'
import type { Venue } from '../types'
import { categories, hourText } from '../types'
import { request } from '../api'
import { notify } from '../state'
const venues = ref<Venue[]>([]),
  loading = ref(true),
  error = ref(''),
  dialog = ref<HTMLDialogElement>(),
  editingId = ref<number | null>(null),
  busy = ref(false),
  formError = ref('')
const blank = () => ({
  name: '',
  category: '羽毛球',
  location: '',
  description: '',
  capacity: 4,
  openHour: 8,
  closeHour: 22,
  enabled: true,
  theme: 'court' as Venue['theme'],
})
const form = ref(blank())
async function load() {
  error.value = ''
  loading.value = true
  try {
    venues.value = await request<Venue[]>('/api/venues')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}
onMounted(load)
function edit(v?: Venue) {
  editingId.value = v?.id ?? null
  form.value = v ? { ...v } : blank()
  formError.value = ''
  dialog.value?.showModal()
}
async function save() {
  busy.value = true
  formError.value = ''
  try {
    await request(`/api/admin/venues${editingId.value ? `/${editingId.value}` : ''}`, {
      method: editingId.value ? 'PUT' : 'POST',
      body: JSON.stringify(form.value),
    })
    dialog.value?.close()
    notify(editingId.value ? '场馆信息已更新' : '新场馆已添加')
    await load()
  } catch (e) {
    formError.value = (e as Error).message
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <div class="page-heading">
    <div>
      <h1>场馆管理</h1>
    </div>
    <button class="button primary" @click="edit()"><Plus :size="18" />新增场馆</button>
  </div>
  <section class="panel">
    <div class="section-heading">
      <div>
        <h2>场馆台账</h2>
        <p>共 {{ venues.length }} 处场馆</p>
      </div>
      <Building2 :size="22" class="muted" />
    </div>
    <div v-if="error" class="error-state" role="alert">
      {{ error }}<button class="button secondary" @click="load">重新加载</button>
    </div>
    <div v-else-if="loading" class="loading-state">正在加载场馆…</div>
    <div v-else class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>场馆 / 位置</th>
            <th>类型</th>
            <th>容量</th>
            <th>开放时间</th>
            <th>状态</th>
            <th class="align-right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="v in venues" :key="v.id">
            <td>
              <strong>{{ v.name }}</strong
              ><small>{{ v.location }}</small>
            </td>
            <td>{{ v.category }}</td>
            <td>{{ v.capacity }} 人</td>
            <td>{{ hourText(v.openHour) }}–{{ hourText(v.closeHour) }}</td>
            <td>
              <span class="status-badge" :class="v.enabled ? 'APPROVED' : 'CANCELLED'"
                ><i></i>{{ v.enabled ? '开放预约' : '暂停预约' }}</span
              >
            </td>
            <td class="align-right">
              <button class="edit-button" @click="edit(v)"><Pencil :size="14" />编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
  <p class="page-note">
    暂停开放将阻止新预约，已有预约保留，请在预约记录中处理。开放时间调整后，已有预约也会保留。
  </p>
  <dialog ref="dialog" class="venue-dialog">
    <div class="modal-header">
      <div>
        <h2>{{ editingId ? '编辑场馆' : '添加新场馆' }}</h2>
      </div>
      <button class="icon-button" aria-label="关闭编辑窗口" @click="dialog?.close()"><X :size="20" /></button>
    </div>
    <form @submit.prevent="save">
      <label class="form-field"
        >场馆名称<input v-model="form.name" required maxlength="60" placeholder="例如：东区羽毛球馆"
      /></label>
      <div class="form-two-columns">
        <label class="form-field"
          >运动类型<select v-model="form.category">
            <option v-for="c in categories" :key="c">{{ c }}</option>
          </select></label
        ><label class="form-field"
          >最多参与人数<input v-model.number="form.capacity" type="number" min="1" max="1000" required
        /></label>
      </div>
      <label class="form-field">场馆位置<input v-model="form.location" maxlength="100" required /></label
      ><label class="form-field"
        >场馆介绍与使用须知<textarea v-model="form.description" required maxlength="500" rows="3"></textarea>
      </label>
      <div class="form-two-columns">
        <label class="form-field"
          >开放时间<select v-model.number="form.openHour">
            <option v-for="h in 24" :key="h" :value="h - 1">{{ hourText(h - 1) }}</option>
          </select></label
        ><label class="form-field"
          >关闭时间<select v-model.number="form.closeHour">
            <option v-for="h in 24" :key="h" :value="h">{{ hourText(h) }}</option>
          </select></label
        >
      </div>
      <div class="form-two-columns">
        <label class="form-field"
          >图标配色<select v-model="form.theme">
            <option value="court">绿色</option>
            <option value="pool">蓝色</option>
            <option value="studio">棕色</option>
            <option value="field">草绿色</option>
          </select></label
        ><label class="form-field"
          >预约状态<select v-model="form.enabled">
            <option :value="true">开放预约</option>
            <option :value="false">暂停预约</option>
          </select></label
        >
      </div>
      <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
      <div class="modal-footer">
        <button type="button" class="button secondary" @click="dialog?.close()">取消</button
        ><button class="button primary" :disabled="busy">{{ busy ? '保存中…' : '保存场馆' }}</button>
      </div>
    </form>
  </dialog>
</template>
