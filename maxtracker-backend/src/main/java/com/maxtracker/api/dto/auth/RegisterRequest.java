package com.maxtracker.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear una cuenta. La contraseña admite hasta 72 caracteres porque BCrypt
 * ignora lo que excede los 72 bytes.
 */
public record RegisterRequest(
        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un email válido")
        @Size(max = 255, message = "debe tener como máximo 255 caracteres")
        String email,

        @NotBlank(message = "es obligatoria")
        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password) {

    /** Quita espacios de los extremos del email (copiar y pegar) antes de validar. */
    public RegisterRequest {
        email = email == null ? null : email.strip();
    }

    /** Nunca imprime la contraseña (logs, depuración). */
    @Override
    public String toString() {
        return "RegisterRequest[email=" + email + ", password=***]";
    }
}
