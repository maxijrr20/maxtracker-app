package com.maxtracker.api.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Propiedades tipadas para la emisión y validación de los JWT.
 *
 * @param secret     clave HMAC-SHA (variable {@code JWT_SECRET}); al menos 32 bytes, se valida al arrancar
 * @param expiracion vigencia del token desde su emisión (variable {@code JWT_EXPIRATION}, formato ISO-8601)
 * @param emisor     valor del claim {@code iss}; los tokens con otro emisor se rechazan
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotNull @DefaultValue("PT12H") Duration expiracion,
        @NotBlank @DefaultValue("maxtracker-api") String emisor) {

    /** Evita que el secreto aparezca en logs si alguien imprime las propiedades. */
    @Override
    public String toString() {
        return "JwtProperties[secret=***, expiracion=" + expiracion + ", emisor=" + emisor + "]";
    }
}
