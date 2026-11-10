package com.maxtracker.api.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxtracker.api.dto.metricas.EjercicioConHistorialDto;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse;
import com.maxtracker.api.dto.metricas.ResumenMetricasResponse;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.repository.SerieHistorial;
import com.maxtracker.api.repository.SerieRegistroRepository;
import com.maxtracker.api.service.MetricasService;
import com.maxtracker.api.service.metricas.CalculadoraMetricas;

import lombok.RequiredArgsConstructor;

/** Carga las series necesarias (proyecciones) y delega los cálculos en {@link CalculadoraMetricas}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaMetricasService implements MetricasService {

    private final SerieRegistroRepository serieRepository;
    private final EjercicioRepository ejercicioRepository;
    private final Clock clock;

    @Override
    public ResumenMetricasResponse resumen(long usuarioId, int semanas) {
        LocalDate hoy = LocalDate.now(clock);
        LocalDate lunes = CalculadoraMetricas.inicioSemana(hoy);
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDate primerLunes = lunes.minusWeeks(semanas - 1L);

        // Una sola consulta cubre el gráfico semanal y la comparación con la semana y el mes anteriores.
        LocalDate desde = primerLunes.isBefore(inicioMesAnterior) ? primerLunes : inicioMesAnterior;
        List<SerieHistorial> recientes = serieRepository.findHistorialDesde(usuarioId, desde);

        return new ResumenMetricasResponse(
                CalculadoraMetricas.periodo(recientes, lunes, hoy),
                CalculadoraMetricas.periodo(recientes, lunes.minusWeeks(1), lunes.minusDays(1)),
                CalculadoraMetricas.periodo(recientes, inicioMes, hoy),
                CalculadoraMetricas.periodo(recientes, inicioMesAnterior, inicioMes.minusDays(1)),
                CalculadoraMetricas.volumenSemanal(recientes, hoy, semanas),
                // Los récords son de todo el historial.
                CalculadoraMetricas.records(serieRepository.findHistorialCompleto(usuarioId)));
    }

    @Override
    public List<EjercicioConHistorialDto> ejerciciosConHistorial(long usuarioId) {
        return CalculadoraMetricas.ejerciciosConHistorial(serieRepository.findHistorialCompleto(usuarioId));
    }

    @Override
    public ProgresoEjercicioResponse progreso(long usuarioId, long ejercicioId) {
        Ejercicio ejercicio = ejercicioRepository.findAccesible(ejercicioId, usuarioId)
                .orElseThrow(() -> new EjercicioNoEncontradoException(ejercicioId));
        List<SerieHistorial> series = serieRepository.findHistorialDeEjercicio(usuarioId, ejercicioId);
        return new ProgresoEjercicioResponse(ejercicioId, ejercicio.getNombre(), CalculadoraMetricas.progreso(series));
    }
}
