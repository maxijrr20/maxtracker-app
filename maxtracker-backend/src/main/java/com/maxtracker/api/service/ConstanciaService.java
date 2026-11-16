package com.maxtracker.api.service;

import java.time.YearMonth;
import java.util.Optional;

import com.maxtracker.api.dto.constancia.ConstanciaResponse;
import com.maxtracker.api.dto.constancia.PlanEntrenamientoRequest;
import com.maxtracker.api.dto.constancia.PlanEntrenamientoResponse;

/** Calendario de días entrenados, plan semanal y rachas. */
public interface ConstanciaService {

    /** @param mes {@code null} = el mes actual */
    ConstanciaResponse consultar(long usuarioId, YearMonth mes);

    Optional<PlanEntrenamientoResponse> planVigente(long usuarioId);

    /** El plan nuevo rige desde hoy; los días anteriores conservan el plan que tenían. */
    PlanEntrenamientoResponse guardarPlan(long usuarioId, PlanEntrenamientoRequest request);
}
