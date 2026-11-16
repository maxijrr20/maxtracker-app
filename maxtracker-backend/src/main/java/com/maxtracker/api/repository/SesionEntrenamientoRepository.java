package com.maxtracker.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxtracker.api.model.SesionEntrenamiento;

/** Las series ({@code SerieRegistro}) se guardan y borran en cascada desde la sesión. */
public interface SesionEntrenamientoRepository extends JpaRepository<SesionEntrenamiento, Long> {

    /** Historial de sesiones de un usuario, de la más reciente a la más antigua. */
    Page<SesionEntrenamiento> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId, Pageable pageable);

    /** Sesión de un usuario con sus series y ejercicios cargados en una sola consulta. */
    @EntityGraph(attributePaths = {"series", "series.ejercicio"})
    Optional<SesionEntrenamiento> findConSeriesByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Días en que el usuario entrenó: fechas con al menos una serie registrada (una sesión vacía, iniciada sin querer,
     * no cuenta). Base del calendario de constancia.
     */
    @Query("""
            select distinct s.fecha from SesionEntrenamiento s
            where s.usuario.id = :usuarioId and exists (select 1 from SerieRegistro r where r.sesion = s)
            order by s.fecha
            """)
    List<LocalDate> findFechasEntrenadas(@Param("usuarioId") Long usuarioId);
}
