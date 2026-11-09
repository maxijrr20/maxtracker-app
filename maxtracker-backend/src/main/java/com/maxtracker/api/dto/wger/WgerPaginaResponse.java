package com.maxtracker.api.dto.wger;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta paginada genérica de wger ({@code count}, {@code next}, {@code previous}, {@code results}). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WgerPaginaResponse<T>(
        long count,
        String next,
        String previous,
        List<T> results) {
}
