/* 
 * Configuration file for vite
 */

import path from "path"
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"), // <-- Back to standard!
    },
  },
  server: {
    proxy: {
    '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
  },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/__tests__/setup.ts'],
  },
})