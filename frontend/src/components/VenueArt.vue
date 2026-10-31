<script setup lang="ts">
withDefaults(defineProps<{ theme?: string; category?: string }>(), {
  theme: 'court',
  category: '羽毛球',
})
</script>
<template>
  <div class="venue-art sport-cover" :class="[theme, `sport-${category}`]" aria-hidden="true">
    <svg
      class="sport-symbol"
      viewBox="0 0 64 64"
      fill="none"
      stroke="currentColor"
      stroke-width="2.4"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      <g v-if="category === '羽毛球'">
        <path d="M19 35 10 51m4-9 8 5" />
        <ellipse cx="30" cy="24" rx="12" ry="16" transform="rotate(32 30 24)" />
        <path
          d="m24 14 14 9M20 20l15 10m-14-3 10 7m-3-20L20 28m16-12L25 34m25 6-4 12-9-3 8-10zM45 39l5-3m-9 7 8 3m-10 0 9 3"
        />
      </g>
      <g v-else-if="category === '篮球'">
        <circle cx="32" cy="32" r="22" />
        <path d="M10 32h44M32 10v44M16 17c12 8 12 22 0 30M48 17c-12 8-12 22 0 30" />
      </g>
      <g v-else-if="category === '游泳'">
        <circle cx="43" cy="17" r="5" />
        <path
          d="m15 29 11-11 12 9 10 10M26 18l-7-5m7 5 3 13M8 40q6-5 12 0t12 0 12 0 12 0M8 51q6-5 12 0t12 0 12 0 12 0"
        />
      </g>
      <g v-else-if="category === '健身'">
        <path d="M25 32h14M6 32h3m46 0h3" />
        <rect x="9" y="22" width="8" height="20" rx="2" />
        <rect x="17" y="16" width="8" height="32" rx="2" />
        <rect x="39" y="16" width="8" height="32" rx="2" />
        <rect x="47" y="22" width="8" height="20" rx="2" />
      </g>
      <g v-else-if="category === '足球'">
        <circle cx="32" cy="32" r="22" />
        <path
          d="m32 23 9 7-3 11H26l-3-11zM32 23V10M41 30l12-4M38 41l7 9M26 41l-7 9M23 30l-12-4M20 14l-5 9m34 0-5-9M13 40l10 5m18 0 10-5M26 53h12"
        />
      </g>
      <g v-else-if="category === '网球'">
        <ellipse cx="31" cy="23" rx="13" ry="17" transform="rotate(34 31 23)" />
        <path d="m22 36-12 17m4-10 8 5m4-34 15 10M21 20l16 11m-16-4 11 8m-3-22L20 28m17-13L25 35" />
        <circle cx="47" cy="46" r="8" />
        <path d="M40 42q9 0 11 10" />
      </g>
      <g v-else>
        <rect x="12" y="18" width="40" height="35" rx="3" />
        <path d="M22 18V10h20v8M24 28h4m8 0h4m-16 9h4m8 0h4M28 53V44h8v9" />
      </g>
    </svg>
  </div>
</template>
<style scoped>
.sport-cover {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  min-height: 0;
  background: #edf3f0;
  color: #42715d;
}
.sport-cover.pool {
  background: #ebf2f8;
  color: #487997;
}
.sport-cover.studio {
  background: #f3efe9;
  color: #8b7153;
}
.sport-cover.field {
  background: #eef2e8;
  color: #687e45;
}
.sport-symbol {
  display: block;
  width: 66px;
  height: 66px;
  stroke-width: 2;
}
:global(.quick-art .sport-symbol),
:global(.modal-art .sport-symbol) {
  width: 31px;
  height: 31px;
  stroke-width: 2.4;
}
</style>
