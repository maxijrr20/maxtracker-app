import { useId } from 'react'

import { cx } from '../../lib/cx'
import { Input, type InputProps } from '../atoms/Input'

export interface FormFieldProps extends Omit<InputProps, 'invalido'> {
  etiqueta: string
  ayuda?: string
  error?: string
}

/** Label siempre visible (nunca solo placeholder) + Input + ayuda/error asociados para lectores de pantalla. */
export function FormField({ etiqueta, ayuda, error, id, ...resto }: FormFieldProps) {
  const idGenerado = useId()
  const idCampo = id ?? idGenerado
  const idDescripcion = `${idCampo}-descripcion`
  const descripcion = error ?? ayuda

  return (
    <div className="flex flex-col gap-2">
      <label htmlFor={idCampo} className="text-sm font-medium text-texto">
        {etiqueta}
      </label>
      <Input
        id={idCampo}
        invalido={Boolean(error)}
        aria-describedby={descripcion ? idDescripcion : undefined}
        {...resto}
      />
      {descripcion && (
        <p id={idDescripcion} className={cx('text-sm', error ? 'text-peligro' : 'text-texto-suave')}>
          {descripcion}
        </p>
      )}
    </div>
  )
}
