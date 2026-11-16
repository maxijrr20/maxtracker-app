package com.maxtracker.api.service.constancia;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import com.maxtracker.api.dto.constancia.DiaConstanciaDto;
import com.maxtracker.api.dto.constancia.EstadoDia;
import com.maxtracker.api.dto.constancia.ResumenConstanciaDto;

/**
 * Cálculos del calendario de constancia. Funciones puras (sin JPA, reloj ni Spring): reciben los días entrenados,
 * el historial de planes y la fecha de hoy, y se prueban con datos en memoria.
 *
 * <p>Reglas:
 * <ul>
 *   <li>Cada día se evalúa con el plan vigente ese día (el último con {@code desde <= fecha}); antes del primer plan
 *       no hay días planificados.</li>
 *   <li>Hoy nunca es FALLADO: si es día del plan y no se entrenó, queda PENDIENTE hasta que termine.</li>
 *   <li>Racha = días entrenados seguidos sin fallar un día del plan. Los descansos no la cortan y los días extra la
 *       suman. Se cuenta desde el primer plan: sin plan no hay días que fallar y la racha no tiene sentido.</li>
 * </ul>
 */
public final class CalculadoraConstancia {

    /** Plan semanal vigente desde una fecha. */
    public record PlanVigente(LocalDate desde, Set<DayOfWeek> dias) {
    }

    /** Racha actual y mejor racha, en días entrenados. */
    public record Rachas(int actual, int mejor) {
    }

    private CalculadoraConstancia() {
    }

    /** @param planes historial de planes ordenado por {@code desde} ascendente */
    public static EstadoDia estado(LocalDate fecha, Set<LocalDate> entrenados, List<PlanVigente> planes,
            LocalDate hoy) {
        boolean planificado = esPlanificado(fecha, planes);
        if (entrenados.contains(fecha)) {
            return planificado ? EstadoDia.ENTRENADO : EstadoDia.EXTRA;
        }
        if (!planificado) {
            return EstadoDia.DESCANSO;
        }
        if (fecha.isAfter(hoy)) {
            return EstadoDia.PLANIFICADO;
        }
        return fecha.isEqual(hoy) ? EstadoDia.PENDIENTE : EstadoDia.FALLADO;
    }

    /** Un elemento por día del mes, en orden. */
    public static List<DiaConstanciaDto> calendario(YearMonth mes, Set<LocalDate> entrenados,
            List<PlanVigente> planes, LocalDate hoy) {
        return mes.atDay(1).datesUntil(mes.atEndOfMonth().plusDays(1))
                .map(fecha -> new DiaConstanciaDto(fecha, estado(fecha, entrenados, planes, hoy)))
                .toList();
    }

    /** Conteos entre {@code desde} y {@code hasta} inclusive, sin pasar de hoy. */
    public static ResumenConstanciaDto resumen(LocalDate desde, LocalDate hasta, Set<LocalDate> entrenados,
            List<PlanVigente> planes, LocalDate hoy) {
        LocalDate fin = hasta.isAfter(hoy) ? hoy : hasta;
        int entrenadosEnPlan = 0;
        int extra = 0;
        int fallados = 0;
        for (LocalDate fecha = desde; !fecha.isAfter(fin); fecha = fecha.plusDays(1)) {
            switch (estado(fecha, entrenados, planes, hoy)) {
                case ENTRENADO -> entrenadosEnPlan++;
                case EXTRA -> extra++;
                case FALLADO -> fallados++;
                default -> {
                    // PENDIENTE, PLANIFICADO y DESCANSO no cuentan.
                }
            }
        }
        int evaluados = entrenadosEnPlan + fallados;
        Integer cumplimiento = evaluados == 0 ? null : Math.round(100f * entrenadosEnPlan / evaluados);
        return new ResumenConstanciaDto(entrenadosEnPlan + extra, fallados, cumplimiento);
    }

    /** {@code null} si el usuario no tiene plan. */
    public static Rachas rachas(Set<LocalDate> entrenados, List<PlanVigente> planes, LocalDate hoy) {
        if (planes.isEmpty()) {
            return null;
        }
        int actual = 0;
        int mejor = 0;
        for (LocalDate fecha = planes.getFirst().desde(); !fecha.isAfter(hoy); fecha = fecha.plusDays(1)) {
            switch (estado(fecha, entrenados, planes, hoy)) {
                case ENTRENADO, EXTRA -> mejor = Math.max(mejor, ++actual);
                case FALLADO -> actual = 0;
                default -> {
                    // Descanso o pendiente: la racha sigue igual.
                }
            }
        }
        return new Rachas(actual, mejor);
    }

    /** Primer día con datos (entreno o plan), o hoy si no hay ninguno. */
    public static LocalDate inicioHistorial(Set<LocalDate> entrenados, List<PlanVigente> planes, LocalDate hoy) {
        LocalDate inicio = hoy;
        for (LocalDate fecha : entrenados) {
            if (fecha.isBefore(inicio)) {
                inicio = fecha;
            }
        }
        if (!planes.isEmpty() && planes.getFirst().desde().isBefore(inicio)) {
            inicio = planes.getFirst().desde();
        }
        return inicio;
    }

    private static boolean esPlanificado(LocalDate fecha, List<PlanVigente> planes) {
        PlanVigente vigente = null;
        for (PlanVigente plan : planes) {
            if (plan.desde().isAfter(fecha)) {
                break;
            }
            vigente = plan;
        }
        return vigente != null && vigente.dias().contains(fecha.getDayOfWeek());
    }
}
