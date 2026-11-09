package com.maxtracker.api.repository;

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
}
