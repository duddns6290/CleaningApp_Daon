<template>
  <div class="chat-list">
    <header class="chat-list__header">
      <router-link to="/" class="chat-list__home">← 홈</router-link>
      <h1>채팅</h1>
      <p class="chat-list__sub">청소 전후 문의 및 조율</p>
    </header>

    <div v-if="loading" class="chat-list__loading">불러오는 중…</div>
    <div v-else-if="error" class="chat-list__error">{{ error }}</div>
    <template v-else>
      <!-- 새 채팅방 만들기 -->
      <section class="chat-list__create">
        <h2 class="chat-list__create-title">새 채팅방 만들기</h2>
        <form class="chat-list__create-form" @submit.prevent="createRoom">
          <div class="field">
            <label for="requestId">청소 요청 ID</label>
            <input
              id="requestId"
              v-model.number="newRequestId"
              type="number"
              min="1"
              placeholder="예: 1"
              required
            />
          </div>
          <div class="field">
            <label for="otherUserId">상대방 이메일</label>
            <input
              id="otherUserId"
              v-model.trim="newOtherUserId"
              type="text"
              placeholder="상대방 로그인 이메일"
              required
            />
          </div>
          <p v-if="createError" class="chat-list__create-error">{{ createError }}</p>
          <button type="submit" class="btn btn--primary" :disabled="createLoading">
            {{ createLoading ? '만드는 중…' : '채팅방 만들기' }}
          </button>
        </form>
      </section>

      <div v-if="rooms.length === 0" class="chat-list__empty">
        참여 중인 채팅방이 없습니다.
      </div>
      <ul v-else class="room-list">
      <li
        v-for="room in rooms"
        :key="room.id"
        class="room-item"
        @click="goRoom(room.id)"
      >
        <div class="room-item__info">
          <span class="room-item__id">{{ roomDisplayName(room) }}</span>
          <span class="room-item__right">
            <span v-if="roomUnreadCount(room) > 0" class="room-item__badge">{{ roomUnreadCount(room) }}</span>
            <span class="room-item__time">{{ formatTime(room.lastMessageAt) }}</span>
          </span>
        </div>
        <p class="room-item__last">{{ room.lastMessage || '(메시지 없음)' }}</p>
      </li>
    </ul>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { chatApi } from '@/api/chat'
import { useSocketStore } from '@/stores/socket'
import { getToken } from '@/api/auth'
import type { ChatRoom } from '@/api/types'

const router = useRouter()
const socketStore = useSocketStore()

const rooms = ref<ChatRoom[]>([])
const loading = ref(true)
const error = ref('')

const newRequestId = ref<number>(1)
const newOtherUserId = ref('')
const createLoading = ref(false)
const createError = ref('')

function formatTime(iso: string | null): string {
  if (!iso) return ''
  const d = new Date(iso)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })
}

function roomDisplayName(room: ChatRoom): string {
  return room.otherUserName?.trim() || `방 #${room.id}`
}

function roomUnreadCount(room: ChatRoom): number {
  const n = room.unreadCount
  if (n === undefined || n === null) return 0
  return Number(n)
}

function goRoom(roomId: number) {
  router.push({ name: 'chat-room', params: { roomId: String(roomId) } })
}

async function createRoom() {
  createError.value = ''
  createLoading.value = true
  try {
    const res = await chatApi.createRoom(newRequestId.value, newOtherUserId.value)
    rooms.value = await chatApi.getRooms()
    goRoom(res.roomId)
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    createError.value = err.response?.data?.message || err.message || '채팅방을 만들 수 없습니다.'
  } finally {
    createLoading.value = false
  }
}

onMounted(async () => {
  socketStore.connect(getToken() || '')
  await fetchRooms()
})

onActivated(() => {
  fetchRooms()
})

async function fetchRooms() {
  try {
    rooms.value = await chatApi.getRooms()
  } catch (e: unknown) {
    const err = e as { response?: { status?: number; data?: { message?: string } }; message?: string }
    if (err.response?.status === 401) {
      error.value = '로그인이 필요합니다.'
    } else {
      error.value = err.response?.data?.message || err.message || '목록을 불러올 수 없습니다.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.chat-list {
  max-width: 480px;
  margin: 0 auto;
  padding: 1rem;
}

.chat-list__header {
  margin-bottom: 1.5rem;
}

.chat-list__home {
  display: inline-block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: var(--vt-c-indigo);
  text-decoration: none;
}

.chat-list__home:hover {
  text-decoration: underline;
}

.chat-list__header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
}

.chat-list__sub {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.8;
  margin-top: 0.25rem;
}

.chat-list__loading,
.chat-list__error,
.chat-list__empty {
  text-align: center;
  padding: 2rem;
  color: var(--color-text);
}

.chat-list__error {
  color: #c00;
}

.chat-list__create {
  margin-bottom: 1.5rem;
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  background: var(--color-background-mute);
}

.chat-list__create-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.chat-list__create-form .field {
  margin-bottom: 0.75rem;
}

.chat-list__create-form label {
  display: block;
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
  color: var(--color-text);
}

.chat-list__create-form input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 0.35rem;
  font-size: 1rem;
}

.chat-list__create-form .btn {
  margin-top: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  border: none;
  background: var(--vt-c-indigo);
  color: white;
  cursor: pointer;
  font-weight: 500;
}

.chat-list__create-form .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chat-list__create-error {
  color: #c00;
  font-size: 0.9rem;
  margin: 0.5rem 0 0 0;
}

.room-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.room-item {
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background 0.15s;
}

.room-item:hover {
  background: var(--color-background-mute);
}

.room-item__info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.35rem;
}

.room-item__id {
  font-weight: 600;
  color: var(--color-heading);
}

.room-item__right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.room-item__badge {
  min-width: 1.25rem;
  padding: 0.15rem 0.4rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
  background: var(--vt-c-indigo);
  border-radius: 999px;
  text-align: center;
}

.room-item__time {
  font-size: 0.8rem;
  color: var(--color-text);
  opacity: 0.7;
}

.room-item__last {
  font-size: 0.9rem;
  color: var(--color-text);
  opacity: 0.9;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
