import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '@/api/auth'

const WS_PATH = '/ws/chat'

function getWsBaseUrl(): string {
  if (import.meta.env.DEV) {
    return import.meta.env.VITE_WS_ORIGIN || 'http://localhost:8080'
  }
  return import.meta.env.VITE_WS_URL || window.location.origin
}

export const useSocketStore = defineStore('socket', () => {
  const client = shallowRef<Client | null>(null)
  const connected = ref(false)

  function connect(accessToken: string) {
    const token = accessToken || getToken()
    if (!token) {
      console.warn('[socket] No token, skip connect')
      return
    }

    if (client.value?.active) {
      return
    }

    const c = new Client({
      webSocketFactory: () => {
        const url = getWsBaseUrl() + WS_PATH
        return new SockJS(url) as unknown as WebSocket
      },
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelaySeconds: 2,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        connected.value = true
      },
      onDisconnect: () => {
        connected.value = false
      },
      onStompError: (frame) => {
        console.error('[STOMP]', frame.headers?.message || frame)
      },
    })

    client.value = c
    c.activate()
  }

  function disconnect() {
    if (client.value) {
      client.value.deactivate()
      client.value = null
      connected.value = false
    }
  }

  function publish(destination: string, body: string | object): void {
    const c = client.value
    if (!c?.active) {
      console.warn('[socket] Not connected, cannot publish to', destination)
      return
    }
    const payload = typeof body === 'string' ? body : JSON.stringify(body)
    c.publish({ destination, body: payload })
  }

  function subscribe(
    destination: string,
    callback: (message: IMessage) => void
  ): () => void {
    const c = client.value
    if (!c?.active) {
      console.warn('[socket] Not connected, cannot subscribe to', destination)
      return () => {}
    }
    const sub = c.subscribe(destination, callback)
    return () => sub.unsubscribe()
  }

  return {
    client,
    connected,
    connect,
    disconnect,
    publish,
    subscribe,
  }
})
