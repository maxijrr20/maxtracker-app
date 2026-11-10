package com.maxtracker.api.dto.auth;

import java.time.Instant;

import com.maxtracker.api.security.TokenJwt;

/**
 * Respuesta de registro y login. El cliente envía {@code token} en
 * {@code Authorization: Bearer <token>} hasta {@code expiraEn}.
 */
public record AuthResponse(String token, String tipo, Instant expiraEn, String email) {

    public static final String TIPO_BEARER = "Bearer";

    public static AuthResponse bearer(TokenJwt token, String email) {
        return new AuthResponse(token.valor(), TIPO_BEARER, token.expiraEn(), email);
    }
}
