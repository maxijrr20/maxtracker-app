package com.maxtracker.api.dto.constancia;

/** Estado de un día en el calendario de constancia. */
public enum EstadoDia {
    /** Día del plan en que se entrenó. */
    ENTRENADO,
    /** Se entrenó un día que no estaba en el plan (suma a la racha). */
    EXTRA,
    /** Día del plan ya pasado sin entrenar (corta la racha). */
    FALLADO,
    /** Hoy es día del plan y aún no se entrenó (no cuenta como fallado hasta que termine el día). */
    PENDIENTE,
    /** Día futuro del plan. */
    PLANIFICADO,
    /** Día fuera del plan sin entrenar, o sin plan configurado. */
    DESCANSO
}
