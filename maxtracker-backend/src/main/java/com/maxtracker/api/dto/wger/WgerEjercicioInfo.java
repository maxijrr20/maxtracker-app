package com.maxtracker.api.dto.wger;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Ejercicio tal como lo devuelve {@code GET /api/v2/exerciseinfo/}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WgerEjercicioInfo(
        long id,
        String uuid,
        WgerCategoria category,
        List<WgerMusculo> muscles,
        @JsonProperty("muscles_secondary") List<WgerMusculo> musclesSecondary,
        List<WgerEquipamiento> equipment,
        List<WgerTraduccion> translations,
        List<WgerImagen> images) {
}
