import { defineConfig, minimal2023Preset } from '@vite-pwa/assets-generator/config'

/**
 * Genera los íconos de la PWA desde public/logo.svg: `npm run generate-pwa-assets`.
 * Los PNG resultantes se versionan en public/ (no se regeneran en cada build).
 */
export default defineConfig({
  headLinkOptions: { preset: '2023' },
  preset: {
    ...minimal2023Preset,
    // Relleno con el color de fondo de la app en lugar del blanco por defecto.
    maskable: { ...minimal2023Preset.maskable, resizeOptions: { background: '#09090b' } },
    apple: { ...minimal2023Preset.apple, resizeOptions: { background: '#09090b' } },
  },
  images: ['public/logo.svg'],
})
