package com.maxtracker.api.dto.analisis;

import java.time.Instant;
import java.util.List;

/**
 * Recomendaciones del entrenador virtual.
 *
 * @param alcance             qué se analizó ("Rutina Empuje", "Sesión del 2026-11-10", "Últimos 28 días")
 * @param ejercicios          sobrecarga progresiva por ejercicio
 * @param ajusteRir           recomendación sobre el esfuerzo (RIR)
 * @param seriesAnalizadas    cantidad de series enviadas al modelo
 * @param modelo              modelo de IA que generó el análisis
 */
public record AnalisisRutinaResponse(
        String alcance,
        String resumen,
        List<EjercicioAnalisisDto> ejercicios,
        String ajusteRir,
        List<String> sugerencias,
        int dias,
        int seriesAnalizadas,
        String modelo,
        Instant generadoEn) {

    public AnalisisRutinaResponse {
        ejercicios = List.copyOf(ejercicios);
        sugerencias = List.copyOf(sugerencias);
    }

    /** @param tendencia PROGRESA, ESTANCADO, RETROCEDE o SIN_DATOS_SUFICIENTES */
    public record EjercicioAnalisisDto(String ejercicio, String tendencia, String observacion, String sugerencia) {
    }
}
