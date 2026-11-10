package com.maxtracker.api.service.ia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos que se envían al modelo. Solo contiene datos de entrenamiento (ejercicios, fechas, cargas): nunca
 * el email ni otros datos de la cuenta, porque el proveedor puede usarlos para mejorar sus productos.
 *
 * @param alcance descripción legible de qué se analiza ("Rutina Empuje", "Sesión del 2026-11-10"...)
 */
public record ContextoAnalisis(String alcance, LocalDate hoy, int dias, List<EjercicioContexto> ejercicios) {

    public ContextoAnalisis {
        ejercicios = List.copyOf(ejercicios);
    }

    /**
     * @param seriesObjetivo       objetivo de la rutina, si se conoce
     * @param unoRmInicialKg       mejor 1RM estimado (Epley) de la primera sesión del periodo
     * @param unoRmFinalKg         mejor 1RM estimado de la última sesión del periodo
     */
    public record EjercicioContexto(
            String nombre,
            String categoria,
            Integer seriesObjetivo,
            Integer repeticionesObjetivo,
            BigDecimal unoRmInicialKg,
            BigDecimal unoRmFinalKg,
            List<SesionContexto> sesiones) {

        public EjercicioContexto {
            sesiones = List.copyOf(sesiones);
        }
    }

    public record SesionContexto(LocalDate fecha, List<SerieContexto> series) {

        public SesionContexto {
            series = List.copyOf(series);
        }
    }

    public record SerieContexto(BigDecimal pesoKg, int repeticiones, Integer rir) {
    }
}
