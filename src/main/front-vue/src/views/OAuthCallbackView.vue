<template>
  <div class="oauth-callback" v-if="errorMessage">
    <p class="oauth-callback__error">{{ errorMessage }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { setToken, setUser } from '@/api/auth'
import { decodeJwtPayload } from '@/utils/jwt'

const router = useRouter()
const errorMessage = ref('')

onMounted(() => {
  const hash = window.location.hash
  if (!hash) {
    errorMessage.value = '토큰이 없습니다.'
    return
  }

  const params = new URLSearchParams(hash.slice(1))
  const accessToken = params.get('accessToken')
  if (!accessToken) {
    errorMessage.value = 'accessToken을 찾을 수 없습니다.'
    return
  }

  setToken(accessToken)
  const payload = decodeJwtPayload(accessToken)
  if (payload?.sub) {
    setUser({
      userId: payload.sub,
      role: payload.role || 'CUSTOMER',
    })
  }

  router.replace('/chat')
})
</script>

<style scoped>
.oauth-callback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
  padding: 2rem;
  text-align: center;
}

.oauth-callback__error {
  color: #c00;
}
</style>
