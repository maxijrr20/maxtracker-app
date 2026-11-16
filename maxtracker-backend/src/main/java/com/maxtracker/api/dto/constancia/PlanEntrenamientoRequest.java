package com.maxtracker.api.dto.constancia;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Días de la semana que el usuario entrena (ISO-8601: 1 = lunes ... 7 = domingo). La cantidad de días por semana es
 * el tamaño de la lista.
 */
public record PlanEntrenamientoRequest(
        @NotEmpty(message = "elige al menos un día")
        @Size(max = 7, message = "debe tener como máximo 7 días")
        List<@NotNull(message = "no puede ser nulo")
             @Min(value = 1, message = "debe ser un día entre 1 (lunes) y 7 (domingo)")
             @Max(value = 7, message = "debe ser un día entre 1 (lunes) y 7 (domingo)") Integer> dias) {

    @AssertTrue(message = "no puede repetir días")
    public boolean isSinRepetidos() {
        return dias == null || dias.stream().distinct().count() == dias.size();
    }
}
