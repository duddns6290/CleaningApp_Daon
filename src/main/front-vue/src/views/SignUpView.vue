<!-- src/views/SignUpView.vue -->
<template>
  <div class="page">
    <div class="card">
      <div class="card-header">
        <img class="logo" src="/images/logo.png" alt="DAON 로고" />
        <h1>회원가입</h1>

        <p v-if="isOauth">
          {{ providerLabel }} 계정으로 인증이 완료되었습니다.<br />
          아래 필수 정보를 입력해 회원가입을 완료해 주세요.
        </p>
        <p v-else>
          이메일과 비밀번호를 입력해 DAON에 가입하세요.
        </p>
      </div>

      <form @submit.prevent="onSubmit">
        <!-- 이메일 -->
        <div class="form-group">
          <label for="email">이메일</label>
          <input
              id="email"
              v-model="form.email"
              type="email"
              required
              :disabled="isOauth"
              placeholder="you@example.com"
          />
        </div>

        <!-- 비밀번호 (일반 가입에서만) -->
        <template v-if="!isOauth">
          <div class="form-group">
            <label for="password">비밀번호</label>
            <input
                id="password"
                v-model="form.password"
                type="password"
                required
                minlength="8"
                placeholder="8자 이상 입력"
            />
          </div>

          <div class="form-group">
            <label for="passwordConfirm">비밀번호 확인</label>
            <input
                id="passwordConfirm"
                v-model="form.passwordConfirm"
                type="password"
                required
                minlength="8"
                placeholder="비밀번호를 한 번 더 입력"
            />
          </div>
        </template>

        <!-- 공통 필수 정보들 -->
        <div class="form-group">
          <label for="name">이름</label>
          <input
              id="name"
              v-model="form.name"
              type="text"
              required
              placeholder="홍길동"
          />
        </div>

        <div class="form-group">
          <label for="phone">휴대폰 번호</label>
          <input
              id="phone"
              v-model="form.phone"
              type="tel"
              required
              placeholder="010-0000-0000"
          />
        </div>

        <!-- 예시: 회원 유형 -->
        <div class="form-group">
          <label>회원 유형</label>
          <div class="radio-group">
            <label>
              <input
                  type="radio"
                  value="CUSTOMER"
                  v-model="form.role"
              />
              청소 요청자
            </label>
            <label>
              <input
                  type="radio"
                  value="CLEANER"
                  v-model="form.role"
              />
              청소 제공자
            </label>
          </div>
        </div>

        <!-- 위치 정보 -->
        <div class="form-group">
          <label>현재 위치</label>

          <div class="location-row">
            <button type="button" class="location-btn" @click="getLocation">
              📍 현재 위치 가져오기
            </button>
          </div>

          <div v-if="loadingLocation" class="location-loading">
            위치 정보를 가져오는 중입니다...
          </div>

          <div v-if="locationError" class="location-error">
            {{ locationError }}
          </div>

          <div class="location-result" v-if="form.region || form.address">
            <p><strong>시/도:</strong> {{ form.region }}</p>
            <p><strong>상세 지역:</strong> {{ form.address }}</p>
          </div>

          <div class="form-group checkbox-row">
            <label>
              <input
                  type="checkbox"
                  v-model="form.locationAgree"
                  required
              />
              위치정보 수집·이용에 동의합니다.
            </label>
          </div>
        </div>

        <button class="submit-btn" type="submit">
          회원가입 완료
        </button>

        <div class="login-link-row">
          이미 계정이 있으신가요?
          <button type="button" class="login-link" @click="goLogin">
            로그인
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'

interface Props {
  type?: string
  email?: string
  provider?: string
}

// ✅ props 실제 값 먼저 받아오기
const props = defineProps<Props>()
const router = useRouter()

// 위치 상태
const loadingLocation = ref(false)
const locationError = ref('')

// 폼 데이터
const form = reactive({
  email: props.email ?? '',   // ✅ 여기서 props 사용
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
  role: 'CUSTOMER',

  // 위치 필드
  region: '',     // 시/도
  address: '',    // 구/동
  locationAgree: false,
})

// 현재 페이지가 OAuth 모드인지 여부
const isOauth = computed(() => props.type === 'oauth')

// 상단에 뜨는 "카카오 / 네이버 / Google" 문구
const providerLabel = computed(() => {
  switch (props.provider) {
    case 'kakao':
      return '카카오'
    case 'naver':
      return '네이버'
    case 'google':
      return 'Google'
    default:
      return '소셜'
  }
})

