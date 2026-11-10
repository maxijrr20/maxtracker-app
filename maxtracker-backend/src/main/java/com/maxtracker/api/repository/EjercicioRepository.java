package com.maxtracker.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxtracker.api.model.Ejercicio;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    /** Copia local de un ejercicio de wger. */
    Optional<Ejercicio> findByWgerId(Long wgerId);

    /** Ejercicios personalizados de un usuario. */
    List<Ejercicio> findByUsuarioIdOrderByNombreAsc(Long usuarioId);

    /** Ejercicio que el usuario puede usar: una copia de wger (compartida) o uno personalizado suyo. */
    @Query("select e from Ejercicio e where e.id = :id and (e.usuario is null or e.usuario.id = :usuarioId)")
    Optional<Ejercicio> findAccesible(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
}
