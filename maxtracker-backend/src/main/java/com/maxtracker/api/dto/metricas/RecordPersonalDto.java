package com.maxtracker.api.dto.metricas;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Récord personal de un ejercicio.
 *
 * @param pesoMaxKg       mayor peso levantado (a igual peso, gana la serie con más repeticiones)
 * @param repeticiones    repeticiones de esa serie
 * @param fecha           primera vez que se logró
 * @param unoRmEstimadoKg mejor 1RM estimado de todo el historial (fórmula de Epley)
 */
public record RecordPersonalDto(
        long ejercicioId,
        Long wgerId,
        String ejercicioNombre,
        String categoria,
        BigDecimal pesoMaxKg,
        int repeticiones,
        LocalDate fecha,
        BigDecimal unoRmEstimadoKg) {
}
