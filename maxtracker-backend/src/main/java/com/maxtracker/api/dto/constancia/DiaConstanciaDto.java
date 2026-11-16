package com.maxtracker.api.dto.constancia;

import java.time.LocalDate;

public record DiaConstanciaDto(LocalDate fecha, EstadoDia estado) {
}
