<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { LogOut } from "lucide-vue-next";
import { user, logout, notify } from "../state";
import { request } from "../api";

const route = useRoute();
const router = useRouter();
const demo = ref(false);
const links = computed(() => [
  { path: "/", label: "工作台" },
  {
    path: "/venues",
    label: user.value?.role === "ADMIN" ? "场馆浏览" : "场馆预约",
  },
  { path: "/bookings", label: "预约记录" },
  ...(user.value?.role === "ADMIN"
    ? [{ path: "/manage", label: "场馆管理" }]
    : []),
]);
const date = new Intl.DateTimeFormat("zh-CN", {
  timeZone: "Asia/Shanghai",
  month: "long",
  day: "numeric",
  weekday: "short",
}).format(new Date());

onMounted(async () => {
  try {
    demo.value = (await request<{ demo: boolean }>("/api/system")).demo;
  } catch (e) {
    notify((e as Error).message, true);
  }
});

async function signout() {
  try {
    await logout();
    await router.replace("/login");
  } catch (e) {
    notify((e as Error).message, true);
  }
}
</script>

<template>
  <header class="campus-header">
    <div class="campus-container campus-header-inner">
      <RouterLink to="/" class="campus-brand">
        <img src="/sdu-emblem.png" width="46" height="46" alt="" />
        <strong>山东大学</strong>
        <span>场馆预约系统</span>
      </RouterLink>
      <span v-if="demo" class="campus-demo">课程演示</span>
      <div class="campus-account">
        <time>{{ date }}</time>
        <span class="campus-avatar" aria-hidden="true">{{
          user?.displayName.slice(0, 1)
        }}</span>
        <span class="campus-user">{{ user?.displayName }}</span>
        <button
          type="button"
          class="campus-logout"
          aria-label="退出登录"
          @click="signout"
        >
          <LogOut :size="17" aria-hidden="true" /><span>退出</span>
        </button>
      </div>
    </div>
  </header>
  <nav class="campus-nav" aria-label="主导航">
    <div class="campus-container campus-nav-links">
      <RouterLink
        v-for="link in links"
        :key="link.path"
        :to="link.path"
        :aria-current="route.path === link.path ? 'page' : undefined"
        >{{ link.label }}</RouterLink
      >
    </div>
  </nav>
  <main class="campus-container campus-page"><RouterView /></main>
</template>
