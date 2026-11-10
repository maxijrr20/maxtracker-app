package com.maxtracker.api.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxtracker.api.model.SerieRegistro;

public interface SerieRegistroRepository extends JpaRepository<SerieRegistro, Long> {

    /** Todas las series de un ejercicio para un usuario, en orden cronológico (base de las métricas de progreso). */
    @Query("""
            select s from SerieRegistro s
            join fetch s.sesion se
            where se.usuario.id = :usuarioId and s.ejercicio.id = :ejercicioId
            order by se.fecha asc, se.id asc, s.numeroSerie asc
            """)
    List<SerieRegistro> findHistorial(@Param("usuarioId") Long usuarioId, @Param("ejercicioId") Long ejercicioId);

    String PROYECCION = """
            select new com.maxtracker.api.repository.SerieHistorial(
                se.id, se.fecha, e.id, e.wgerId, e.nombre, e.categoria, s.numeroSerie, s.pesoKg, s.repeticiones, s.rir)
            from SerieRegistro s
            join s.sesion se
            join s.ejercicio e
            """;

    String ORDEN = " order by se.fecha asc, se.id asc, s.id asc";

    /** Todas las series del usuario (récords personales y ejercicios con historial). */
    @Query(PROYECCION + " where se.usuario.id = :usuarioId" + ORDEN)
    List<SerieHistorial> findHistorialCompleto(@Param("usuarioId") Long usuarioId);

    /** Series del usuario con fecha de sesión desde {@code desde} (inclusive): volumen por semana y por mes. */
    @Query(PROYECCION + " where se.usuario.id = :usuarioId and se.fecha >= :desde" + ORDEN)
    List<SerieHistorial> findHistorialDesde(@Param("usuarioId") Long usuarioId, @Param("desde") LocalDate desde);

    /** Series de un ejercicio del usuario (curva de progreso). */
    @Query(PROYECCION + " where se.usuario.id = :usuarioId and e.id = :ejercicioId" + ORDEN)
    List<SerieHistorial> findHistorialDeEjercicio(@Param("usuarioId") Long usuarioId,
            @Param("ejercicioId") Long ejercicioId);

    /** Series recientes de ciertos ejercicios del usuario (contexto del análisis con IA). */
    @Query(PROYECCION + " where se.usuario.id = :usuarioId and se.fecha >= :desde and e.id in :ejercicioIds" + ORDEN)
    List<SerieHistorial> findHistorialDeEjerciciosDesde(@Param("usuarioId") Long usuarioId,
            @Param("ejercicioIds") Collection<Long> ejercicioIds, @Param("desde") LocalDate desde);
}
