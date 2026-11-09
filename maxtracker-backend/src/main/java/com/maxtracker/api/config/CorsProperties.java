package com.maxtracker.api.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Orígenes del frontend autorizados a llamar a la API.
 * Se configuran con {@code CORS_ALLOWED_ORIGINS} (lista separada por comas).
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(@DefaultValue("http://localhost:5173") List<String> allowedOrigins) {
}
