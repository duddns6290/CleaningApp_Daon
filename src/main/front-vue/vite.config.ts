import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  define: {
    global: 'globalThis',
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server:{
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/chat': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          const path = req.url?.split('?')[0] ?? ''
          if (path.startsWith('/chat/rooms')) return undefined
          if (path.startsWith('/chat')) return path
          return undefined
        },
      },
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
      },
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    }
  }
})
