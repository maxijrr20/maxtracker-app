package com.maxtracker.api.service;

import java.util.List;

import com.maxtracker.api.dto.rutina.ActualizarRutinaRequest;
import com.maxtracker.api.dto.rutina.CrearRutinaRequest;
import com.maxtracker.api.dto.rutina.RutinaResponse;
import com.maxtracker.api.dto.rutina.RutinaResumenResponse;

/**
 * Rutinas (plantillas de entrenamiento) del usuario. Todas las operaciones reciben el {@code usuarioId}
 * autenticado: una rutina de otro usuario se trata como inexistente
 * ({@link com.maxtracker.api.exception.RecursoNoEncontradoException}, 404).
 *
 * <p>Crear y actualizar pueden lanzar {@link com.maxtracker.api.exception.EjercicioNoEncontradoException} (404)
 * o {@link com.maxtracker.api.exception.WgerApiException} (502) al resolver los ejercicios.
 */
public interface RutinaService {

    RutinaResponse crear(long usuarioId, CrearRutinaRequest request);

    /** Ordenadas por nombre. */
    List<RutinaResumenResponse> listar(long usuarioId);

    RutinaResponse obtener(long usuarioId, long rutinaId);

    /** Reemplaza nombre, descripción y la lista completa de ejercicios. */
    RutinaResponse actualizar(long usuarioId, long rutinaId, ActualizarRutinaRequest request);

    /** Las sesiones basadas en la rutina se conservan, con {@code rutinaId = null}. */
    void eliminar(long usuarioId, long rutinaId);
}
