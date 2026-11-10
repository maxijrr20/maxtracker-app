package com.maxtracker.api.service;

import com.maxtracker.api.dto.auth.AuthResponse;
import com.maxtracker.api.dto.auth.LoginRequest;
import com.maxtracker.api.dto.auth.RegisterRequest;

/** Registro y login con emisión de JWT. */
public interface AuthService {

    /**
     * Crea la cuenta (contraseña con BCrypt) y devuelve un token para no exigir un login aparte.
     *
     * @throws com.maxtracker.api.exception.EmailYaRegistradoException     si el email ya existe
     * @throws com.maxtracker.api.exception.RegistroDeshabilitadoException si el registro está desactivado
     */
    AuthResponse registrar(RegisterRequest request);

    /**
     * @throws org.springframework.security.core.AuthenticationException si las credenciales no son válidas
     */
    AuthResponse login(LoginRequest request);
}
