/**
 * Access token 저장/조회.
 * 로그인 성공 시 setToken(accessToken) 호출하고,
 * 실제 로그인 API 연동 후 여기서 토큰을 설정하면 됩니다.
 */
const TOKEN_KEY = 'daon_access_token'
const USER_KEY = 'daon_user'

export interface AuthUser {
  userId: string
  role: string
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function setUser(user: AuthUser): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

export function isAuthenticated(): boolean {
  return !!getToken()
}
