package com.maxtracker.api.ia.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Parte de la respuesta de {@code models/{modelo}:generateContent} que se usa (el resto se ignora). */
@JsonIgnoreProperties(ignoreUnknown = true)
record GeminiRespuesta(List<Candidato> candidates, PromptFeedback promptFeedback, String modelVersion) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidato(Contenido content, String finishReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Contenido(List<Parte> parts) {
    }

    /** @param thought {@code true} en las partes de "razonamiento" de los modelos con thinking: no son la respuesta */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Parte(String text, Boolean thought) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PromptFeedback(String blockReason) {
    }
}
