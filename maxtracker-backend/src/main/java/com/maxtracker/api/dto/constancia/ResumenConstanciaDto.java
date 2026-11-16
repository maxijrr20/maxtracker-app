package com.maxtracker.api.dto.constancia;

/**
 * Conteos de un periodo, hasta hoy inclusive.
 *
 * @param entrenados    días con al menos una serie (del plan o extra)
 * @param fallados      días del plan ya pasados sin entrenar
 * @param cumplimiento  % de días del plan cumplidos (ENTRENADO / (ENTRENADO + FALLADO)), o {@code null} si en el
 *                      periodo no hubo días del plan que evaluar
 */
public record ResumenConstanciaDto(int entrenados, int fallados, Integer cumplimiento) {
}
