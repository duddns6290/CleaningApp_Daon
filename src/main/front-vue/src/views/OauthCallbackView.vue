<!-- src/views/OauthCallbackView.vue -->
<template>
  <div class="callback-page">
    <p>소셜 로그인 처리 중입니다...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const code = route.query.code as string | undefined
  const provider = route.query.provider as string | undefined

  if (!code || !provider) {
    alert('잘못된 접근입니다.')
    router.push({ name: 'LoginPage' })
    return
  }

  try {
    // 1) 백엔드에 code + provider 보내서 토큰 + 유저정보 받아오기
    /*
    const res = await api.get('/api/auth/oauth/callback', {
      params: { code, provider },
    })

    const { isNewUser, email, accessToken } = res.data
    saveToken(accessToken)
    */

    // 예시용 MOCK
    const isNewUser = true
    const email = 'you@example.com'

    if (isNewUser) {
      // 추가정보 입력 페이지로 이동
      router.push({
        name: 'SignUp',
        query: {
          type: 'oauth',
          email,
          provider,
        },
      })
    } else {
      // 기존 회원이면 홈으로
      router.push({ name: 'home' })
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
