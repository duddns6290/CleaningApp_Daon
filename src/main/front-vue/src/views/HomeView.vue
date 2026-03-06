<template>
  <div class="page">
    <!-- Header -->
    <TheHeader>
      <template #right>
        <button class="btn btn--outline" @click="goChat">채팅</button>
        <button v-if="isLoggedIn" class="btn btn--outline" @click="onLogout">로그아웃</button>
        <button v-else class="btn btn--outline" @click="onLogin">로그인</button>
      </template>
    </TheHeader>

    <!-- Hero / Search -->
    <section class="hero container section">
      <h1 class="hero__title">
        <span class="location">{{ currentLocation }}</span>에서
        <span class="highlight">원룸 청소</span>
        <span>찾고 계신가요?</span>
      </h1>

      <!-- ... 나머지 기존 홈 내용 그대로 ... -->
    </section>

    <!-- Categories -->
    <section class="categories container section">
      <div class="grid">
        <button
            v-for="c in categories"
            :key="c.key"
            class="card"
            @click="openCategory(c.key)"
        >
          <div class="card__icon">{{ c.emoji }}</div>
          <div class="card__title">{{ c.title }}</div>
        </button>
      </div>
    </section>

    <!-- Neighborhood tags -->
    <section class="tags container section">
      <div class="taglist">
        <button v-for="t in neighborhoods" :key="t" class="pill">{{ t }}</button>
      </div>
    </section>

    <!-- Footer -->
    <TheFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { isAuthenticated, clearToken } from '@/api/auth'
import { useSocketStore } from '@/stores/socket'
import '../assets/HomePage.css'
import TheHeader from '@/components/layout/TheHeader.vue'
import TheFooter from '@/components/layout/TheFooter.vue'

const router = useRouter()
const socketStore = useSocketStore()

const isLoggedIn = ref(isAuthenticated())
onMounted(() => {
  isLoggedIn.value = isAuthenticated()
})

const currentLocation = ref('옥길동')
const showLocationMenu = ref(false)
const searchTab = ref<'market' | 'job' | 'estate'>('market')
const keyword = ref('')

const placeholders = {
  market: '검색어를 입력해주세요',
  job: '어떤 알바를 찾으세요?',
  estate: '원룸, 투룸, 오피스텔…'
} as const

const quickKeywords = ['냉장고 청소', '에어컨 청소', '베란다 청소', '화장실 청소']
const locations = ['옥길동', '역삼동', '청담동', '범내동', '신림동', '화도읍']

const categories = [
  { key: 'market', title: '10평이하', emoji: '🛍️' },
  { key: 'job', title: '10평 - 20평', emoji: '🔎' },
  { key: 'estate', title: '20평-30평', emoji: '🏠' },
  { key: 'car', title: '30평-40평', emoji: '🚗' },
  { key: 'biz', title: '40평이상', emoji: '🏪' }
]

const neighborhoods = [
  '옹달샘', '역삼동', '물금읍', '봉담읍', '배방읍', '서초동', '옥정동', '신림동', '불당동', '향남읍',
  '청담동', '다산동', '범내동', '화도읍', '다산읍', '마곡동', '압구정동', '배곧동'
]

function toggleLocationMenu () { showLocationMenu.value = !showLocationMenu.value }
function selectLocation (loc: string) { currentLocation.value = loc; showLocationMenu.value = false }
function onSearch () {
  const q = keyword.value || placeholders[searchTab.value]
  alert(`[검색] 탭: ${searchTab.value}, 키워드: ${q}`)
}
function openCategory (key: string) {
  alert(`[카테고리] ${key}`)
}
function onLogin () { router.push('/login') }
function onLogout () {
  clearToken()
  socketStore.disconnect()
  isLoggedIn.value = false
  router.replace('/')
}
function goChat () { router.push('/chat') }
</script>
