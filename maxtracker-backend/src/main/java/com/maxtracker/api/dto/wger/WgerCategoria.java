package com.maxtracker.api.dto.wger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WgerCategoria(int id, String name) {
}
