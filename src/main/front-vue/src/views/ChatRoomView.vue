<template>
  <div class="chat-room">
    <header class="chat-room__header">
      <button type="button" class="chat-room__back" @click="goBack" aria-label="뒤로">←</button>
      <h1>{{ roomTitle }}</h1>
    </header>

    <!-- 수신 중인 화상통화 알림 (화면 중앙) -->
    <Teleport to="body">
      <div v-if="incomingSessionId" class="incoming-call-overlay">
        <div class="incoming-call-overlay__backdrop" @click.self="rejectIncoming" />
        <div class="incoming-call-overlay__card">
          <p class="incoming-call-overlay__title">화상통화 요청</p>
          <p class="incoming-call-overlay__sub">상대방이 화상통화를 요청했습니다.</p>
          <div class="incoming-call-overlay__actions">
            <button type="button" class="btn btn--outline" @click="rejectIncoming">거절</button>
            <button type="button" class="btn btn--primary" @click="acceptIncoming">수락</button>
          </div>
        </div>
      </div>
    </Teleport>

    <div ref="messagesEl" class="chat-room__messages">
      <div v-if="loading" class="chat-room__loading">메시지 불러오는 중…</div>
      <template v-else>
        <div
          v-for="m in sortedMessages"
          :key="m.id"
          :class="['message', { 'message--mine': isMine(m) }]"
        >
          <span class="message__sender">{{ m.senderId }}</span>
          <p class="message__text">{{ displayMessage(m) }}</p>
          <span class="message__meta">
            {{ formatTime(m.createdAt) }}
            <span v-if="isMine(m)" class="message__read">{{ m.readYn ? '읽음' : '' }}</span>
          </span>
        </div>
      </template>
    </div>

    <form class="chat-room__form" @submit.prevent="send">
      <input
        v-model="inputText"
        type="text"
        class="chat-room__input"
        placeholder="메시지를 입력하세요"
        maxlength="2000"
        :disabled="!socketStore.connected"
      />
      <button type="submit" class="chat-room__send" :disabled="!inputText.trim() || !socketStore.connected">
        전송
      </button>
    </form>

    <div class="chat-room__actions">
      <button type="button" class="btn btn--primary" @click="startVideoCall" :disabled="videoStarting">
        {{ videoStarting ? '연결 중…' : '화상통화 걸기' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chatApi } from '@/api/chat'
import { videoApi } from '@/api/video'
import { useSocketStore } from '@/stores/socket'
import { getHandledSessionIds, addHandledSession, removeHandledSession } from '@/stores/handledVideoSessions'
import { getToken, getUser } from '@/api/auth'
import type { ChatMessageResponse, ChatRoomDetailResponse } from '@/api/types'

const VIDEO_SESSION_PREFIX = '[VIDEO_SESSION:'
const VIDEO_SESSION_SUFFIX = ']'
/** 로드된 메시지에서 수락/거절 바를 띄울 때, 이 시간(ms)보다 오래된 요청은 무시 (방 입장 시 오래된 요청이 계속 뜨는 것 방지) */
const VIDEO_REQUEST_MAX_AGE_MS = 5 * 60 * 1000 // 5분

const route = useRoute()
const router = useRouter()
const socketStore = useSocketStore()

const roomId = computed(() => Number(route.params.roomId))
const roomDetail = ref<ChatRoomDetailResponse | null>(null)
const messages = ref<ChatMessageResponse[]>([])
const loading = ref(true)
const inputText = ref('')
const messagesEl = ref<HTMLElement | null>(null)
const videoStarting = ref(false)
const incomingSessionId = ref<number | null>(null)
let unsubscribe: (() => void) | null = null

function isSessionHandled(sessionId: number) {
  return getHandledSessionIds(roomId.value).has(sessionId)
}

const me = computed(() => getUser()?.userId ?? '')

const roomTitle = computed(() => {
  if (roomDetail.value?.otherUserName) return roomDetail.value.otherUserName
  return `채팅방 #${roomId.value}`
})

const sortedMessages = computed(() => {
  return [...messages.value].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  )
})

