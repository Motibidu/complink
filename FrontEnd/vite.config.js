import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        // '/api'로 시작하는 요청을 프록시합니다.
        target: 'http://localhost:8080', // 프록시할 대상 서버 주소
        changeOrigin: true, // 대상 서버의 호스트 헤더를 변경합니다.
        rewrite: (path) => path.replace(/^\/api/, ""), // '/api' 경로를 제거하고 전송합니다.
        secure: false,
      },
    },
  },
})
