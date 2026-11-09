package com.maxtracker.api.dto.wger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WgerImagen(
        long id,
        String image,
        @JsonProperty("is_main") boolean isMain) {
}
