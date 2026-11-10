package com.maxtracker.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxtracker.api.dto.rutina.ActualizarRutinaRequest;
import com.maxtracker.api.dto.rutina.CrearRutinaRequest;
import com.maxtracker.api.dto.rutina.RutinaEjercicioRequest;
import com.maxtracker.api.dto.rutina.RutinaResponse;
import com.maxtracker.api.dto.rutina.RutinaResumenResponse;
import com.maxtracker.api.exception.RecursoNoEncontradoException;
import com.maxtracker.api.mapper.RutinaMapper;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Rutina;
import com.maxtracker.api.repository.RutinaRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.service.RutinaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaRutinaService implements RutinaService {

    private final RutinaRepository rutinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResolutorEjercicio resolutorEjercicio;
    private final RutinaMapper mapper;

    @Override
    @Transactional
    public RutinaResponse crear(long usuarioId, CrearRutinaRequest request) {
        List<Ejercicio> ejercicios = resolver(request.ejercicios(), usuarioId);
        Rutina rutina = new Rutina(usuarioRepository.getReferenceById(usuarioId), request.nombre().strip(),
                normalizar(request.descripcion()));
        agregar(rutina, request.ejercicios(), ejercicios);
        return mapper.aResponse(rutinaRepository.saveAndFlush(rutina));
    }

    @Override
    public List<RutinaResumenResponse> listar(long usuarioId) {
        return rutinaRepository.findConEjerciciosByUsuarioIdOrderByNombreAscIdAsc(usuarioId).stream()
                .map(mapper::aResumen)
                .toList();
    }

    @Override
    public RutinaResponse obtener(long usuarioId, long rutinaId) {
        return mapper.aResponse(buscar(usuarioId, rutinaId));
    }

    @Override
    @Transactional
    public RutinaResponse actualizar(long usuarioId, long rutinaId, ActualizarRutinaRequest request) {
        Rutina rutina = buscar(usuarioId, rutinaId);
        List<Ejercicio> ejercicios = resolver(request.ejercicios(), usuarioId);
        rutina.setNombre(request.nombre().strip());
        rutina.setDescripcion(normalizar(request.descripcion()));
        rutina.quitarTodosLosEjercicios();
        agregar(rutina, request.ejercicios(), ejercicios);
        // flush (no save): la rutina ya está gestionada; el flush borra los ítems viejos e inserta los nuevos.
        rutinaRepository.flush();
        return mapper.aResponse(rutina);
    }

    @Override
    @Transactional
    public void eliminar(long usuarioId, long rutinaId) {
        // Las sesiones que la usaban se conservan con rutina_id = NULL (FK ON DELETE SET NULL).
        rutinaRepository.delete(buscar(usuarioId, rutinaId));
    }

    /**
     * Resuelve todos los ejercicios antes de tocar la rutina: si uno no existe (404) o wger falla (502),
     * la transacción se revierte y la rutina queda como estaba.
     */
    private List<Ejercicio> resolver(List<RutinaEjercicioRequest> items, long usuarioId) {
        return items.stream()
                .map(item -> item.wgerId() != null
                        ? resolutorEjercicio.porWgerId(item.wgerId())
                        : resolutorEjercicio.porIdLocal(item.ejercicioId(), usuarioId))
                .toList();
    }

    private static void agregar(Rutina rutina, List<RutinaEjercicioRequest> items, List<Ejercicio> ejercicios) {
        for (int i = 0; i < items.size(); i++) {
            RutinaEjercicioRequest item = items.get(i);
            rutina.agregarEjercicio(ejercicios.get(i), item.seriesObjetivo(), item.repeticionesObjetivo());
        }
    }

    private Rutina buscar(long usuarioId, long rutinaId) {
        return rutinaRepository.findConEjerciciosByIdAndUsuarioId(rutinaId, usuarioId)
                .orElseThrow(() -> RecursoNoEncontradoException.rutina(rutinaId));
    }

    /** Texto opcional: en blanco equivale a ausente. */
    private static String normalizar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
