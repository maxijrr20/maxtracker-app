package com.maxtracker.api.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.dto.sesion.SerieDto;
import com.maxtracker.api.dto.sesion.SesionDto;
import com.maxtracker.api.dto.sesion.SesionResumenDto;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Rutina;
import com.maxtracker.api.model.RutinaEjercicio;
import com.maxtracker.api.model.SerieRegistro;
import com.maxtracker.api.model.SesionEntrenamiento;

import lombok.RequiredArgsConstructor;

/** Entidades de sesión → DTOs. Debe usarse dentro de la transacción (las relaciones son LAZY). */
@Component
@RequiredArgsConstructor
public class SesionMapper {

    private final RutinaMapper rutinaMapper;

    /**
     * @param planRutina ejercicios de la rutina de la sesión (vacía si no tiene); se reciben aparte para que el
     *                   servicio decida cómo cargarlos (en una sola consulta)
     */
    public SesionDto aDto(SesionEntrenamiento sesion, List<RutinaEjercicio> planRutina) {
        return new SesionDto(sesion.getId(), sesion.getFecha(), rutinaId(sesion), sesion.getNotas(),
                sesion.getIniciadaEn(), sesion.getFinalizadaEn(), sesion.estaFinalizada(),
                rutinaMapper.aResponses(planRutina),
                sesion.getSeries().stream().map(this::aDto).toList());
    }

    public SesionResumenDto aResumen(SesionEntrenamiento sesion) {
        return new SesionResumenDto(sesion.getId(), sesion.getFecha(), rutinaId(sesion), sesion.getNotas(),
                sesion.getIniciadaEn(), sesion.getFinalizadaEn(), sesion.estaFinalizada());
    }

    public PaginaDto<SesionResumenDto> aPagina(Page<SesionEntrenamiento> pagina) {
        return new PaginaDto<>(pagina.map(this::aResumen).getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }

    public SerieDto aDto(SerieRegistro serie) {
        Ejercicio ejercicio = serie.getEjercicio();
        return new SerieDto(serie.getId(), ejercicio.getId(), ejercicio.getWgerId(), ejercicio.getNombre(),
                serie.getNumeroSerie(), serie.getPesoKg(), serie.getRepeticiones(), serie.getRir());
    }

    private static Long rutinaId(SesionEntrenamiento sesion) {
        Rutina rutina = sesion.getRutina();
        return rutina == null ? null : rutina.getId();
    }
}
