package com.maxtracker.api.dto.metricas;

import java.math.BigDecimal;
import java.time.LocalDate;

/** @param inicioSemana lunes de la semana */
public record VolumenSemanalDto(LocalDate inicioSemana, BigDecimal volumenKg, int sesiones) {
}
