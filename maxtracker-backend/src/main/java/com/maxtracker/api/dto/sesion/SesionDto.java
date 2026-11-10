package com.maxtracker.api.dto.sesion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.maxtracker.api.dto.rutina.RutinaEjercicioResponse;

/**
 * Sesión con todas sus series, en el orden en que se registraron.
 *
 * @param finalizadaEn     {@code null} mientras la sesión está en curso
 * @param ejerciciosRutina ejercicios planificados de la rutina en la que se basa la sesión (en orden, con sus
 *                         objetivos), para precargarlos al registrar series; vacía si la sesión no tiene rutina.
 *                         Refleja la rutina actual: si se edita, cambia; si se elimina, queda vacía.
 */
public record SesionDto(
        long id,
        LocalDate fecha,
        Long rutinaId,
        String notas,
        Instant iniciadaEn,
        Instant finalizadaEn,
        boolean finalizada,
        List<RutinaEjercicioResponse> ejerciciosRutina,
        List<SerieDto> series) {

    public SesionDto {
        ejerciciosRutina = List.copyOf(ejerciciosRutina);
        series = List.copyOf(series);
    }
}
