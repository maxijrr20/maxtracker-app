package com.maxtracker.api.dto.rutina;

/**
 * Ejercicio de una rutina.
 *
 * @param orden           posición en la rutina (1..n)
 * @param ejercicioId     ID local (sirve para registrar series con {@code ejercicioId})
 * @param wgerId          ID en wger, o {@code null} si es un ejercicio personalizado
 * @param ejercicioNombre nombre guardado en la copia local
 */
public record RutinaEjercicioResponse(
        int orden,
        long ejercicioId,
        Long wgerId,
        String ejercicioNombre,
        String categoria,
        Integer seriesObjetivo,
        Integer repeticionesObjetivo) {
}