function isMine(m: ChatMessageResponse) {
  return m.senderId === me.value
}

function displayMessage(m: ChatMessageResponse): string {
  const text = m.message
  if (text.startsWith(VIDEO_SESSION_PREFIX) && text.includes(VIDEO_SESSION_SUFFIX)) {
    return '[화상통화 요청]'
  }
  return text
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

function goBack() {
  router.push({ name: 'chat-list' })
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesEl.value) {
      messagesEl.value.scrollTop = messagesEl.value.scrollHeight
    }
  })
}

function loadMessages() {
  loading.value = true
  chatApi
    .getRoomDetail(roomId.value)
    .then((detail) => {
      roomDetail.value = detail
    })
    .catch(() => {})
  chatApi
    .getMessages(roomId.value)
    .then((list) => {
      messages.value = list.reverse()
      applyIncomingFromLoadedMessages()
      scrollToBottom()
      chatApi.markRead(roomId.value).catch(() => {})
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

function send() {
  const text = inputText.value.trim()
  if (!text || !socketStore.connected) return
  socketStore.publish('/app/chat/send', { roomId: roomId.value, message: text })
  inputText.value = ''
}

function startVideoCall() {
  videoStarting.value = true
  videoApi
    .createSession(roomId.value)
    .then((res) => {
      socketStore.publish('/app/chat/send', {
        roomId: roomId.value,
        message: `${VIDEO_SESSION_PREFIX}${res.sessionId}${VIDEO_SESSION_SUFFIX}`,
      })
      router.push({
        name: 'video-call',
        params: { roomId: String(roomId.value) },
        query: { sessionId: String(res.sessionId), role: 'caller' },
      })
    })
    .catch((err) => {
      alert(err?.response?.data?.message || '화상통화를 시작할 수 없습니다.')
    })
    .finally(() => {
      videoStarting.value = false
    })
}

function acceptIncoming() {
  const sid = incomingSessionId.value
  if (sid == null) return
  addHandledSession(roomId.value, sid)
  incomingSessionId.value = null
  videoApi
    .accept(sid, roomId.value)
    .then(() => {
      router.push({
        name: 'video-call',
        params: { roomId: String(roomId.value) },
        query: { sessionId: String(sid), role: 'callee' },
      })
    })
    .catch(() => {
      removeHandledSession(roomId.value, sid)
      alert('통화 수락에 실패했습니다.')
    })
}

function rejectIncoming() {
  const sid = incomingSessionId.value
  if (sid == null) return
  addHandledSession(roomId.value, sid)
  incomingSessionId.value = null
  videoApi
    .reject(sid, roomId.value)
    .catch(() => {})
}

function checkIncomingFromMessage(m: ChatMessageResponse) {
  if (m.senderId === me.value) return
  const text = m.message
  const start = text.indexOf(VIDEO_SESSION_PREFIX)
  if (start === -1) return
  const end = text.indexOf(VIDEO_SESSION_SUFFIX, start)
  if (end === -1) return
  const idStr = text.slice(start + VIDEO_SESSION_PREFIX.length, end)
  const id = parseInt(idStr, 10)
  if (!Number.isNaN(id) && !isSessionHandled(id)) {
    incomingSessionId.value = id
  }
}

/**
 * 로드된 메시지 중 상대가 보낸 '가장 최근' 화상통화 요청 하나만 보고 수신 알림 표시.
 * - 상대가 보낸 것만, 이미 수락/거절/종료한 세션이면 바 띄우지 않음.
 * - 가장 최근 요청만 검사하고 끝냄 (예전 요청으로 수락/거절 바가 다시 뜨지 않도록).
 * - VIDEO_REQUEST_MAX_AGE_MS 보다 오래된 요청은 무시.
 */
function applyIncomingFromLoadedMessages() {
  const now = Date.now()
  for (let i = messages.value.length - 1; i >= 0; i--) {
    const m = messages.value[i]
    if (m.senderId === me.value) continue
    const msgTime = new Date(m.createdAt).getTime()
    if (now - msgTime > VIDEO_REQUEST_MAX_AGE_MS) continue
    const text = m.message
    const start = text.indexOf(VIDEO_SESSION_PREFIX)
    if (start === -1) continue
    const end = text.indexOf(VIDEO_SESSION_SUFFIX, start)
    if (end === -1) continue
    const idStr = text.slice(start + VIDEO_SESSION_PREFIX.length, end)
    const id = parseInt(idStr, 10)
    if (!Number.isNaN(id)) {
      if (!isSessionHandled(id)) {
        incomingSessionId.value = id
      }
      break
    }
  }
}

onMounted(() => {
  socketStore.connect(getToken() || '')
  loadMessages()

  watch(
    () => socketStore.connected,
    (isConnected) => {
      unsubscribe?.()
      unsubscribe = null
      if (!isConnected) return
      unsubscribe = socketStore.subscribe(`/topic/chat/${roomId.value}`, (msg) => {
        const body = msg.body
        if (!body) return
        try {
          const data = JSON.parse(body) as Record<string, unknown>
          if (data.type === 'ROOM_READ' && typeof data.readerId === 'string') {
            const readerId = data.readerId as string
            if (readerId !== me.value) {
              messages.value = messages.value.map((m) =>
                m.senderId === me.value ? { ...m, readYn: true } : m
              )
            }
            return
          }
          const asMsg = data as ChatMessageResponse
          if (asMsg.id != null && asMsg.message != null) {
            if (!messages.value.some((m) => m.id === asMsg.id)) {
              messages.value.push(asMsg)
              checkIncomingFromMessage(asMsg)
              scrollToBottom()
              if (asMsg.senderId !== me.value) {
                chatApi.markRead(roomId.value).catch(() => {})
              }
            }
          }
        } catch {
          // ignore
        }
      })
    },
    { immediate: true }
  )
})

onUnmounted(() => {
  unsubscribe?.()
})
</script>

<style scoped>
.chat-room {
  max-width: 480px;
  margin: 0 auto;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-background);
}

.chat-room__header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--color-border);
}

