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
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5858,
    strictPort: true, // Garante que o Vite use apenas esta porta (não tenta a 5859, 5860, etc)
    host: true, // Necesário para o cloudflared conseguir acessar
    allowedHosts: ['space.walkerteste.site'], // Permite acesso via túnel Cloudflare
    proxy: {
      '/uploads': {
        target: 'http://localhost:5555',
        changeOrigin: true,
      },
      '/api': {
        target: 'http://localhost:5555',
        changeOrigin: true
      }
    }
  }
})

