package com.maxtracker.api.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.dto.wger.WgerEjercicioInfo;
import com.maxtracker.api.dto.wger.WgerPaginaResponse;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.exception.WgerApiException;
import com.maxtracker.api.mapper.WgerEjercicioMapper;
import com.maxtracker.api.service.EjercicioService;

/**
 * Implementación del catálogo basada en {@code GET /exerciseinfo/} de wger.
 * El nombre de cada ejercicio se resuelve en español con respaldo a inglés (ver {@link WgerEjercicioMapper}).
 */
@Service
public class WgerExerciseService implements EjercicioService {

    private static final Logger log = LoggerFactory.getLogger(WgerExerciseService.class);
    private static final ParameterizedTypeReference<WgerPaginaResponse<WgerEjercicioInfo>> TIPO_PAGINA =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient wgerRestClient;
    private final WgerEjercicioMapper mapper;

    public WgerExerciseService(@Qualifier("wgerRestClient") RestClient wgerRestClient, WgerEjercicioMapper mapper) {
        this.wgerRestClient = wgerRestClient;
        this.mapper = mapper;
    }

    @Override
    @Cacheable(cacheNames = "ejercicios", key = "#pagina + '-' + #tamanio + '-' + #categoria")
    public PaginaDto<EjercicioDto> listar(int pagina, int tamanio, Integer categoria) {
        WgerPaginaResponse<WgerEjercicioInfo> respuesta;
        try {
            respuesta = wgerRestClient.get()
                    .uri(uri -> {
                        uri.path("exerciseinfo/")
                                .queryParam("limit", tamanio)
                                .queryParam("offset", (long) pagina * tamanio);
                        if (categoria != null) {
                            uri.queryParam("category", categoria);
                        }
                        return uri.build();
                    })
                    .retrieve()
                    .body(TIPO_PAGINA);
        } catch (RestClientException e) {
            log.warn("Error al consultar el catálogo de wger (pagina={}, tamanio={}, categoria={})",
                    pagina, tamanio, categoria, e);
            throw new WgerApiException("No se pudo obtener el catálogo de ejercicios desde wger", e);
        }

        if (respuesta == null) {
            throw new WgerApiException("wger devolvió una respuesta vacía");
        }

        List<EjercicioDto> contenido = respuesta.results() == null
                ? List.of()
                : respuesta.results().stream().map(mapper::toDto).toList();
        int totalPaginas = (int) ((respuesta.count() + tamanio - 1) / tamanio);

        return new PaginaDto<>(contenido, pagina, tamanio, respuesta.count(), totalPaginas);
    }

    @Override
    @Cacheable(cacheNames = "ejercicio", key = "#id")
    public EjercicioDto obtenerPorId(long id) {
        WgerEjercicioInfo info;
        try {
            info = wgerRestClient.get()
                    .uri("exerciseinfo/{id}/", id)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, response) -> {
                        throw new EjercicioNoEncontradoException(id);
                    })
                    .body(WgerEjercicioInfo.class);
        } catch (RestClientException e) {
            log.warn("Error al consultar el ejercicio {} en wger", id, e);
            throw new WgerApiException("No se pudo obtener el ejercicio desde wger", e);
        }

        if (info == null) {
            throw new WgerApiException("wger devolvió una respuesta vacía");
        }
        return mapper.toDto(info);
    }
}
