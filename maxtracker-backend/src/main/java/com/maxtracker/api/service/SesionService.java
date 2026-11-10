package com.maxtracker.api.service;

import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.dto.sesion.ActualizarSesionRequest;
import com.maxtracker.api.dto.sesion.IniciarSesionRequest;
import com.maxtracker.api.dto.sesion.RegistrarSerieRequest;
import com.maxtracker.api.dto.sesion.SerieDto;
import com.maxtracker.api.dto.sesion.SesionDto;
import com.maxtracker.api.dto.sesion.SesionResumenDto;

/**
 * Sesiones de entrenamiento y sus series. Todas las operaciones reciben el {@code usuarioId} autenticado:
 * una sesión de otro usuario se trata como inexistente
 * ({@link com.maxtracker.api.exception.RecursoNoEncontradoException}, 404).
 */
public interface SesionService {

    SesionDto iniciar(long usuarioId, IniciarSesionRequest request);

    /** Historial de la más reciente a la más antigua. */
    PaginaDto<SesionResumenDto> listar(long usuarioId, int pagina, int tamanio);

    SesionDto obtener(long usuarioId, long sesionId);

    SesionDto actualizar(long usuarioId, long sesionId, ActualizarSesionRequest request);

    void eliminar(long usuarioId, long sesionId);

    /** @throws com.maxtracker.api.exception.SesionFinalizadaException si ya estaba finalizada */
    SesionDto finalizar(long usuarioId, long sesionId);

    /** @throws com.maxtracker.api.exception.SesionFinalizadaException si la sesión está finalizada */
    SerieDto registrarSerie(long usuarioId, long sesionId, RegistrarSerieRequest request);

    /** @throws com.maxtracker.api.exception.SesionFinalizadaException si la sesión está finalizada */
    void eliminarSerie(long usuarioId, long sesionId, long serieId);
}
