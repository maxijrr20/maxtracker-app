package com.maxtracker.api.dto.rutina;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Crea una rutina. {@code ejercicios} es opcional (ausente = rutina vacía) y su orden es el de la lista.
 * Los límites reflejan las columnas: {@code nombre VARCHAR(100)}, {@code descripcion VARCHAR(500)}.
 */
public record CrearRutinaRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "debe tener como máximo 100 caracteres")
        String nombre,

        @Size(max = 500, message = "debe tener como máximo 500 caracteres")
        String descripcion,

        @Size(max = RutinaLimites.MAX_EJERCICIOS, message = "admite como máximo " + RutinaLimites.MAX_EJERCICIOS
                + " ejercicios")
        List<@Valid @NotNull(message = "no puede ser nulo") RutinaEjercicioRequest> ejercicios) {

    public CrearRutinaRequest {
        ejercicios = ejercicios == null ? List.of() : List.copyOf(ejercicios);
    }
}
