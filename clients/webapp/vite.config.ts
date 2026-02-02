/**
 * Converted from: src/WebApp (Blazor to React)
 *
 * Vite configuration for the React SPA.
 */
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 8080,
    proxy: {
      '/api/catalog': {
        target: 'http://localhost:9101',
        changeOrigin: true,
      },
      '/api/basket': {
        target: 'http://localhost:9103',
        changeOrigin: true,
      },
      '/api/orders': {
        target: 'http://localhost:9102',
        changeOrigin: true,
      },
    },
  },
})
