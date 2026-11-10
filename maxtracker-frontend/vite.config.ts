import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // El backend solo acepta este origen por CORS (CORS_ALLOWED_ORIGINS=http://localhost:5173).
    port: 5173,
    strictPort: true,
  },
})
