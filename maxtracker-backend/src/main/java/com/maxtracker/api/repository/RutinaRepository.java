package com.maxtracker.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.maxtracker.api.model.Rutina;

/** Los ítems ({@code RutinaEjercicio}) se guardan y borran en cascada desde la rutina. */
public interface RutinaRepository extends JpaRepository<Rutina, Long> {

    List<Rutina> findByUsuarioIdOrderByNombreAsc(Long usuarioId);

    /** Rutina de un usuario (sin cargar sus ejercicios). */
    Optional<Rutina> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** Rutina de un usuario con sus ejercicios cargados en una sola consulta. */
    @EntityGraph(attributePaths = {"ejercicios", "ejercicios.ejercicio"})
    Optional<Rutina> findConEjerciciosByIdAndUsuarioId(Long id, Long usuarioId);
}
