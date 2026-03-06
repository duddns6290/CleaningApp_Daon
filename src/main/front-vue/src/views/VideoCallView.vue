<template>
  <div class="video-call">
    <header class="video-call__header">
      <button type="button" class="video-call__back" @click="leave" aria-label="통화 종료">통화 종료</button>
      <h1>화상통화</h1>
      <span class="video-call__status">{{ statusText }}</span>
    </header>

    <div class="video-call__videos">
      <div class="video-call__remote">
        <video ref="remoteVideoEl" autoplay playsinline />
        <p v-if="callEndedByRemote" class="video-call__ended">상대방이 통화를 종료했습니다.</p>
        <p v-else-if="!remoteStream && !connecting">상대방을 기다리는 중…</p>
      </div>
      <div class="video-call__local">
        <video ref="localVideoEl" autoplay playsinline muted />
      </div>
    </div>

    <div class="video-call__actions">
      <button type="button" class="btn btn--danger" @click="leave" :disabled="ending">
        {{ ending ? '종료 중…' : '통화 종료' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { videoApi } from '@/api/video'
import { useSocketStore } from '@/stores/socket'
import { addHandledSession } from '@/stores/handledVideoSessions'
import { getToken, getUser } from '@/api/auth'
import type { SignalMessage } from '@/api/types'

const route = useRoute()
const router = useRouter()
const socketStore = useSocketStore()

const sessionId = computed(() => Number(route.query.sessionId))
const chatRoomId = computed(() => Number(route.params.roomId))
const role = computed(() => (route.query.role as string) || 'caller')

const localVideoEl = ref<HTMLVideoElement | null>(null)
const remoteVideoEl = ref<HTMLVideoElement | null>(null)
const localStream = ref<MediaStream | null>(null)
const remoteStream = ref<MediaStream | null>(null)
const connecting = ref(true)
const ending = ref(false)
const callEndedByRemote = ref(false)

let pc: RTCPeerConnection | null = null
let unsubscribeSignal: (() => void) | null = null

const me = computed(() => getUser()?.userId ?? '')

const statusText = computed(() => {
  if (callEndedByRemote.value) return '상대방이 통화를 종료했습니다.'
  if (remoteStream.value) return '통화 중'
  if (connecting.value) return '연결 중…'
  return '대기 중'
})

const iceConfig: RTCConfiguration = {
  iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
}

function attachLocalStream(stream: MediaStream) {
  localStream.value = stream
  if (localVideoEl.value) {
    localVideoEl.value.srcObject = stream
  }
}

function attachRemoteStream(stream: MediaStream) {
  remoteStream.value = stream
  if (remoteVideoEl.value) {
    remoteVideoEl.value.srcObject = stream
  }
  connecting.value = false
}

function sendSignal(type: SignalMessage['type'], payload?: string) {
  socketStore.publish('/app/video.signal', {
    sessionId: sessionId.value,
    type,
    fromUserId: me.value,
    payload,
  })
}

function createPeerConnection(): RTCPeerConnection {
  const peer = new RTCPeerConnection(iceConfig)

  peer.onicecandidate = (e) => {
    if (e.candidate) {
      sendSignal('ICE', JSON.stringify(e.candidate))
    }
  }

  peer.ontrack = (e) => {
    if (e.streams[0]) {
      attachRemoteStream(e.streams[0])
    }
  }

  return peer
}

function subscribeAndStart() {
  const dest = `/topic/video.session.${sessionId.value}`
  unsubscribeSignal = socketStore.subscribe(dest, (msg) => {
    const body = msg.body
    if (!body) return
    try {
      const data = JSON.parse(body) as SignalMessage
      if (data.fromUserId === me.value) return

      switch (data.type) {
        case 'JOIN':
          if (role.value === 'caller' && pc) {
            pc.createOffer()
              .then((offer) => pc!.setLocalDescription(offer))
              .then(() =>
                sendSignal('OFFER', JSON.stringify(pc!.localDescription))
              )
              .catch(console.error)
          }
          break
        case 'OFFER': {
          if (role.value !== 'callee' || !pc) break
          const offer = JSON.parse(data.payload || '{}') as RTCSessionDescriptionInit
          pc.setRemoteDescription(new RTCSessionDescription(offer))
            .then(() => pc!.createAnswer())
            .then((answer) => pc!.setLocalDescription(answer))
            .then(() =>
              sendSignal('ANSWER', JSON.stringify(pc!.localDescription))
            )
            .catch(console.error)
          break
        }
        case 'ANSWER': {
          if (role.value !== 'caller' || !pc) break
          const answer = JSON.parse(data.payload || '{}') as RTCSessionDescriptionInit
          pc.setRemoteDescription(new RTCSessionDescription(answer)).catch(
            console.error
          )
          break
        }
        case 'ICE': {
          if (!pc) break
          const candidate = JSON.parse(data.payload || '{}') as RTCIceCandidateInit
          pc.addIceCandidate(new RTCIceCandidate(candidate)).catch(console.error)
          break
        }
        case 'END':
        case 'LEAVE':
          addHandledSession(chatRoomId.value, sessionId.value)
          connecting.value = false
          remoteStream.value = null
          if (remoteVideoEl.value) remoteVideoEl.value.srcObject = null
          callEndedByRemote.value = true
          videoApi.end(sessionId.value, chatRoomId.value).catch(() => {})
          cleanup()
          setTimeout(() => {
            router.replace({ name: 'chat-room', params: { roomId: String(chatRoomId.value) } })
          }, 1500)
          break
      }
    } catch (e) {
      console.warn('Signal parse error', e)
    }
  })

  sendSignal('JOIN')

  if (role.value === 'caller') {
    navigator.mediaDevices
      .getUserMedia({ video: true, audio: true })
      .then((stream) => {
        attachLocalStream(stream)
        pc = createPeerConnection()
        stream.getTracks().forEach((t) => pc!.addTrack(t, stream))
      })
      .catch((err) => {
        console.error('getUserMedia', err)
        connecting.value = false
        alert('카메라/마이크 접근을 허용해주세요.')
      })
  } else {
    navigator.mediaDevices
      .getUserMedia({ video: true, audio: true })
      .then((stream) => {
        attachLocalStream(stream)
        pc = createPeerConnection()
        stream.getTracks().forEach((t) => pc!.addTrack(t, stream))
      })
      .catch((err) => {
        console.error('getUserMedia', err)
        connecting.value = false
        alert('카메라/마이크 접근을 허용해주세요.')
      })
  }
}

function leave() {
  ending.value = true
  addHandledSession(chatRoomId.value, sessionId.value)
  sendSignal('END')
  videoApi
    .end(sessionId.value, chatRoomId.value)
    .catch(() => {})
    .finally(() => {
      cleanup()
      router.push({ name: 'chat-room', params: { roomId: String(chatRoomId.value) } })
    })
}

function cleanup() {
  unsubscribeSignal?.()
  unsubscribeSignal = null
  localStream.value?.getTracks().forEach((t) => t.stop())
  localStream.value = null
  remoteStream.value = null
  if (remoteVideoEl.value) remoteVideoEl.value.srcObject = null
  if (localVideoEl.value) localVideoEl.value.srcObject = null
  pc?.close()
  pc = null
}

onMounted(() => {
  socketStore.connect(getToken() || '')
  if (!sessionId.value || !chatRoomId.value) {
    router.replace({ name: 'chat-list' })
    return
  }
  subscribeAndStart()
})

onUnmounted(() => {
  if (!ending.value) {
    sendSignal('END')
    cleanup()
  }
})
</script>

<style scoped>
.video-call {
  max-width: 720px;
  margin: 0 auto;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-background);
}

.video-call__header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--color-border);
}

.video-call__back {
  background: none;
  border: none;
  font-size: 0.95rem;
  cursor: pointer;
  color: var(--vt-c-indigo);
}

.video-call__header h1 {
  font-size: 1.1rem;
  font-weight: 600;
  flex: 1;
}

.video-call__status {
  font-size: 0.85rem;
  color: var(--color-text);
  opacity: 0.8;
}

.video-call__videos {
  flex: 1;
  position: relative;
  background: #111;
  min-height: 300px;
}

.video-call__remote {
  width: 100%;
  height: 100%;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-call__remote video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-call__remote p {
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.video-call__ended {
  font-weight: 600;
  font-size: 1rem;
}

.video-call__local {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  width: 120px;
  height: 90px;
  border-radius: 0.5rem;
  overflow: hidden;
  border: 2px solid var(--color-border);
}

.video-call__local video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-call__actions {
  padding: 1rem;
  border-top: 1px solid var(--color-border);
  text-align: center;
}

.btn--danger {
  background: #c00;
  color: white;
  padding: 0.5rem 1.5rem;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  font-size: 1rem;
}

.btn--danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
