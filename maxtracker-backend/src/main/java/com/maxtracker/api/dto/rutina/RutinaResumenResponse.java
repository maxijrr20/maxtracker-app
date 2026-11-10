package com.maxtracker.api.dto.rutina;

import java.time.Instant;

/** Rutina en el listado, sin el detalle de sus ejercicios. */
public record RutinaResumenResponse(
        long id,
        String nombre,
        String descripcion,
        Instant creadoEn,
        int cantidadEjercicios) {
}
