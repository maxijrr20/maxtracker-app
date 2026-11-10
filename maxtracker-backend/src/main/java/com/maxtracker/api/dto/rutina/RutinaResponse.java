package com.maxtracker.api.dto.rutina;

import java.time.Instant;
import java.util.List;

/** Rutina con sus ejercicios en orden. */
public record RutinaResponse(
        long id,
        String nombre,
        String descripcion,
        Instant creadoEn,
        List<RutinaEjercicioResponse> ejercicios) {

    public RutinaResponse {
        ejercicios = List.copyOf(ejercicios);
    }
}
