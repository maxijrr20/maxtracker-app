package com.maxtracker.api.dto;

import java.util.List;

/** Página de resultados (la numeración de páginas empieza en 0). */
public record PaginaDto<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas) {
}
