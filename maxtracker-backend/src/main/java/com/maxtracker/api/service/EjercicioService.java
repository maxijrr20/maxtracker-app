package com.maxtracker.api.service;

import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.dto.PaginaDto;

/** Catálogo de ejercicios. */
public interface EjercicioService {

    /**
     * @param pagina    número de página, empezando en 0
     * @param tamanio   elementos por página (1–100)
     * @param categoria ID de categoría de wger, o {@code null} para no filtrar
     */
    PaginaDto<EjercicioDto> listar(int pagina, int tamanio, Integer categoria);

    EjercicioDto obtenerPorId(long id);
}
