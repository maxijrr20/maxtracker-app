package com.maxtracker.api.dto.sesion;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Una serie realizada. El ejercicio se indica con <b>exactamente uno</b> de:
 * <ul>
 *   <li>{@code wgerId}: ID del catálogo ({@code GET /api/v1/ejercicios}); la primera vez se guarda una copia local.</li>
 *   <li>{@code ejercicioId}: ID local (el que devuelve una serie ya registrada, o un ejercicio personalizado).</li>
 * </ul>
 * Los límites reflejan las columnas: {@code peso_kg NUMERIC(6,2)}, {@code repeticiones}/{@code rir SMALLINT}.
 */
public record RegistrarSerieRequest(
        @Positive(message = "debe ser un ID positivo")
        Long ejercicioId,

        @Positive(message = "debe ser un ID positivo")
        Long wgerId,

        @NotNull(message = "es obligatorio")
        @DecimalMin(value = "0", message = "debe ser mayor o igual a 0")
        @Digits(integer = 4, fraction = 2, message = "admite hasta 4 enteros y 2 decimales")
        BigDecimal pesoKg,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser al menos 1")
        @Max(value = 999, message = "debe ser como máximo 999")
        Integer repeticiones,

        @Min(value = 0, message = "debe estar entre 0 y 10")
        @Max(value = 10, message = "debe estar entre 0 y 10")
        Integer rir) {

    @JsonIgnore
    @AssertTrue(message = "indica exactamente uno de ejercicioId o wgerId")
    public boolean isEjercicio() {
        return (ejercicioId == null) != (wgerId == null);
    }
}