// 위치 가져오기 함수
const getLocation = () => {
  locationError.value = ''
  loadingLocation.value = true

  if (!navigator.geolocation) {
    locationError.value = '이 브라우저는 위치 정보를 지원하지 않습니다.'
    loadingLocation.value = false
    return
  }

  navigator.geolocation.getCurrentPosition(
      async position => {
        const { latitude, longitude } = position.coords
        await reverseGeocode(latitude, longitude)
      },
      error => {
        loadingLocation.value = false
        switch (error.code) {
          case error.PERMISSION_DENIED:
            locationError.value = '위치 권한이 거부되었습니다.'
            break
          case error.POSITION_UNAVAILABLE:
            locationError.value = '위치 정보를 사용할 수 없습니다.'
            break
          case error.TIMEOUT:
            locationError.value = '위치 정보 요청이 시간 초과되었습니다.'
            break
          default:
            locationError.value = '알 수 없는 오류가 발생했습니다.'
        }
      }
  )
}

// 위도/경도 → 주소 변환 (Nominatim API 사용)
const reverseGeocode = async (lat: number, lon: number) => {
  try {
    const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`
    )
    const data = await res.json()

    const addr = data.address || {}

    // 시/군/구 구성
    form.region = addr.state || addr.region || addr.city || ''
    form.address =
        `${addr.city || addr.town || addr.county || ''} ${addr.suburb || addr.village || addr.hamlet || addr.road || ''}`.trim()
  } catch (e) {
    locationError.value = '주소 변환 중 오류가 발생했습니다.'
  } finally {
    loadingLocation.value = false
  }
}

const onSubmit = async () => {
  // 일반 회원가입일 때만 비밀번호 확인 체크
  if (!isOauth.value && form.password !== form.passwordConfirm) {
    alert('비밀번호가 서로 일치하지 않습니다.')
    return
  }

  try {
    if (isOauth.value) {
      // ✅ OAuth 회원가입 (추가 정보 등록만)
      /*
      await api.post('/api/auth/oauth/signup', {
        email: form.email,
        name: form.name,
        phone: form.phone,
        role: form.role,
        region: form.region,
        address: form.address,
      })
      */
    } else {
      // ✅ 일반 회원가입
      /*
      await api.post('/api/auth/signup', {
        email: form.email,
        password: form.password,
        name: form.name,
        phone: form.phone,
        role: form.role,
        region: form.region,
        address: form.address,
      })
      */
    }

    alert('회원가입이 완료되었습니다.')
    router.push({ name: 'LoginPage' })
  } catch (error) {
    console.error(error)
    alert('회원가입 중 오류가 발생했습니다.')
  }
}

const goLogin = () => {
  router.push({ name: 'LoginPage' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f6fa;
}
.card {
  width: 480px;
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 40px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}
.card-header {
  text-align: center;
  margin-bottom: 24px;
}
.logo {
  width: 150px;
  height: 70px;
  border-radius: 50%;
  object-fit: cover; /* 이미지 잘 맞게 */
}

.card-header h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}
.card-header p {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}
.form-group {
  margin-bottom: 16px;
}
label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
}
input[type='text'],
input[type='email'],
input[type='password'],
input[type='tel'] {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  font-size: 14px;
}
input:disabled {
  background: #f3f4f6;
}
.radio-group {
  display: flex;
  gap: 16px;
  font-size: 14px;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
  padding: 12px;
  border-radius: 999px;
  border: none;
  background: #2563eb;
  color: #ffffff;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
}
.submit-btn:hover {
  opacity: 0.95;
}
.login-link-row {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
}
.login-link {
  margin-left: 4px;
  border: none;
  background: none;
  color: #2563eb;
  cursor: pointer;
  font-weight: 600;
}
.location-row {
  margin-top: 6px;
}
.location-btn {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #2563eb;
  background: white;
  color: #2563eb;
  cursor: pointer;
  font-size: 14px;
}
.location-btn:hover {
  background: #eff6ff;
}
.location-loading {
  margin-top: 6px;
  font-size: 14px;
  color: #2563eb;
}
.location-error {
  margin-top: 6px;
  font-size: 14px;
  color: red;
}
.location-result {
  margin-top: 10px;
  font-size: 14px;
  background: #f1f5f9;
  padding: 10px;
  border-radius: 8px;
}
</style>
