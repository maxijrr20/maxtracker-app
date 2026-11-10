package com.maxtracker.api.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.security.Keys;

/**
 * Beans de soporte para JWT: la clave de firma (validada al arrancar, así un {@code JWT_SECRET}
 * débil impide el despliegue en lugar de fallar en el primer login). El reloj está en {@link ClockConfig}.
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, AuthProperties.class})
public class JwtConfig {

    /** HS256 exige una clave de al menos 256 bits. */
    static final int BYTES_MINIMOS_SECRETO = 32;

    @Bean
    public SecretKey jwtSigningKey(JwtProperties properties) {
        byte[] secreto = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secreto.length < BYTES_MINIMOS_SECRETO) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + BYTES_MINIMOS_SECRETO
                    + " bytes (tiene " + secreto.length + ")");
        }
        return Keys.hmacShaKeyFor(secreto);
    }
}
