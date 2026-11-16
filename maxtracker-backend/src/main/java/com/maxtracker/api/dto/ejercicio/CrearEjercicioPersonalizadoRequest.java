package com.maxtracker.api.dto.ejercicio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Ejercicio propio del usuario (no está en el catálogo de wger).
 *
 * @param nombre    1–150 caracteres; único por usuario sin distinguir mayúsculas
 * @param categoria grupo muscular, uno de los del catálogo (así el ejercicio se filtra y colorea igual que los de
 *                  wger), o {@code null} si no aplica
 */
public record CrearEjercicioPersonalizadoRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 150, message = "debe tener como máximo 150 caracteres") String nombre,
        @Pattern(regexp = "Pecho|Espalda|Piernas|Hombros|Brazos|Abdominales|Pantorrillas|Cardio",
                message = "debe ser un grupo muscular del catálogo") String categoria) {
}
