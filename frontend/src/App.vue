<script setup lang="ts">
import { CheckCircle2, CircleAlert } from 'lucide-vue-next'
import { toast } from './state'
</script>
<template>
  <RouterView />
  <Transition name="toast"
    ><div v-if="toast" class="toast" :class="{ error: toast.error }" role="status" aria-live="polite">
      <component :is="toast.error ? CircleAlert : CheckCircle2" :size="18" />{{ toast.message }}
    </div></Transition
  >
</template>
