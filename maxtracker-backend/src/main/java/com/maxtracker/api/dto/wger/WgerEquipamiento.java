package com.maxtracker.api.dto.wger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WgerEquipamiento(int id, String name) {
}