.chat-room__back {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  color: var(--color-heading);
}

.chat-room__header h1 {
  font-size: 1.1rem;
  font-weight: 600;
}

/* 화상통화 수신 알림 - 화면 중앙 오버레이 */
.incoming-call-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.incoming-call-overlay__backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
}

.incoming-call-overlay__card {
  position: relative;
  width: 100%;
  max-width: 320px;
  padding: 1.5rem;
  background: var(--color-background);
  border-radius: 1rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.incoming-call-overlay__title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  color: var(--color-heading);
}

.incoming-call-overlay__sub {
  font-size: 0.95rem;
  color: var(--color-text);
  margin: 0 0 1.25rem 0;
}

.incoming-call-overlay__actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.chat-room__messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.chat-room__loading {
  text-align: center;
  padding: 1rem;
  color: var(--color-text);
}

.message {
  max-width: 85%;
  align-self: flex-start;
  padding: 0.5rem 0.75rem;
  border-radius: 0.75rem;
  background: var(--color-background-mute);
}

.message--mine {
  align-self: flex-end;
  background: var(--vt-c-indigo);
  color: var(--vt-c-white);
}

.message__sender {
  font-size: 0.75rem;
  opacity: 0.9;
  display: block;
  margin-bottom: 0.2rem;
}

.message__text {
  margin: 0;
  font-size: 0.95rem;
  word-break: break-word;
}

.message__meta {
  font-size: 0.7rem;
  opacity: 0.8;
  display: block;
  margin-top: 0.2rem;
}

.message__read {
  margin-left: 0.35rem;
}

.chat-room__form {
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-top: 1px solid var(--color-border);
}

.chat-room__input {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  font-size: 1rem;
}

.chat-room__send {
  padding: 0.5rem 1rem;
  background: var(--vt-c-indigo);
  color: white;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  font-weight: 500;
}

.chat-room__send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chat-room__actions {
  padding: 0.5rem 1rem;
  border-top: 1px solid var(--color-border);
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.95rem;
  cursor: pointer;
  border: none;
}

.btn--primary {
  background: var(--vt-c-indigo);
  color: white;
}

.btn--outline {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
</style>
