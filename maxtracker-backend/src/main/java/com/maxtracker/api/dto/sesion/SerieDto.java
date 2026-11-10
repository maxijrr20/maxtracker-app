package com.maxtracker.api.dto.sesion;

import java.math.BigDecimal;

/**
 * Serie registrada.
 *
 * @param ejercicioId     ID local del ejercicio (sirve para registrar más series sin pasar por wger)
 * @param wgerId          ID en wger, o {@code null} si es un ejercicio personalizado
 * @param ejercicioNombre nombre guardado en la copia local (visible aunque wger esté caído)
 * @param numeroSerie     correlativo por ejercicio dentro de la sesión (1, 2, 3...)
 */
public record SerieDto(
        long id,
        long ejercicioId,
        Long wgerId,
        String ejercicioNombre,
        int numeroSerie,
        BigDecimal pesoKg,
        int repeticiones,
        Integer rir) {
}
