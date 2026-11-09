package com.maxtracker.api.dto.wger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Traducción de un ejercicio; {@code description} viene en HTML. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WgerTraduccion(
        long id,
        String name,
        String description,
        int language) {
}
