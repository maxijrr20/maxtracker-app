package com.maxtracker.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciales para obtener un JWT. No se valida el largo mínimo: eso solo aplica al registrarse. */
public record LoginRequest(
        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un email válido")
        String email,

        @NotBlank(message = "es obligatoria")
        @Size(max = 72, message = "debe tener como máximo 72 caracteres")
        String password) {

    /** Quita espacios de los extremos del email (copiar y pegar) antes de validar. */
    public LoginRequest {
        email = email == null ? null : email.strip();
    }

    /** Nunca imprime la contraseña (logs, depuración). */
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
