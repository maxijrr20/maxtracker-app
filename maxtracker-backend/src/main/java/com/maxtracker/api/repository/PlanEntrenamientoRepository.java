package com.maxtracker.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maxtracker.api.model.PlanEntrenamiento;

public interface PlanEntrenamientoRepository extends JpaRepository<PlanEntrenamiento, Long> {

    /** Historial de planes del usuario, del más antiguo al vigente. */
    List<PlanEntrenamiento> findByUsuarioIdOrderByVigenteDesdeAsc(Long usuarioId);

    /** Plan vigente (el más reciente). */
    Optional<PlanEntrenamiento> findFirstByUsuarioIdOrderByVigenteDesdeDesc(Long usuarioId);
}
