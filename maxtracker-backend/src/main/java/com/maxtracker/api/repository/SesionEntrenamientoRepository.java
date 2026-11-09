package com.maxtracker.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.maxtracker.api.model.SesionEntrenamiento;

/** Las series ({@code SerieRegistro}) se guardan y borran en cascada desde la sesión. */
public interface SesionEntrenamientoRepository extends JpaRepository<SesionEntrenamiento, Long> {

    /** Historial de sesiones de un usuario, de la más reciente a la más antigua. */
    Page<SesionEntrenamiento> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId, Pageable pageable);

    /** Sesión de un usuario con sus series y ejercicios cargados en una sola consulta. */
    @EntityGraph(attributePaths = {"series", "series.ejercicio"})
    Optional<SesionEntrenamiento> findConSeriesByIdAndUsuarioId(Long id, Long usuarioId);
}
