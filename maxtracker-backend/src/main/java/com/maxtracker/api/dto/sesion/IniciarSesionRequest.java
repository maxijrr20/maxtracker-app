package com.maxtracker.api.dto.sesion;

import java.time.LocalDate;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Inicia una sesión de entrenamiento. Todos los campos son opcionales: sin {@code fecha} se usa la de hoy
 * en la zona horaria de la app ({@code APP_ZONA_HORARIA}).
 *
 * @param fecha    día del entrenamiento (permite registrar una sesión pasada)
 * @param rutinaId rutina del usuario en la que se basa la sesión
 * @param notas    texto libre
 */
public record IniciarSesionRequest(
        LocalDate fecha,

        @Positive(message = "debe ser un ID positivo")
        Long rutinaId,

        @Size(max = 1000, message = "debe tener como máximo 1000 caracteres")
        String notas) {
}
