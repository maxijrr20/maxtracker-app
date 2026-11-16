package com.maxtracker.api.dto.constancia;

import java.time.LocalDate;
import java.util.List;

/**
 * Plan semanal vigente.
 *
 * @param dias          días de la semana ISO-8601, ordenados (1 = lunes ... 7 = domingo)
 * @param vigenteDesde  desde cuándo rige (los días anteriores se evalúan con el plan que estaba vigente entonces)
 */
public record PlanEntrenamientoResponse(List<Integer> dias, LocalDate vigenteDesde) {
}
