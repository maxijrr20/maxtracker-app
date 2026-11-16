package com.maxtracker.api.service.constancia;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.maxtracker.api.dto.constancia.DiaConstanciaDto;
import com.maxtracker.api.dto.constancia.EstadoDia;
import com.maxtracker.api.dto.constancia.ResumenConstanciaDto;
import com.maxtracker.api.service.constancia.CalculadoraConstancia.PlanVigente;
import com.maxtracker.api.service.constancia.CalculadoraConstancia.Rachas;

/**
 * Escenario: hoy es miércoles 18/11/2026. Plan lunes-miércoles-viernes desde el lunes 2/11. Entrenó 2, 4, 6, el
 * sábado 7 (extra), 9, faltó el miércoles 11, volvió el 13 y el 16; hoy aún no entrena. También entrenó el 29/10,
 * antes de tener plan.
 */
class CalculadoraConstanciaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);
    private static final List<PlanVigente> PLAN_LMV =
            List.of(new PlanVigente(LocalDate.of(2026, 11, 2), EnumSet.of(MONDAY, WEDNESDAY, FRIDAY)));
    private static final Set<LocalDate> ENTRENADOS = Set.of(
            LocalDate.of(2026, 10, 29),
            LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 6),
            LocalDate.of(2026, 11, 7),
            LocalDate.of(2026, 11, 9),
            LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 16));

    private static EstadoDia estado(int mes, int dia) {
        return CalculadoraConstancia.estado(LocalDate.of(2026, mes, dia), ENTRENADOS, PLAN_LMV, HOY);
    }

    @Test
    void clasificaCadaDiaSegunElPlanYLoEntrenado() {
        assertThat(estado(11, 2)).isEqualTo(EstadoDia.ENTRENADO);
        assertThat(estado(11, 7)).as("sábado entrenado fuera del plan").isEqualTo(EstadoDia.EXTRA);
        assertThat(estado(11, 8)).as("domingo sin plan ni entreno").isEqualTo(EstadoDia.DESCANSO);
        assertThat(estado(11, 11)).as("miércoles del plan sin entrenar").isEqualTo(EstadoDia.FALLADO);
        assertThat(estado(11, 18)).as("hoy es día del plan y aún no entrena").isEqualTo(EstadoDia.PENDIENTE);
        assertThat(estado(11, 20)).as("viernes futuro").isEqualTo(EstadoDia.PLANIFICADO);
        assertThat(estado(11, 19)).as("jueves futuro").isEqualTo(EstadoDia.DESCANSO);
    }

    @Test
    void antesDelPrimerPlanNoHayDiasPlanificadosNiFallados() {
        assertThat(estado(10, 29)).as("entrenó antes de tener plan").isEqualTo(EstadoDia.EXTRA);
        assertThat(estado(10, 30)).as("viernes sin plan vigente").isEqualTo(EstadoDia.DESCANSO);
    }

    @Test
    void cadaDiaSeEvaluaConElPlanVigenteEseDia() {
        // Desde el lunes 16 solo entrena los lunes: el miércoles 11 sigue fallado (plan anterior), hoy ya no toca.
        List<PlanVigente> planes = List.of(
                PLAN_LMV.getFirst(),
                new PlanVigente(LocalDate.of(2026, 11, 16), EnumSet.of(MONDAY)));

        assertThat(CalculadoraConstancia.estado(LocalDate.of(2026, 11, 11), ENTRENADOS, planes, HOY))
                .isEqualTo(EstadoDia.FALLADO);
        assertThat(CalculadoraConstancia.estado(HOY, ENTRENADOS, planes, HOY)).isEqualTo(EstadoDia.DESCANSO);
    }

    @Test
    void elCalendarioTieneUnDiaPorCadaDiaDelMes() {
        List<DiaConstanciaDto> noviembre = CalculadoraConstancia.calendario(YearMonth.of(2026, 11), ENTRENADOS,
                PLAN_LMV, HOY);

        assertThat(noviembre).hasSize(30);
        assertThat(noviembre.getFirst().fecha()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(noviembre.get(10)).isEqualTo(new DiaConstanciaDto(LocalDate.of(2026, 11, 11), EstadoDia.FALLADO));
    }

    @Test
    void resumenCuentaEntrenosFallosYCumplimientoHastaHoy() {
        ResumenConstanciaDto noviembre = CalculadoraConstancia.resumen(LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30), ENTRENADOS, PLAN_LMV, HOY);

        // 7 días entrenados (6 del plan + el sábado extra), 1 fallado; hoy (pendiente) y el futuro no cuentan.
        assertThat(noviembre.entrenados()).isEqualTo(7);
        assertThat(noviembre.fallados()).isEqualTo(1);
        assertThat(noviembre.cumplimiento()).as("6 de 7 días del plan").isEqualTo(86);
    }

    @Test
    void sinDiasDelPlanEnElPeriodoElCumplimientoEsNulo() {
        ResumenConstanciaDto octubre = CalculadoraConstancia.resumen(LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31), ENTRENADOS, PLAN_LMV, HOY);

        assertThat(octubre).isEqualTo(new ResumenConstanciaDto(1, 0, null));
    }

    @Test
    void laRachaSumaDiasEntrenadosYSeCortaAlFallarUnDiaDelPlan() {
        Rachas rachas = CalculadoraConstancia.rachas(ENTRENADOS, PLAN_LMV, HOY);

        // 2, 4, 6, 7 (extra), 9 → 5 seguidos; falla el 11; 13 y 16 → racha actual 2. Hoy pendiente no la corta.
        assertThat(rachas).isEqualTo(new Rachas(2, 5));
    }

    @Test
    void sinPlanNoHayRacha() {
        assertThat(CalculadoraConstancia.rachas(ENTRENADOS, List.of(), HOY)).isNull();
        assertThat(CalculadoraConstancia.estado(LocalDate.of(2026, 11, 11), ENTRENADOS, List.of(), HOY))
                .isEqualTo(EstadoDia.DESCANSO);
    }

    @Test
    void elHistorialEmpiezaEnElPrimerEntrenoOPlan() {
        assertThat(CalculadoraConstancia.inicioHistorial(ENTRENADOS, PLAN_LMV, HOY)).isEqualTo(LocalDate.of(2026, 10, 29));
        assertThat(CalculadoraConstancia.inicioHistorial(Set.of(), List.of(), HOY)).isEqualTo(HOY);
    }
}
