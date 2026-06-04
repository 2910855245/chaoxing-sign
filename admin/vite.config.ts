import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://38.76.190.251:8765',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://38.76.190.251:8765',
        ws: true,
      },
    },
  },
})
