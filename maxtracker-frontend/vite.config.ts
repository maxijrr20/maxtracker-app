import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

const COLOR_FONDO = '#09090b' // zinc-950, igual que bg-fondo

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      // "prompt": la app avisa que hay una versión nueva y el usuario decide cuándo recargar,
      // así una actualización nunca recarga la página en medio de una serie.
      registerType: 'prompt',
      includeAssets: ['favicon.ico', 'favicon.svg', 'apple-touch-icon-180x180.png'],
      manifest: {
        name: 'MaxTracker',
        short_name: 'MaxTracker',
        description: 'Registra tus entrenamientos, series y descansos, y sigue tu progreso.',
        lang: 'es',
        start_url: '/',
        scope: '/',
        display: 'standalone',
        orientation: 'portrait',
        theme_color: COLOR_FONDO,
        background_color: COLOR_FONDO,
        categories: ['health', 'fitness', 'sports'],
        icons: [
          { src: 'pwa-64x64.png', sizes: '64x64', type: 'image/png' },
          { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' },
          { src: 'maskable-icon-512x512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // Precachea el "app shell": la app abre sin conexión. Las llamadas a la API (otro origen)
        // no se cachean: los datos de entrenamiento siempre van a la red.
        globPatterns: ['**/*.{js,css,html,svg,png,ico,woff2}'],
        navigateFallback: '/index.html',
        cleanupOutdatedCaches: true,
      },
    }),
  ],
  server: {
    // El backend solo acepta este origen por CORS (CORS_ALLOWED_ORIGINS=http://localhost:5173).
    port: 5173,
    strictPort: true,
  },
})
