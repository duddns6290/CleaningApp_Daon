import { http } from './http'
import type { VideoSessionResponse } from './types'

export const videoApi = {
  /** 화상 세션 생성 (통화 걸기) */
  createSession(chatRoomId: number): Promise<VideoSessionResponse> {
    return http
      .post<VideoSessionResponse>('/api/video/sessions', { chatRoomId })
      .then((r) => r.data)
  },

  /** 통화 수락 */
  accept(sessionId: number, chatRoomId: number): Promise<VideoSessionResponse> {
    return http
      .post<VideoSessionResponse>(
        `/api/video/sessions/${sessionId}/accept`,
        null,
        { params: { chatRoomId } }
      )
      .then((r) => r.data)
  },

  /** 통화 거절 */
  reject(sessionId: number, chatRoomId: number): Promise<VideoSessionResponse> {
    return http
      .post<VideoSessionResponse>(
        `/api/video/sessions/${sessionId}/reject`,
        null,
        { params: { chatRoomId } }
      )
      .then((r) => r.data)
  },

  /** 통화 종료 */
  end(sessionId: number, chatRoomId: number): Promise<VideoSessionResponse> {
    return http
      .post<VideoSessionResponse>(
        `/api/video/sessions/${sessionId}/end`,
        null,
        { params: { chatRoomId } }
      )
      .then((r) => r.data)
  },
}
