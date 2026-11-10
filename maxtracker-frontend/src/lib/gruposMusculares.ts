/**
 * Color de identidad por grupo muscular (categoría de wger, ya traducida por el backend). Clases literales para
 * que Tailwind las detecte. La paleta está validada para daltonismo (ver --color-grupo-* en index.css) y el
 * nombre del grupo siempre se muestra junto al color: el color nunca informa solo.
 */
const POR_GRUPO: Record<string, string> = {
  Pecho: 'bg-grupo-pecho',
  Espalda: 'bg-grupo-espalda',
  Piernas: 'bg-grupo-piernas',
  Hombros: 'bg-grupo-hombros',
  Brazos: 'bg-grupo-brazos',
  Abdominales: 'bg-grupo-abdominales',
  Pantorrillas: 'bg-grupo-pantorrillas',
  Cardio: 'bg-grupo-cardio',
}

export function claseGrupo(categoria: string | null | undefined): string {
  return (categoria && POR_GRUPO[categoria]) || 'bg-grupo-otro'
}
