package com.maxtracker.api.service.ia;

/**
 * Preferencias del usuario para el análisis y su credencial opcional.
 *
 * @param enfoque         objetivo del entrenamiento, o {@code null} para un análisis general
 * @param unidadPeso      unidad en la que el modelo debe expresar las cargas (los datos siempre van en kg)
 * @param claveApiUsuario clave personal del proveedor de IA, o {@code null} para usar la del servidor. Nunca forma
 *                        parte del prompt ni se registra
 */
public record OpcionesAnalisis(Enfoque enfoque, UnidadPeso unidadPeso, String claveApiUsuario) {

    public static final OpcionesAnalisis POR_DEFECTO = new OpcionesAnalisis(null, UnidadPeso.KG, null);

    public OpcionesAnalisis {
        unidadPeso = unidadPeso == null ? UnidadPeso.KG : unidadPeso;
        claveApiUsuario = claveApiUsuario == null || claveApiUsuario.isBlank() ? null : claveApiUsuario.strip();
    }

    public boolean tieneClaveUsuario() {
        return claveApiUsuario != null;
    }

    /** Nunca imprime la clave. */
    @Override
    public String toString() {
        return "OpcionesAnalisis[enfoque=" + enfoque + ", unidadPeso=" + unidadPeso + ", claveApiUsuario="
                + (tieneClaveUsuario() ? "***" : "(servidor)") + "]";
    }

    public enum Enfoque {
        /** Cargas altas, pocas repeticiones (1–6), descansos largos. */
        FUERZA,
        /** Volumen moderado-alto, 6–12 repeticiones cerca del fallo. */
        HIPERTROFIA,
        /** Sostener el nivel con menos volumen y esfuerzo. */
        MANTENIMIENTO
    }

    public enum UnidadPeso {
        KG, LB
    }
}
