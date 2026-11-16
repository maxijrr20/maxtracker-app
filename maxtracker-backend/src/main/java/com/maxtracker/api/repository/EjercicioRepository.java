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

    /** Ejercicio personalizado de un usuario (nunca una copia de wger ni uno de otro usuario). */
    Optional<Ejercicio> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** El usuario ya tiene un personalizado con ese nombre, sin distinguir mayúsculas. */
    boolean existsByUsuarioIdAndNombreIgnoreCase(Long usuarioId, String nombre);

    /** El ejercicio aparece en alguna rutina o serie (su FK impide borrarlo). */
    default boolean estaEnUso(Long id) {
        return usadoEnRutinas(id) || usadoEnSeries(id);
    }

    @Query("select case when count(re) > 0 then true else false end from RutinaEjercicio re where re.ejercicio.id = :id")
    boolean usadoEnRutinas(@Param("id") Long id);

    @Query("select case when count(s) > 0 then true else false end from SerieRegistro s where s.ejercicio.id = :id")
    boolean usadoEnSeries(@Param("id") Long id);
}
