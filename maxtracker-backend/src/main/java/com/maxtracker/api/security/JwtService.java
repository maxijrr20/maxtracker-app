package com.maxtracker.api.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.maxtracker.api.config.JwtProperties;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

/**
 * Emite y valida JWT firmados con HMAC-SHA256. El {@code sub} es el email del usuario;
 * se verifican firma, expiración y emisor.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey jwtSigningKey;
    private final JwtProperties properties;
    private final Clock clock;

    public TokenJwt generarToken(UserDetails usuario) {
        Instant ahora = clock.instant();
        Instant expiraEn = ahora.plus(properties.expiracion());
        String token = Jwts.builder()
                .issuer(properties.emisor())
                .subject(usuario.getUsername())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiraEn))
                .signWith(jwtSigningKey, Jwts.SIG.HS256)
                .compact();
        return new TokenJwt(token, expiraEn);
    }

    /**
     * @return el email ({@code sub}) si el token es válido; vacío si está mal formado, vencido,
     *         tiene otra firma u otro emisor
     */
    public Optional<String> extraerEmail(String token) {
        try {
            String email = Jwts.parser()
                    .verifyWith(jwtSigningKey)
                    .requireIssuer(properties.emisor())
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(email).filter(valor -> !valor.isBlank());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT rechazado: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
