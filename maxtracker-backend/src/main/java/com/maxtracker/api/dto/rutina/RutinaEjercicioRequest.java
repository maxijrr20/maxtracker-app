package com.maxtracker.api.dto.rutina;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Un ejercicio de la rutina, en la posición en que aparece en la lista. El ejercicio se indica con
 * <b>exactamente uno</b> de {@code wgerId} (catálogo; la primera vez se guarda una copia local) o
 * {@code ejercicioId} (copia local de wger o ejercicio personalizado propio), igual que al registrar una serie.
 *
 * @param seriesObjetivo       opcional, 1–20
 * @param repeticionesObjetivo opcional, 1–999
 */
public record RutinaEjercicioRequest(
        @Positive(message = "debe ser un ID positivo")
        Long ejercicioId,

        @Positive(message = "debe ser un ID positivo")
        Long wgerId,

        @Min(value = 1, message = "debe estar entre 1 y 20")
        @Max(value = 20, message = "debe estar entre 1 y 20")
        Integer seriesObjetivo,

        @Min(value = 1, message = "debe estar entre 1 y 999")
        @Max(value = 999, message = "debe estar entre 1 y 999")
        Integer repeticionesObjetivo) {

    @JsonIgnore
    @AssertTrue(message = "indica exactamente uno de ejercicioId o wgerId")
    public boolean isEjercicio() {
        return (ejercicioId == null) != (wgerId == null);
    }
}
