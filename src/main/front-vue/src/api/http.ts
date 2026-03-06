import axios from 'axios'
import { getToken } from './auth'

const baseURL = import.meta.env.DEV ? '' : (import.meta.env.VITE_API_BASE_URL || '')

export const http = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      // 필요 시 로그인 페이지로 리다이렉트
      const path = window.location.pathname
      if (!path.startsWith('/login')) {
        // store 또는 router로 로그인 페이지 이동 가능
      }
    }
    return Promise.reject(err)
  }
)
