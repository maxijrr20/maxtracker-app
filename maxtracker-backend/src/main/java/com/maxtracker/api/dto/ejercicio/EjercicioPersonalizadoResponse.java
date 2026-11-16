package com.maxtracker.api.dto.ejercicio;

/**
 * Ejercicio propio del usuario. {@code ejercicioId} es el que se envía al registrar series o armar rutinas
 * (en lugar de {@code wgerId}).
 */
public record EjercicioPersonalizadoResponse(long ejercicioId, String nombre, String categoria) {
}
