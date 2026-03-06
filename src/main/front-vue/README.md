# front-vue

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Recommended Browser Setup

- Chromium-based browsers (Chrome, Edge, Brave, etc.):
  - [Vue.js devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd) 
  - [Turn on Custom Object Formatter in Chrome DevTools](http://bit.ly/object-formatters)
- Firefox:
  - [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)
  - [Turn on Custom Object Formatter in Firefox DevTools](https://fxdx.dev/firefox-devtools-custom-object-formatters/)

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

## 채팅 / 화상통화

- **채팅**: `/chat` — 방 목록, `/chat/:roomId` — 실시간 메시지 (STOMP `/topic/chat/{roomId}`).
- **화상통화**: 채팅방에서 "화상통화 걸기" → 세션 생성 후 `/chat/:roomId/video`에서 WebRTC + 시그널링(STOMP `/app/video.signal`, `/topic/video.session.{sessionId}`).

모든 API·WebSocket은 **JWT(access token)** 가 필요합니다. 로그인 연동 후 `api/auth.ts`의 `setToken(accessToken)`, `setUser({ userId, role })`를 호출해 두면 됩니다. (개발 시 토큰은 localStorage `daon_access_token`에 넣어 테스트할 수 있습니다.)
