package com.maxtracker.api.dto.sesion;

import java.time.Instant;
import java.time.LocalDate;

/** Sesión en el historial paginado, sin sus series (evita cargarlas para cada fila). */
public record SesionResumenDto(
        long id,
        LocalDate fecha,
        Long rutinaId,
        String notas,
        Instant iniciadaEn,
        Instant finalizadaEn,
        boolean finalizada) {
}
