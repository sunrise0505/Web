import { ref } from 'vue'
import { request, resetCsrf } from './api'
import type { User } from './types'
export const user = ref<User | null>(null)
let restored = false
export async function restoreSession() {
  if (!restored) {
    user.value = await request<User>('/auth/me').catch(() => null)
    restored = true
  }
}
export async function login(username: string, password: string) {
  await request('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ username, password }).toString(),
  })
  resetCsrf()
  user.value = await request<User>('/auth/me')
  restored = true
}
export async function logout() {
  await request('/auth/logout', { method: 'POST' })
  resetCsrf()
  user.value = null
  restored = true
}
export const toast = ref<{ message: string; error: boolean } | null>(null)
let timer: ReturnType<typeof setTimeout>
export function notify(message: string, error = false) {
  toast.value = { message, error }
  clearTimeout(timer)
  timer = setTimeout(() => {
    toast.value = null
  }, 4500)
}
