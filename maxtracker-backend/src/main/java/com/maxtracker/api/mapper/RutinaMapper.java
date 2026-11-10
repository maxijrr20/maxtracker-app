package com.maxtracker.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.maxtracker.api.dto.rutina.RutinaEjercicioResponse;
import com.maxtracker.api.dto.rutina.RutinaResponse;
import com.maxtracker.api.dto.rutina.RutinaResumenResponse;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Rutina;
import com.maxtracker.api.model.RutinaEjercicio;

/** Entidades de rutina → DTOs. Debe usarse dentro de la transacción (las relaciones son LAZY). */
@Component
public class RutinaMapper {

    public RutinaResponse aResponse(Rutina rutina) {
        return new RutinaResponse(rutina.getId(), rutina.getNombre(), rutina.getDescripcion(), rutina.getCreadoEn(),
                aResponses(rutina.getEjercicios()));
    }

    public RutinaResumenResponse aResumen(Rutina rutina) {
        return new RutinaResumenResponse(rutina.getId(), rutina.getNombre(), rutina.getDescripcion(),
                rutina.getCreadoEn(), rutina.getEjercicios().size());
    }

    public List<RutinaEjercicioResponse> aResponses(List<RutinaEjercicio> items) {
        return items.stream().map(RutinaMapper::aResponse).toList();
    }

    private static RutinaEjercicioResponse aResponse(RutinaEjercicio item) {
        Ejercicio ejercicio = item.getEjercicio();
        return new RutinaEjercicioResponse(item.getOrden(), ejercicio.getId(), ejercicio.getWgerId(),
                ejercicio.getNombre(), ejercicio.getCategoria(), item.getSeriesObjetivo(),
                item.getRepeticionesObjetivo());
    }
}
