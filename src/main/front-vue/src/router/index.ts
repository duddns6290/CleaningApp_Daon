import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginPage from '../views/LoginPage.vue'
import ChatListView from '../views/ChatListView.vue'
import ChatRoomView from '../views/ChatRoomView.vue'
import VideoCallView from '../views/VideoCallView.vue'
import OAuthCallbackView from '../views/OAuthCallbackView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'LoginPage',
      component: LoginPage,
    },
    {
      path: '/oauth-callback',
      name: 'oauth-callback',
      component: OAuthCallbackView,
    },
    {
      path: '/chat',
      name: 'chat-list',
      component: ChatListView,
    },
    {
      path: '/chat/:roomId',
      name: 'chat-room',
      component: ChatRoomView,
    },
    {
      path: '/chat/:roomId/video',
      name: 'video-call',
      component: VideoCallView,
    },
  ],
})

export default router
