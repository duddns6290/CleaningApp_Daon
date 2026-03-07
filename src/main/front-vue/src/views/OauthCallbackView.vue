<!-- src/views/OauthCallbackView.vue -->
<template>
  <div class="callback-page">
    <p>소셜 로그인 처리 중입니다...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { setToken, setUser } from '@/api/auth'
import { decodeJwtPayload } from '@/utils/jwt'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  const hash = window.location.hash
  const accessTokenMatch = hash?.match(/#accessToken=([^&]+)/)
  if (accessTokenMatch?.[1]) {
    const accessToken = accessTokenMatch[1]
    setToken(accessToken)
    const payload = decodeJwtPayload(accessToken)
    if (payload?.sub) {
      setUser({ userId: payload.sub, role: payload.role ?? 'CUSTOMER' })
    }
    router.replace('/')
    return
  }

  const code = route.query.code as string | undefined
  const provider = route.query.provider as string | undefined

  if (!code || !provider) {
    alert('잘못된 접근입니다.')
    router.push({ name: 'LoginPage' })
    return
  }

  try {
    const isNewUser = true
    const email = 'you@example.com'

    if (isNewUser) {
      router.push({
        name: 'SignUp',
        query: { type: 'oauth', email, provider },
      })
    } else {
      router.replace('/')
    }
  } catch (error) {
    console.error(error)
    alert('소셜 로그인 처리 중 오류가 발생했습니다.')
    router.push({ name: 'LoginPage' })
  }
})
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
}
</style>
