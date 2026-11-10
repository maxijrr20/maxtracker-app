package com.maxtracker.api.ia.gemini;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de la API de Gemini (Google AI Studio).
 *
 * @param apiKey      variable {@code GEMINI_API_KEY}; vacía = análisis deshabilitado (responde 503, la app arranca igual)
 * @param modelo      variable {@code GEMINI_MODEL}; un modelo con plan gratuito (ver ai.google.dev/pricing)
 * @param baseUrl     URL base de la API (debe terminar en "/")
 * @param readTimeout un modelo puede tardar varios segundos en responder
 * @param temperatura baja: se busca un análisis consistente, no creativo
 */
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        @DefaultValue("") String apiKey,
        @DefaultValue("gemini-3.5-flash-lite") String modelo,
        @DefaultValue("https://generativelanguage.googleapis.com/v1beta/") String baseUrl,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("45s") Duration readTimeout,
        @DefaultValue("0.4") double temperatura) {

    public boolean configurada() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Nunca imprime la clave. */
    @Override
    public String toString() {
        return "GeminiProperties[apiKey=" + (configurada() ? "***" : "(vacía)") + ", modelo=" + modelo
                + ", baseUrl=" + baseUrl + "]";
    }
}
