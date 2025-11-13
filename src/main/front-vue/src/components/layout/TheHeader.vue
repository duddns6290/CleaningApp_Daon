<!-- src/components/layout/TheHeader.vue -->
<template>
  <header class="header">
    <div class="header__left">
      <img
          class="logo"
          :src="logoSrc"
          alt="DAON 로고"
          @click="goHome"
          style="cursor: pointer;"
      />
    </div>
    <div class="header__right">
      <slot name="right" />
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps<{
  logoSrc?: string
}>()

const logoSrc = computed(() => props.logoSrc ?? '/images/logo.png')

function goHome() {
  router.push('/')
}
</script>
