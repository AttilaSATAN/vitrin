import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Vite configuration with Vue plugin and a dev proxy
 * forwarding /api requests to the Spring Boot backend.
 */
export default defineConfig({
  plugins: [vue()],

})
