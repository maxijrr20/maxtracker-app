package com.maxtracker.api.dto;

import java.util.List;

/**
 * Ejercicio del catálogo expuesto por la API de MaxTracker.
 *
 * @param idioma "es" si el nombre está en español, "en" si se usó el respaldo en inglés,
 *               "otro" si solo existía otra traducción, o {@code null} si no había ninguna
 */
public record EjercicioDto(
        long id,
        String nombre,
        String descripcion,
        String idioma,
        Integer categoriaId,
        String categoria,
        List<String> musculosPrincipales,
        List<String> musculosSecundarios,
        List<String> equipamiento,
        String imagenUrl) {
}
