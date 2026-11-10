/** La app se abrió instalada (pantalla de inicio / app de escritorio) y no en una pestaña del navegador. */
export function estaInstalada(): boolean {
  try {
    const standaloneIos = (navigator as Navigator & { standalone?: boolean }).standalone === true
    return standaloneIos || window.matchMedia('(display-mode: standalone)').matches
  } catch {
    return false
  }
}
