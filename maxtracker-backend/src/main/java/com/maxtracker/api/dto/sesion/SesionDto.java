package com.maxtracker.api.dto.sesion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Sesión con todas sus series, en el orden en que se registraron.
 *
 * @param finalizadaEn {@code null} mientras la sesión está en curso
 */
public record SesionDto(
        long id,
        LocalDate fecha,
        Long rutinaId,
        String notas,
        Instant iniciadaEn,
        Instant finalizadaEn,
        boolean finalizada,
        List<SerieDto> series) {
}
