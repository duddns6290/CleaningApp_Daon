<template>
  <div class="page">
    <!-- Header -->
    <header class="header">
      <div class="header__left">
        <img class="logo" :src="logoSrc" alt="앱 로고" />
      </div>
      <div class="header__right">
        <button class="btn btn--outline" @click="onLogin">로그인</button>
      </div>
    </header>

    <!-- Hero / Search -->
    <section class="hero container section">
      <h1 class="hero__title">
        <span class="location">{{ currentLocation }}</span>에서
        <span class="highlight">원룸 청소</span>
        <span>찾고 계신가요?</span>
      </h1>

      <div class="searchbar">
        <div class="chip chip--active" @click="toggleLocationMenu">
          <span class="dot" /> {{ currentLocation }}
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round"
               class="chev">
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </div>

        <div class="search">
          <div class="tabs">
            <button :class="['tab', { 'is-active': searchTab==='market' }]" @click="searchTab='market'">방청소</button>
            <button :class="['tab', { 'is-active': searchTab==='job' }]" @click="searchTab='job'">부분청소</button>
            <button :class="['tab', { 'is-active': searchTab==='estate' }]" @click="searchTab='estate'">청소</button>
          </div>

          <div class="search__input">
            <svg class="icon" viewBox="0 0 24 24" width="20" height="20" aria-hidden>
              <circle cx="11" cy="11" r="7" stroke="currentColor" fill="none" stroke-width="2" />
              <line x1="16.65" y1="16.65" x2="22" y2="22" stroke="currentColor" stroke-width="2" />
            </svg>
            <input
                v-model.trim="keyword"
                :placeholder="placeholders[searchTab]"
                @keydown.enter="onSearch"
            />
          </div>

          <div class="quick">
            <button v-for="kw in quickKeywords" :key="kw"
                    class="quick__btn" @click="keyword = kw">{{ kw }}</button>
          </div>
        </div>
      </div>

      <!-- 위치 드롭다운 -->
      <div v-if="showLocationMenu" class="dropdown" @click.self="showLocationMenu=false">
        <div class="dropdown__panel">
          <h3>동네 선택</h3>
          <div class="chips">
            <button
                v-for="loc in locations"
                :key="loc"
                class="chip"
                :class="{ 'chip--active': loc===currentLocation }"
                @click="selectLocation(loc)"
            >
              <span class="dot" /> {{ loc }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="categories container section">
      <div class="grid">
        <button v-for="c in categories" :key="c.key" class="card" @click="openCategory(c.key)">
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
    <footer class="footer">© 2025 DAON — All rights reserved.</footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import '../assets/HomePage.css'

const router = useRouter()
const logoSrc = '/images/logo.png'

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
</script>
