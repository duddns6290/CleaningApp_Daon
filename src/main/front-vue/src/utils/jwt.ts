/**
 * JWT payload만 base64 디코딩하여 읽기 (서명 검증 없음, 클라이언트 표시용).
 * sub → userId, role 사용.
 */
export interface JwtPayload {
  sub?: string
  role?: string
  typ?: string
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(json) as JwtPayload
  } catch {
    return null
  }
}
