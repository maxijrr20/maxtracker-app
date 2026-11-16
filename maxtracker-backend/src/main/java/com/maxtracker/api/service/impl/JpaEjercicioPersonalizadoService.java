package com.maxtracker.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxtracker.api.dto.ejercicio.CrearEjercicioPersonalizadoRequest;
import com.maxtracker.api.dto.ejercicio.EjercicioPersonalizadoResponse;
import com.maxtracker.api.exception.EjercicioEnUsoException;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.exception.EjercicioPersonalizadoDuplicadoException;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.service.EjercicioPersonalizadoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaEjercicioPersonalizadoService implements EjercicioPersonalizadoService {

    private final EjercicioRepository ejercicioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public EjercicioPersonalizadoResponse crear(long usuarioId, CrearEjercicioPersonalizadoRequest request) {
        String nombre = request.nombre().strip().replaceAll("\\s+", " ");
        // La BD también lo impide (uk_ejercicio_usuario_nombre), pero distingue mayúsculas y su error es genérico.
        if (ejercicioRepository.existsByUsuarioIdAndNombreIgnoreCase(usuarioId, nombre)) {
            throw new EjercicioPersonalizadoDuplicadoException(nombre);
        }
        Ejercicio ejercicio = Ejercicio.personalizado(usuarioRepository.getReferenceById(usuarioId), nombre,
                request.categoria());
        return aResponse(ejercicioRepository.saveAndFlush(ejercicio));
    }

    @Override
    public List<EjercicioPersonalizadoResponse> listar(long usuarioId) {
        return ejercicioRepository.findByUsuarioIdOrderByNombreAsc(usuarioId).stream()
                .map(JpaEjercicioPersonalizadoService::aResponse)
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(long usuarioId, long ejercicioId) {
        Ejercicio ejercicio = ejercicioRepository.findByIdAndUsuarioId(ejercicioId, usuarioId)
                .orElseThrow(() -> new EjercicioNoEncontradoException(ejercicioId));
        if (ejercicioRepository.estaEnUso(ejercicioId)) {
            throw new EjercicioEnUsoException(ejercicio.getNombre());
        }
        ejercicioRepository.delete(ejercicio);
    }

    private static EjercicioPersonalizadoResponse aResponse(Ejercicio ejercicio) {
        return new EjercicioPersonalizadoResponse(ejercicio.getId(), ejercicio.getNombre(), ejercicio.getCategoria());
    }
}
