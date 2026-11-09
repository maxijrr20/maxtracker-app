package com.maxtracker.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maxtracker.api.model.Ejercicio;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    /** Copia local de un ejercicio de wger. */
    Optional<Ejercicio> findByWgerId(Long wgerId);

    /** Ejercicios personalizados de un usuario. */
    List<Ejercicio> findByUsuarioIdOrderByNombreAsc(Long usuarioId);
}
