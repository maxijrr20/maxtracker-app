package com.maxtracker.api.dto.sesion;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Reemplaza los datos editables de una sesión ({@code PUT}): una {@code notas} ausente las borra.
 * Se permite también en sesiones finalizadas (corregir la fecha o agregar comentarios después).
 */
public record ActualizarSesionRequest(
        @NotNull(message = "es obligatoria")
        LocalDate fecha,

        @Size(max = 1000, message = "debe tener como máximo 1000 caracteres")
        String notas) {
}
