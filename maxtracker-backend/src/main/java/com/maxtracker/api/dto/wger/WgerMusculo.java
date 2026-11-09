package com.maxtracker.api.dto.wger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Músculo de wger: {@code name} es el nombre latino y {@code name_en} el nombre común en inglés. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WgerMusculo(
        int id,
        String name,
        @JsonProperty("name_en") String nameEn) {
}
