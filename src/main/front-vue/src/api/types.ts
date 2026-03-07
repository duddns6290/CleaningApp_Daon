// 채팅방 (GET /chat/rooms 응답 한 건 - 목록용, 상대방 이름·안 읽은 개수 포함)
export interface ChatRoom {
  id: number
  requestId: number
  customerId: string
  providerId: string
  lastMessage: string | null
  lastMessageAt: string | null
  createdAt: string
  otherUserId?: string
  otherUserName?: string
  unreadCount?: number
}

/** 채팅방 상세 (GET /chat/rooms/:roomId) - 헤더 제목용 상대방 회원명 */
export interface ChatRoomDetailResponse {
  roomId: number
  otherUserId: string
  otherUserName: string
}

/** 채팅방 생성 요청 */
export interface CreateChatRoomRequest {
  requestId: number
  otherUserId: string
}

/** 채팅방 생성 응답 */
export interface ChatRoomResponse {
  roomId: number
  requestId: number
  customerId: string
  providerId: string
  lastMessage: string | null
  lastMessageAt: string | null
}

export type SenderRole = 'CUSTOMER' | 'PROVIDER'

// 메시지 (목록/실시간)
export interface ChatMessageResponse {
  id: number
  roomId: number
  senderId: string
  senderRole: SenderRole
  message: string
  readYn: boolean
  createdAt: string
}

export interface ChatSendRequest {
  roomId: number
  message: string
}

// 화상 세션 응답
export type VideoSessionStatus =
  | 'CREATED'
  | 'RINGING'
  | 'ACTIVE'
  | 'REJECTED'
  | 'MISSED'
  | 'ENDED'

export interface VideoSessionResponse {
  sessionId: number
  chatRoomId: number
  status: VideoSessionStatus
  createdBy: string
  createdAt: string
  acceptedAt: string | null
  endedAt: string | null
}

// 시그널 메시지 (WebSocket)
export type SignalType = 'JOIN' | 'LEAVE' | 'OFFER' | 'ANSWER' | 'ICE' | 'END'

export interface SignalMessage {
  sessionId: number
  type: SignalType
  fromUserId?: string
  payload?: string
}
