package com.maxtracker.api.service;

import java.util.List;

import com.maxtracker.api.dto.metricas.EjercicioConHistorialDto;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse;
import com.maxtracker.api.dto.metricas.ResumenMetricasResponse;

/** Métricas de entrenamiento del usuario autenticado, calculadas a partir de sus series. */
public interface MetricasService {

    /**
     * Volumen de la semana y el mes (actual y anterior), volumen de las últimas {@code semanas} semanas
     * y récords personales.
     */
    ResumenMetricasResponse resumen(long usuarioId, int semanas);

    /** Ejercicios con series registradas, del más usado recientemente al más antiguo. */
    List<EjercicioConHistorialDto> ejerciciosConHistorial(long usuarioId);

    /**
     * Evolución de un ejercicio (vacía si aún no tiene series).
     *
     * @throws com.maxtracker.api.exception.EjercicioNoEncontradoException si no existe o es un personalizado
     *                                                                     de otro usuario
     */
    ProgresoEjercicioResponse progreso(long usuarioId, long ejercicioId);
}
