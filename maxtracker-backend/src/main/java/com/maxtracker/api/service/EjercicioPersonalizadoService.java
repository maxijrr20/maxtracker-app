package com.maxtracker.api.service;

import java.util.List;

import com.maxtracker.api.dto.ejercicio.CrearEjercicioPersonalizadoRequest;
import com.maxtracker.api.dto.ejercicio.EjercicioPersonalizadoResponse;

/** Ejercicios propios de un usuario, para los que no están en el catálogo de wger. */
public interface EjercicioPersonalizadoService {

    /** @throws com.maxtracker.api.exception.EjercicioPersonalizadoDuplicadoException si ya existe ese nombre */
    EjercicioPersonalizadoResponse crear(long usuarioId, CrearEjercicioPersonalizadoRequest request);

    /** Ordenados por nombre. */
    List<EjercicioPersonalizadoResponse> listar(long usuarioId);

    /**
     * @throws com.maxtracker.api.exception.EjercicioNoEncontradoException si no existe o no es un personalizado suyo
     * @throws com.maxtracker.api.exception.EjercicioEnUsoException       si está en una rutina o tiene series
     */
    void eliminar(long usuarioId, long ejercicioId);
}
