package com.maxtracker.api.dto.rutina;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Reemplaza la rutina completa ({@code PUT}): una {@code descripcion} ausente la borra y {@code ejercicios}
 * reemplaza la lista entera (ausente o vacía = rutina sin ejercicios). Las sesiones ya registradas no cambian.
 */
public record ActualizarRutinaRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "debe tener como máximo 100 caracteres")
        String nombre,

        @Size(max = 500, message = "debe tener como máximo 500 caracteres")
        String descripcion,

        @Size(max = RutinaLimites.MAX_EJERCICIOS, message = "admite como máximo " + RutinaLimites.MAX_EJERCICIOS
                + " ejercicios")
        List<@Valid @NotNull(message = "no puede ser nulo") RutinaEjercicioRequest> ejercicios) {

    public ActualizarRutinaRequest {
        ejercicios = ejercicios == null ? List.of() : List.copyOf(ejercicios);
    }
}
