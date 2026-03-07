import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginPage from '../views/LoginPage.vue'
import SignUpView from '@/views/SignUpView.vue'
import OauthCallbackView from '@/views/OauthCallbackView.vue'
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
      path: '/signup',
      name: 'SignUp',
      component: SignUpView,
      // ?type=oauth&email=xxx 이런 식으로 넘겨받기
      props: route => ({
        type: route.query.type,
        email: route.query.email,
        provider: route.query.provider,
      }),
    },
    {
      path: '/oauth/callback',
      name: 'OauthCallback',
      component: OauthCallbackView,
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
