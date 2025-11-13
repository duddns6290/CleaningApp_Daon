<template>
  <div class="auth-page">
    <!-- Header -->
    <TheHeader />
    <!-- Auth Card -->
    <main class="auth container">
      <section class="auth__card" role="dialog" aria-labelledby="authTitle">
        <div class="auth__brand">
          <div class="auth__badge">DAON</div>
          <h1 id="authTitle">로그인</h1>
          <p class="auth__sub">ELO로 더 똑똑한 1:1 청소 매칭</p>
        </div>

        <!-- 탭 -->
        <div class="auth__tabs" role="tablist" aria-label="로그인 방식">
          <button
              role="tab"
              :aria-selected="mode==='email'"
              :class="['auth__tab', { 'is-active': mode==='email' }]"
              @click="mode='email'"
          >이메일</button>
          <button
              role="tab"
              :aria-selected="mode==='phone'"
              :class="['auth__tab', { 'is-active': mode==='phone' }]"
              @click="mode='phone'"
          >휴대폰</button>
        </div>

        <!-- 폼 -->
        <form class="auth__form" @submit.prevent="onSubmit">
          <div v-if="mode==='email'" class="field">
            <label for="email">이메일</label>
            <input
                id="email"
                type="email"
                v-model.trim="email"
                inputmode="email"
                autocomplete="email"
                placeholder="you@example.com"
                :class="{ 'is-invalid': errors.email }"
                @blur="validateEmail()"
            />
            <p v-if="errors.email" class="error">{{ errors.email }}</p>
          </div>

          <div v-if="mode==='phone'" class="field">
            <label for="phone">휴대폰 번호</label>
            <input
                id="phone"
                type="tel"
                v-model.trim="phone"
                inputmode="tel"
                autocomplete="tel"
                placeholder="010-1234-5678"
                :class="{ 'is-invalid': errors.phone }"
                @blur="validatePhone()"
            />
            <p v-if="errors.phone" class="error">{{ errors.phone }}</p>
          </div>

          <div class="field">
            <label for="password">비밀번호</label>
            <div class="password">
              <input
                  id="password"
                  :type="showPw ? 'text' : 'password'"
                  v-model="password"
                  autocomplete="current-password"
                  placeholder="••••••••"
                  :class="{ 'is-invalid': errors.password }"
                  @blur="validatePassword()"
              />
              <button class="pw-toggle" type="button" @click="showPw=!showPw" :aria-pressed="showPw">
                {{ showPw ? '숨기기' : '표시' }}
              </button>
            </div>
            <p v-if="errors.password" class="error">{{ errors.password }}</p>
          </div>

          <div class="row row--between">
            <label class="check">
              <input type="checkbox" v-model="remember" />
              <span>로그인 상태 유지</span>
            </label>
            <button type="button" class="link" @click="onForgot">비밀번호 찾기</button>
          </div>

          <button class="btn btn--primary auth__submit" type="submit" :disabled="loading">
            {{ loading ? '로그인 중…' : '로그인' }}
          </button>
        </form>

        <div class="divider"><span>또는</span></div>

        <div class="social">
          <button class="social__btn social--kakao" @click="social('kakao')" aria-label="카카오로 로그인">카카오로 로그인</button>
          <button class="social__btn social--naver" @click="social('naver')" aria-label="네이버로 로그인">네이버로 로그인</button>
          <button class="social__btn social--google" @click="social('google')" aria-label="구글로 로그인">Google</button>
        </div>

        <p class="signup">
          계정이 없으신가요?
          <button class="link" type="button" @click="goSignup">회원가입</button>
        </p>
      </section>
    </main>

    <TheFooter />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TheHeader from '@/components/layout/TheHeader.vue'
import TheFooter from '@/components/layout/TheFooter.vue'
import '../assets/LoginPage.css'

type Mode = 'email' | 'phone'
const mode = ref<Mode>('email')

const email = ref('')
const phone = ref('')
const password = ref('')
const remember = ref(true)
const showPw = ref(false)
const loading = ref(false)

const errors = ref<{ email?: string; phone?: string; password?: string }>({})

function validateEmail () {
  if (mode.value !== 'email') return true
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)
  errors.value.email = ok ? '' : '유효한 이메일을 입력해주세요.'
  return ok
}
function validatePhone () {
  if (mode.value !== 'phone') return true
  const ok = /^01[0-9]-?\d{3,4}-?\d{4}$/.test(phone.value)
  errors.value.phone = ok ? '' : '휴대폰 번호 형식(010-1234-5678)을 확인해주세요.'
  return ok
}
function validatePassword () {
  const ok = password.value.length >= 6
  errors.value.password = ok ? '' : '비밀번호는 6자 이상이어야 합니다.'
  return ok
}

async function onSubmit () {
  const ok =
      (mode.value === 'email' ? validateEmail() : validatePhone()) &&
      validatePassword()
  if (!ok) return

  try {
    loading.value = true
    await new Promise(r => setTimeout(r, 600))
    alert('로그인 성공! (데모)')
  } catch (e) {
    alert('로그인에 실패했어요. 다시 시도해주세요.')
  } finally {
    loading.value = false
  }
}

//소셜 로그인 링크 이동 함수
function social (provider: 'kakao' | 'naver' | 'google') {
  window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
}

function onForgot () { alert('[데모] 비밀번호 찾기') }
function goSignup () { alert('[데모] 회원가입 이동') }
function goHome () { history.back() }
</script>
