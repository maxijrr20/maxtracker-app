package com.maxtracker.api.dto.metricas;

import java.time.LocalDate;

/** Ejercicio con al menos una serie registrada (selector de la gráfica de progreso). */
public record EjercicioConHistorialDto(
        long ejercicioId,
        Long wgerId,
        String ejercicioNombre,
        String categoria,
        int series,
        LocalDate ultimaFecha) {
}
