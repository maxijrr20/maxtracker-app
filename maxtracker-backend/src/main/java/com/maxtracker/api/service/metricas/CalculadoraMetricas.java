package com.maxtracker.api.service.metricas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.maxtracker.api.dto.metricas.EjercicioConHistorialDto;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse.PuntoProgresoDto;
import com.maxtracker.api.dto.metricas.RecordPersonalDto;
import com.maxtracker.api.dto.metricas.ResumenMetricasResponse.PeriodoDto;
import com.maxtracker.api.dto.metricas.VolumenSemanalDto;
import com.maxtracker.api.repository.SerieHistorial;

/**
 * Cálculos de métricas sobre series ya cargadas. Funciones puras: sin base de datos, sin reloj ni Spring,
 * así se prueban con datos en memoria.
 */
public final class CalculadoraMetricas {

    private static final BigDecimal TREINTA = BigDecimal.valueOf(30);
    private static final int DECIMALES = 2;

    private CalculadoraMetricas() {
    }

    /** Lunes de la semana de {@code fecha}. */
    public static LocalDate inicioSemana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * 1RM estimado con la fórmula de Epley: {@code peso × (1 + reps / 30)}; con 1 repetición es el propio peso.
     * Es una estimación: pierde precisión por encima de ~10 repeticiones.
     */
    public static BigDecimal unoRmEpley(BigDecimal pesoKg, int repeticiones) {
        if (repeticiones <= 1) {
            return pesoKg.setScale(DECIMALES, RoundingMode.HALF_UP);
        }
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(repeticiones).divide(TREINTA, 10, RoundingMode.HALF_UP));
        return pesoKg.multiply(factor).setScale(DECIMALES, RoundingMode.HALF_UP);
    }

    /** Totales de las series cuya fecha está en [{@code desde}, {@code hasta}]. */
    public static PeriodoDto periodo(List<SerieHistorial> series, LocalDate desde, LocalDate hasta) {
        List<SerieHistorial> enPeriodo = series.stream().filter(entre(desde, hasta)).toList();
        int sesiones = (int) enPeriodo.stream().map(SerieHistorial::sesionId).distinct().count();
        return new PeriodoDto(volumen(enPeriodo), sesiones, enPeriodo.size());
    }

    /**
     * Volumen de las últimas {@code semanas} semanas hasta la de {@code hoy}, de la más antigua a la actual.
     * Incluye las semanas sin series (volumen 0) para que la gráfica no salte semanas.
     */
    public static List<VolumenSemanalDto> volumenSemanal(List<SerieHistorial> series, LocalDate hoy, int semanas) {
        LocalDate actual = inicioSemana(hoy);
        List<VolumenSemanalDto> resultado = new ArrayList<>(semanas);
        for (int i = semanas - 1; i >= 0; i--) {
            LocalDate lunes = actual.minusWeeks(i);
            PeriodoDto totales = periodo(series, lunes, lunes.plusDays(6));
            resultado.add(new VolumenSemanalDto(lunes, totales.volumenKg(), totales.sesiones()));
        }
        return resultado;
    }

    /**
     * Récord de peso por ejercicio: mayor peso; a igual peso, más repeticiones; a igual serie, la primera vez.
     * Ordenados del récord más reciente al más antiguo (los nuevos récords aparecen primero).
     */
    public static List<RecordPersonalDto> records(List<SerieHistorial> series) {
        Comparator<SerieHistorial> mejor = Comparator.comparing(SerieHistorial::pesoKg)
                .thenComparingInt(SerieHistorial::repeticiones)
                // Con el mismo peso y repeticiones, la más antigua: el récord se logró esa vez.
                .thenComparing(SerieHistorial::fecha, Comparator.reverseOrder());

        return porEjercicio(series).values().stream()
                .map(delEjercicio -> {
                    SerieHistorial top = delEjercicio.stream().max(mejor).orElseThrow();
                    BigDecimal mejor1Rm = delEjercicio.stream()
                            .map(s -> unoRmEpley(s.pesoKg(), s.repeticiones()))
                            .max(Comparator.naturalOrder())
                            .orElseThrow();
                    return new RecordPersonalDto(top.ejercicioId(), top.wgerId(), top.ejercicioNombre(),
                            top.categoria(), escala(top.pesoKg()), top.repeticiones(), top.fecha(), mejor1Rm);
                })
                .sorted(Comparator.comparing(RecordPersonalDto::fecha).reversed()
                        .thenComparing(RecordPersonalDto::ejercicioNombre))
                .toList();
    }

    /** Un punto por sesión (en orden cronológico) con peso máximo, volumen y mejor 1RM estimado. */
    public static List<PuntoProgresoDto> progreso(List<SerieHistorial> seriesDeUnEjercicio) {
        Map<Long, List<SerieHistorial>> porSesion = seriesDeUnEjercicio.stream()
                .sorted(Comparator.comparing(SerieHistorial::fecha).thenComparing(SerieHistorial::sesionId))
                .collect(Collectors.groupingBy(SerieHistorial::sesionId, LinkedHashMap::new, Collectors.toList()));

        return porSesion.values().stream()
                .map(delDia -> {
                    SerieHistorial primera = delDia.get(0);
                    BigDecimal pesoMax = delDia.stream().map(SerieHistorial::pesoKg)
                            .max(Comparator.naturalOrder()).orElseThrow();
                    BigDecimal mejor1Rm = delDia.stream().map(s -> unoRmEpley(s.pesoKg(), s.repeticiones()))
                            .max(Comparator.naturalOrder()).orElseThrow();
                    return new PuntoProgresoDto(primera.sesionId(), primera.fecha(), escala(pesoMax),
                            volumen(delDia), mejor1Rm, delDia.size());
                })
                .toList();
    }

    /** Ejercicios con series, del usado más recientemente al más antiguo. */
    public static List<EjercicioConHistorialDto> ejerciciosConHistorial(List<SerieHistorial> series) {
        return porEjercicio(series).values().stream()
                .map(delEjercicio -> {
                    SerieHistorial ultima = delEjercicio.stream()
                            .max(Comparator.comparing(SerieHistorial::fecha)).orElseThrow();
                    return new EjercicioConHistorialDto(ultima.ejercicioId(), ultima.wgerId(),
                            ultima.ejercicioNombre(), ultima.categoria(), delEjercicio.size(), ultima.fecha());
                })
                .sorted(Comparator.comparing(EjercicioConHistorialDto::ultimaFecha).reversed()
                        .thenComparing(EjercicioConHistorialDto::ejercicioNombre))
                .toList();
    }

    public static BigDecimal volumen(List<SerieHistorial> series) {
        return escala(series.stream().map(SerieHistorial::volumenKg).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static Map<Long, List<SerieHistorial>> porEjercicio(List<SerieHistorial> series) {
        return series.stream()
                .collect(Collectors.groupingBy(SerieHistorial::ejercicioId, LinkedHashMap::new, Collectors.toList()));
    }

    private static Predicate<SerieHistorial> entre(LocalDate desde, LocalDate hasta) {
        return s -> !s.fecha().isBefore(desde) && !s.fecha().isAfter(hasta);
    }

    private static BigDecimal escala(BigDecimal valor) {
        return valor.setScale(DECIMALES, RoundingMode.HALF_UP);
    }
}
