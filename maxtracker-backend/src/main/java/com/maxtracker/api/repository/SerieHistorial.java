package com.maxtracker.api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Serie aplanada con los datos de su sesión y ejercicio, para métricas y análisis. Es una proyección (no una
 * entidad): no tiene relaciones LAZY, así que se puede usar fuera de una transacción.
 *
 * <p>{@code numeroSerie}, {@code repeticiones} y {@code rir} son {@code short} porque así están mapeadas las
 * columnas SMALLINT: Hibernate busca un constructor con los tipos exactos de la consulta.
 */
public record SerieHistorial(
        Long sesionId,
        LocalDate fecha,
        Long ejercicioId,
        Long wgerId,
        String ejercicioNombre,
        String categoria,
        short numeroSerie,
        BigDecimal pesoKg,
        short repeticiones,
        Short rir) {

    /** Volumen de la serie: peso × repeticiones (kg). */
    public BigDecimal volumenKg() {
        return pesoKg.multiply(BigDecimal.valueOf(repeticiones));
    }
}
