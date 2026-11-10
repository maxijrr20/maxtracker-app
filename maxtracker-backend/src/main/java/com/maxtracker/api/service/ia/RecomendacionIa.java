package com.maxtracker.api.service.ia;

import java.util.List;

/**
 * Respuesta del entrenador virtual, ya validada y acotada por el adaptador.
 *
 * @param resumen    diagnóstico general en 2–3 frases
 * @param ejercicios análisis de sobrecarga progresiva por ejercicio
 * @param ajusteRir  recomendación sobre el esfuerzo (RIR)
 * @param modelo     modelo que generó la respuesta (trazabilidad)
 */
public record RecomendacionIa(
        String resumen,
        List<EjercicioRecomendacion> ejercicios,
        String ajusteRir,
        List<String> sugerencias,
        String modelo) {

    public RecomendacionIa {
        ejercicios = List.copyOf(ejercicios);
        sugerencias = List.copyOf(sugerencias);
    }

    public record EjercicioRecomendacion(String ejercicio, Tendencia tendencia, String observacion, String sugerencia) {
    }

    /** Tendencia de la carga en el periodo analizado. */
    public enum Tendencia {
        PROGRESA, ESTANCADO, RETROCEDE, SIN_DATOS_SUFICIENTES
    }
}
