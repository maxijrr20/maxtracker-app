package com.maxtracker.api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.dto.sesion.ActualizarSesionRequest;
import com.maxtracker.api.dto.sesion.IniciarSesionRequest;
import com.maxtracker.api.dto.sesion.RegistrarSerieRequest;
import com.maxtracker.api.dto.sesion.SerieDto;
import com.maxtracker.api.dto.sesion.SesionDto;
import com.maxtracker.api.dto.sesion.SesionResumenDto;
import com.maxtracker.api.exception.RecursoNoEncontradoException;
import com.maxtracker.api.mapper.SesionMapper;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.SerieRegistro;
import com.maxtracker.api.model.SesionEntrenamiento;
import com.maxtracker.api.repository.RutinaRepository;
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.service.SesionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaSesionService implements SesionService {

    /** Coincide con {@code peso_kg NUMERIC(6,2)}: la respuesta muestra lo mismo que queda guardado. */
    private static final int DECIMALES_PESO = 2;

    private final SesionEntrenamientoRepository sesionRepository;
    private final RutinaRepository rutinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResolutorEjercicio resolutorEjercicio;
    private final SesionMapper mapper;
    private final Clock clock;

    @Override
    @Transactional
    public SesionDto iniciar(long usuarioId, IniciarSesionRequest request) {
        LocalDate fecha = request.fecha() != null ? request.fecha() : LocalDate.now(clock);
        SesionEntrenamiento sesion =
                new SesionEntrenamiento(usuarioRepository.getReferenceById(usuarioId), fecha, clock.instant());
        sesion.setNotas(request.notas());
        if (request.rutinaId() != null) {
            sesion.setRutina(rutinaRepository.findByIdAndUsuarioId(request.rutinaId(), usuarioId)
                    .orElseThrow(() -> RecursoNoEncontradoException.rutina(request.rutinaId())));
        }
        return mapper.aDto(sesionRepository.saveAndFlush(sesion));
    }

    @Override
    public PaginaDto<SesionResumenDto> listar(long usuarioId, int pagina, int tamanio) {
        return mapper.aPagina(
                sesionRepository.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId, PageRequest.of(pagina, tamanio)));
    }

    @Override
    public SesionDto obtener(long usuarioId, long sesionId) {
        return mapper.aDto(buscar(usuarioId, sesionId));
    }

    @Override
    @Transactional
    public SesionDto actualizar(long usuarioId, long sesionId, ActualizarSesionRequest request) {
        SesionEntrenamiento sesion = buscar(usuarioId, sesionId);
        sesion.setFecha(request.fecha());
        sesion.setNotas(request.notas());
        return mapper.aDto(sesion);
    }

    @Override
    @Transactional
    public void eliminar(long usuarioId, long sesionId) {
        sesionRepository.delete(buscar(usuarioId, sesionId));
    }

    @Override
    @Transactional
    public SesionDto finalizar(long usuarioId, long sesionId) {
        SesionEntrenamiento sesion = buscar(usuarioId, sesionId);
        sesion.finalizar(clock.instant());
        return mapper.aDto(sesion);
    }

    @Override
    @Transactional
    public SerieDto registrarSerie(long usuarioId, long sesionId, RegistrarSerieRequest request) {
        SesionEntrenamiento sesion = buscar(usuarioId, sesionId);
        // Falla antes de consultar wger o crear la copia local del ejercicio.
        sesion.exigirEnCurso();
        Ejercicio ejercicio = request.wgerId() != null
                ? resolutorEjercicio.porWgerId(request.wgerId())
                : resolutorEjercicio.porIdLocal(request.ejercicioId(), usuarioId);
        SerieRegistro serie = sesion.registrarSerie(ejercicio, normalizarPeso(request.pesoKg()),
                request.repeticiones(), request.rir());
        // flush (no save): la sesión ya está gestionada y un merge en cascada guardaría una copia de la serie,
        // dejando sin id la instancia que se devuelve. El flush la persiste en cascada y le asigna el id.
        sesionRepository.flush();
        return mapper.aDto(serie);
    }

    @Override
    @Transactional
    public void eliminarSerie(long usuarioId, long sesionId, long serieId) {
        SesionEntrenamiento sesion = buscar(usuarioId, sesionId);
        SerieRegistro serie = sesion.buscarSerie(serieId)
                .orElseThrow(() -> RecursoNoEncontradoException.serie(serieId));
        sesion.quitarSerie(serie);
    }

    private SesionEntrenamiento buscar(long usuarioId, long sesionId) {
        return sesionRepository.findConSeriesByIdAndUsuarioId(sesionId, usuarioId)
                .orElseThrow(() -> RecursoNoEncontradoException.sesion(sesionId));
    }

    /** {@code @Digits(fraction = 2)} ya garantiza como máximo 2 decimales: el redondeo nunca pierde datos. */
    private static BigDecimal normalizarPeso(BigDecimal pesoKg) {
        return pesoKg.setScale(DECIMALES_PESO, RoundingMode.UNNECESSARY);
    }
}
