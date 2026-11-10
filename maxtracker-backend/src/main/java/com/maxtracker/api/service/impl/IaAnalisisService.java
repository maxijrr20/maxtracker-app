package com.maxtracker.api.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.maxtracker.api.dto.analisis.AnalisisRutinaRequest;
import com.maxtracker.api.dto.analisis.AnalisisRutinaResponse;
import com.maxtracker.api.dto.analisis.AnalisisRutinaResponse.EjercicioAnalisisDto;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse.PuntoProgresoDto;
import com.maxtracker.api.exception.RecursoNoEncontradoException;
import com.maxtracker.api.exception.SinDatosParaAnalisisException;
import com.maxtracker.api.model.Rutina;
import com.maxtracker.api.model.RutinaEjercicio;
import com.maxtracker.api.model.SerieRegistro;
import com.maxtracker.api.model.SesionEntrenamiento;
import com.maxtracker.api.repository.RutinaRepository;
import com.maxtracker.api.repository.SerieHistorial;
import com.maxtracker.api.repository.SerieRegistroRepository;
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.service.AnalisisService;
import com.maxtracker.api.service.ia.AsistenteEntrenamiento;
import com.maxtracker.api.service.ia.ContextoAnalisis;
import com.maxtracker.api.service.ia.ContextoAnalisis.EjercicioContexto;
import com.maxtracker.api.service.ia.ContextoAnalisis.SerieContexto;
import com.maxtracker.api.service.ia.ContextoAnalisis.SesionContexto;
import com.maxtracker.api.service.ia.RecomendacionIa;
import com.maxtracker.api.service.metricas.CalculadoraMetricas;



/**
 * Arma el historial reciente del usuario y se lo pasa al {@link AsistenteEntrenamiento}.
 *
 * <p>El método no es {@code @Transactional}: las lecturas van en una transacción de solo lectura que termina
 * <b>antes</b> de llamar a la IA, así la conexión a la base (pool pequeño) no queda tomada mientras el modelo
 * responde (varios segundos).
 */
@Service
public class IaAnalisisService implements AnalisisService {

    /** Acotan el tamaño del prompt (y el consumo de la cuota gratuita). */
    static final int MAX_EJERCICIOS = 12;
    static final int MAX_SESIONES_POR_EJERCICIO = 8;

    private final SerieRegistroRepository serieRepository;
    private final RutinaRepository rutinaRepository;
    private final SesionEntrenamientoRepository sesionRepository;
    private final AsistenteEntrenamiento asistente;
    private final LimitadorAnalisis limitador;
    private final Clock clock;
    private final TransactionTemplate lectura;

    IaAnalisisService(SerieRegistroRepository serieRepository, RutinaRepository rutinaRepository,
            SesionEntrenamientoRepository sesionRepository, AsistenteEntrenamiento asistente,
            LimitadorAnalisis limitador, Clock clock, PlatformTransactionManager transactionManager) {
        this.serieRepository = serieRepository;
        this.rutinaRepository = rutinaRepository;
        this.sesionRepository = sesionRepository;
        this.asistente = asistente;
        this.limitador = limitador;
        this.clock = clock;
        this.lectura = new TransactionTemplate(transactionManager);
        this.lectura.setReadOnly(true);
    }

    private record Recolectado(Alcance alcance, List<SerieHistorial> series) {
    }

    /**
     * Qué se analiza: todos los ejercicios del periodo ({@code general}) o un conjunto de ejercicios,
     * con sus objetivos si vienen de una rutina.
     */
    private record Alcance(String descripcion, boolean general, Set<Long> ejercicioIds,
            Map<Long, RutinaEjercicio> plan, LocalDate desde) {
    }

    @Override
    public AnalisisRutinaResponse analizar(long usuarioId, AnalisisRutinaRequest request) {
        int dias = request.diasOPorDefecto();
        LocalDate hoy = LocalDate.now(clock);
        LocalDate desde = hoy.minusDays(dias - 1L);

        // Lectura (en transacción) → contexto en memoria → IA (fuera de la transacción).
        Recolectado datos = lectura.execute(estado -> recolectar(usuarioId, request, dias, desde));
        if (datos == null || datos.series().isEmpty()) {
            throw new SinDatosParaAnalisisException(dias);
        }
        Alcance alcance = datos.alcance();

        List<EjercicioContexto> ejercicios = contextoPorEjercicio(datos.series(), alcance);
        int seriesAnalizadas = ejercicios.stream()
                .flatMap(e -> e.sesiones().stream())
                .mapToInt(s -> s.series().size())
                .sum();

        // Solo cuenta para el límite si de verdad se va a consultar a la IA.
        limitador.registrar(usuarioId);
        RecomendacionIa recomendacion = asistente.analizar(
                new ContextoAnalisis(alcance.descripcion(), hoy, dias, ejercicios));

        return new AnalisisRutinaResponse(alcance.descripcion(), recomendacion.resumen(),
                recomendacion.ejercicios().stream()
                        .map(e -> new EjercicioAnalisisDto(e.ejercicio(), e.tendencia().name(), e.observacion(),
                                e.sugerencia()))
                        .toList(),
                recomendacion.ajusteRir(), recomendacion.sugerencias(), dias, seriesAnalizadas,
                recomendacion.modelo(), clock.instant());
    }

