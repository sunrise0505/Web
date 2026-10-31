<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, ArrowRight, ShieldCheck, UserRound } from 'lucide-vue-next'
import { login } from '../state'
const username = ref('admin'),
  password = ref('Admin123!'),
  busy = ref(false),
  error = ref(''),
  router = useRouter()
function choose(role: string) {
  username.value = role
  password.value = role === 'admin' ? 'Admin123!' : 'Student123!'
  error.value = ''
}
async function submit() {
  busy.value = true
  error.value = ''
  try {
    await login(username.value, password.value)
    await router.push('/')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <div class="login-page">
    <section class="login-form-side">
      <div class="login-form-wrap">
        <div class="login-logo"><Building2 :size="26" /></div>
        <h1>校园场馆预约管理系统</h1>
        <div class="login-role-switch">
          <button type="button" :class="{ selected: username === 'admin' }" @click="choose('admin')">
            <ShieldCheck :size="17" />管理员</button
          ><button type="button" :class="{ selected: username === 'student' }" @click="choose('student')">
            <UserRound :size="17" />学生
          </button>
        </div>
        <form @submit.prevent="submit">
          <label class="form-field"
            >账号<input
              v-model="username"
              name="username"
              autocomplete="username"
              required
              placeholder="请输入账号" /></label
          ><label class="form-field"
            >密码<input
              v-model="password"
              name="password"
              type="password"
              autocomplete="current-password"
              required
              placeholder="请输入密码"
          /></label>
          <p v-if="error" class="form-error" role="alert">{{ error }}</p>
          <button class="button primary login-submit" :disabled="busy">
            {{ busy ? '正在登录…' : '登录' }}<ArrowRight :size="18" />
          </button>
        </form>
      </div>
    </section>
  </div>
</template>
