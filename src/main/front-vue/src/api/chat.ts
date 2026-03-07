import { http } from './http'
import type {
  ChatRoom,
  ChatRoomDetailResponse,
  ChatMessageResponse,
  ChatRoomResponse,
  CreateChatRoomRequest,
} from './types'

export const chatApi = {
  /** 채팅방 생성 (있으면 기존 방 반환) */
  createRoom(requestId: number, otherUserId: string): Promise<ChatRoomResponse> {
    return http
      .post<ChatRoomResponse>('/chat/rooms', { requestId, otherUserId })
      .then((r) => r.data)
  },

  /** 내 채팅방 목록 */
  getRooms(): Promise<ChatRoom[]> {
    return http.get<ChatRoom[]>('/chat/rooms').then((r) => r.data)
  },

  /** 채팅방 상세 (상대방 회원명 등). 헤더 제목 표시용 */
  getRoomDetail(roomId: number): Promise<ChatRoomDetailResponse> {
    return http.get<ChatRoomDetailResponse>(`/chat/rooms/${roomId}`).then((r) => r.data)
  },

  /** 방 메시지 목록 (과거 메시지, 최신이 먼저) */
  getMessages(
    roomId: number,
    page = 0,
    size = 30
  ): Promise<ChatMessageResponse[]> {
    return http
      .get<ChatMessageResponse[]>(`/chat/rooms/${roomId}/messages`, {
        params: { page, size },
      })
      .then((r) => r.data)
  },

  /** 읽음 처리 */
  markRead(roomId: number): Promise<void> {
    return http.post(`/chat/rooms/${roomId}/read`).then(() => undefined)
  },

  /** 안 읽은 개수 */
  getUnreadCount(roomId: number): Promise<number> {
    return http
      .get<number>(`/chat/rooms/${roomId}/unread-count`)
      .then((r) => r.data)
  },
}
