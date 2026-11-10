package com.maxtracker.api.dto.metricas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Evolución de un ejercicio: un punto por sesión en la que se hizo, en orden cronológico. */
public record ProgresoEjercicioResponse(
        long ejercicioId,
        String ejercicioNombre,
        List<PuntoProgresoDto> puntos) {

    public ProgresoEjercicioResponse {
        puntos = List.copyOf(puntos);
    }

    /**
     * @param pesoMaxKg       mayor peso de la sesión
     * @param volumenKg       suma de peso × repeticiones de la sesión
     * @param unoRmEstimadoKg mejor 1RM estimado de la sesión (Epley)
     */
    public record PuntoProgresoDto(
            long sesionId,
            LocalDate fecha,
            BigDecimal pesoMaxKg,
            BigDecimal volumenKg,
            BigDecimal unoRmEstimadoKg,
            int series) {
    }
}
