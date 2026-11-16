package com.maxtracker.api.dto.constancia;

import java.util.List;

/**
 * Calendario de un mes y estadísticas de constancia.
 *
 * @param mes          mes consultado, "AAAA-MM"
 * @param dias         un elemento por día del mes, en orden
 * @param resumenMes   conteos del mes (hasta hoy)
 * @param resumenTotal conteos de todo el historial (hasta hoy)
 * @param rachaActual  días entrenados seguidos sin fallar un día del plan; {@code null} sin plan configurado
 * @param mejorRacha   la racha más larga lograda; {@code null} sin plan configurado
 * @param plan         plan vigente, o {@code null} si el usuario aún no configuró uno
 */
public record ConstanciaResponse(
        String mes,
        List<DiaConstanciaDto> dias,
        ResumenConstanciaDto resumenMes,
        ResumenConstanciaDto resumenTotal,
        Integer rachaActual,
        Integer mejorRacha,
        PlanEntrenamientoResponse plan) {
}
