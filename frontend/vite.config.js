import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Vite configuration with Vue plugin and a dev proxy
 * forwarding /api requests to the Spring Boot backend.
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws-native': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
})
