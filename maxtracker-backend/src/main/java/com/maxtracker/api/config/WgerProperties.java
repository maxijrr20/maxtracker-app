package com.maxtracker.api.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades tipadas para la integración con la API pública de wger.
 *
 * @param baseUrl        URL base de la API (debe terminar en "/")
 * @param idiomaEspanol  ID de idioma de wger para español (4)
 * @param idiomaRespaldo ID de idioma de respaldo cuando no hay traducción al español (2 = inglés)
 * @param connectTimeout tiempo máximo para establecer la conexión
 * @param readTimeout    tiempo máximo de espera de la respuesta
 */
@ConfigurationProperties(prefix = "wger")
public record WgerProperties(
        @DefaultValue("https://wger.de/api/v2/") String baseUrl,
        @DefaultValue("4") int idiomaEspanol,
        @DefaultValue("2") int idiomaRespaldo,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("10s") Duration readTimeout) {
}