    private Recolectado recolectar(long usuarioId, AnalisisRutinaRequest request, int dias, LocalDate desde) {
        Alcance alcance = request.rutinaId() != null ? deRutina(usuarioId, request.rutinaId(), desde)
                : request.sesionId() != null ? deSesion(usuarioId, request.sesionId(), desde)
                : new Alcance("Últimos " + dias + " días", true, Set.of(), Map.of(), desde);

        List<SerieHistorial> series;
        if (alcance.general()) {
            series = serieRepository.findHistorialDesde(usuarioId, alcance.desde());
        } else if (alcance.ejercicioIds().isEmpty()) {
            series = List.of(); // Rutina sin ejercicios o sesión sin series ni plan.
        } else {
            series = serieRepository.findHistorialDeEjerciciosDesde(usuarioId, alcance.ejercicioIds(),
                    alcance.desde());
        }
        return new Recolectado(alcance, series);
    }

    private Alcance deRutina(long usuarioId, long rutinaId, LocalDate desde) {
        Rutina rutina = rutinaRepository.findConEjerciciosByIdAndUsuarioId(rutinaId, usuarioId)
                .orElseThrow(() -> RecursoNoEncontradoException.rutina(rutinaId));
        Map<Long, RutinaEjercicio> plan = planPorEjercicio(rutina);
        return new Alcance("Rutina " + rutina.getNombre(), false, plan.keySet(), plan, desde);
    }

    private Alcance deSesion(long usuarioId, long sesionId, LocalDate desde) {
        SesionEntrenamiento sesion = sesionRepository.findConSeriesByIdAndUsuarioId(sesionId, usuarioId)
                .orElseThrow(() -> RecursoNoEncontradoException.sesion(sesionId));
        Map<Long, RutinaEjercicio> plan = sesion.getRutina() == null ? Map.of()
                : rutinaRepository.findConEjerciciosByIdAndUsuarioId(sesion.getRutina().getId(), usuarioId)
                        .map(IaAnalisisService::planPorEjercicio)
                        .orElse(Map.of());
        Set<Long> ids = new LinkedHashSet<>(plan.keySet());
        sesion.getSeries().stream().map(SerieRegistro::getEjercicio).forEach(e -> ids.add(e.getId()));
        // La sesión analizada siempre entra, aunque sea anterior al periodo.
        LocalDate desdeEfectivo = sesion.getFecha().isBefore(desde) ? sesion.getFecha() : desde;
        return new Alcance("Sesión del " + sesion.getFecha(), false, ids, plan, desdeEfectivo);
    }

    private static Map<Long, RutinaEjercicio> planPorEjercicio(Rutina rutina) {
        // Si un ejercicio aparece dos veces en la rutina, vale el primero.
        return rutina.getEjercicios().stream().collect(Collectors.toMap(
                item -> item.getEjercicio().getId(), item -> item, (primero, otro) -> primero, LinkedHashMap::new));
    }

    /**
     * Agrupa por ejercicio, en el orden del plan (o del más reciente al más antiguo si no hay plan),
     * con las últimas sesiones de cada uno.
     */
    private static List<EjercicioContexto> contextoPorEjercicio(List<SerieHistorial> series, Alcance alcance) {
        Map<Long, List<SerieHistorial>> porEjercicio = series.stream()
                .collect(Collectors.groupingBy(SerieHistorial::ejercicioId, LinkedHashMap::new, Collectors.toList()));

        Comparator<Long> orden = alcance.plan().isEmpty()
                ? Comparator.comparing((Long id) -> ultimaFecha(porEjercicio.get(id))).reversed()
                : Comparator.comparingInt((Long id) -> alcance.plan().containsKey(id)
                        ? alcance.plan().get(id).getOrden() : Integer.MAX_VALUE);

        return porEjercicio.keySet().stream()
                .sorted(orden)
                .limit(MAX_EJERCICIOS)
                .map(id -> contexto(porEjercicio.get(id), alcance.plan().get(id)))
                .toList();
    }

    private static EjercicioContexto contexto(List<SerieHistorial> delEjercicio, RutinaEjercicio plan) {
        Map<Long, List<SerieHistorial>> porSesion = delEjercicio.stream()
                .collect(Collectors.groupingBy(SerieHistorial::sesionId, LinkedHashMap::new, Collectors.toList()));
        List<List<SerieHistorial>> ultimas = porSesion.values().stream()
                .skip(Math.max(0, porSesion.size() - MAX_SESIONES_POR_EJERCICIO))
                .toList();

        List<SesionContexto> sesiones = ultimas.stream()
                .map(delDia -> new SesionContexto(delDia.get(0).fecha(), delDia.stream()
                        .map(s -> new SerieContexto(s.pesoKg(), s.repeticiones(),
                                s.rir() == null ? null : s.rir().intValue()))
                        .toList()))
                .toList();
        List<PuntoProgresoDto> progreso = CalculadoraMetricas.progreso(ultimas.stream().flatMap(List::stream).toList());

        SerieHistorial cualquiera = delEjercicio.get(0);
        return new EjercicioContexto(cualquiera.ejercicioNombre(), cualquiera.categoria(),
                plan == null ? null : plan.getSeriesObjetivo(),
                plan == null ? null : plan.getRepeticionesObjetivo(),
                progreso.get(0).unoRmEstimadoKg(), progreso.get(progreso.size() - 1).unoRmEstimadoKg(), sesiones);
    }

    private static LocalDate ultimaFecha(List<SerieHistorial> series) {
        return series.get(series.size() - 1).fecha();
    }
}
