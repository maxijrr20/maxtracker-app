package com.maxtracker.api.service.metricas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.maxtracker.api.dto.metricas.EjercicioConHistorialDto;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse.PuntoProgresoDto;
import com.maxtracker.api.dto.metricas.RecordPersonalDto;
import com.maxtracker.api.dto.metricas.ResumenMetricasResponse.PeriodoDto;
import com.maxtracker.api.dto.metricas.VolumenSemanalDto;
import com.maxtracker.api.repository.SerieHistorial;

class CalculadoraMetricasTest {

    private static final long BANCA = 1;
    private static final long REMO = 2;

    /** Miércoles. */
    private static final LocalDate HOY = LocalDate.of(2026, 11, 11);

    private static SerieHistorial serie(long sesionId, String fecha, long ejercicioId, String peso, int reps) {
        String nombre = ejercicioId == BANCA ? "Press de banca" : "Remo con barra";
        return new SerieHistorial(sesionId, LocalDate.parse(fecha), ejercicioId, 100 + ejercicioId, nombre, "Pecho",
                (short) 1, new BigDecimal(peso), (short) reps, null);
    }

    private static BigDecimal kg(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void epleyConUnaRepeticionEsElPesoYConVariasLoAumenta() {
        assertThat(CalculadoraMetricas.unoRmEpley(kg("100"), 1)).isEqualByComparingTo("100.00");
        // 100 × (1 + 10/30) = 133,33
        assertThat(CalculadoraMetricas.unoRmEpley(kg("100"), 10)).isEqualByComparingTo("133.33");
        // 80 × (1 + 6/30) = 96
        assertThat(CalculadoraMetricas.unoRmEpley(kg("80"), 6)).isEqualByComparingTo("96.00");
    }

    @Test
    void laSemanaEmpiezaEnLunes() {
        assertThat(CalculadoraMetricas.inicioSemana(HOY)).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(CalculadoraMetricas.inicioSemana(LocalDate.of(2026, 11, 9))).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(CalculadoraMetricas.inicioSemana(LocalDate.of(2026, 11, 15))).isEqualTo(LocalDate.of(2026, 11, 9));
    }

    @Test
    void periodoSumaVolumenYCuentaSesionesDistintasDentroDelRango() {
        List<SerieHistorial> series = List.of(
                serie(1, "2026-11-09", BANCA, "80", 6), // 480
                serie(1, "2026-11-09", REMO, "70", 8), // 560
                serie(2, "2026-11-11", BANCA, "82.5", 5), // 412,5
                serie(3, "2026-11-08", BANCA, "100", 1)); // fuera del rango

        PeriodoDto periodo = CalculadoraMetricas.periodo(series, LocalDate.of(2026, 11, 9), HOY);

        assertThat(periodo.volumenKg()).isEqualByComparingTo("1452.50");
        assertThat(periodo.sesiones()).isEqualTo(2);
        assertThat(periodo.series()).isEqualTo(3);
    }

    @Test
    void elPesoCorporalNoSumaVolumen() {
        PeriodoDto periodo = CalculadoraMetricas.periodo(List.of(serie(1, "2026-11-10", BANCA, "0", 15)),
                LocalDate.of(2026, 11, 9), HOY);

        assertThat(periodo.volumenKg()).isEqualByComparingTo("0");
        assertThat(periodo.series()).isEqualTo(1);
    }

    @Test
    void volumenSemanalIncluyeLasSemanasVaciasDeLaMasAntiguaALaActual() {
        List<SerieHistorial> series = List.of(
                serie(1, "2026-10-26", BANCA, "50", 10), // semana del 26-oct: 500
                serie(2, "2026-11-10", BANCA, "60", 10)); // semana actual (9-nov): 600

        List<VolumenSemanalDto> semanas = CalculadoraMetricas.volumenSemanal(series, HOY, 4);

        assertThat(semanas).extracting(VolumenSemanalDto::inicioSemana).containsExactly(
                LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 9));
        assertThat(semanas).extracting(VolumenSemanalDto::volumenKg)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(kg("0"), kg("500"), kg("0"), kg("600"));
        assertThat(semanas).extracting(VolumenSemanalDto::sesiones).containsExactly(0, 1, 0, 1);
    }

    @Test
    void recordEsElMayorPesoYAIgualPesoGananMasRepeticionesLograndoseLaPrimeraVez() {
        List<SerieHistorial> series = List.of(
                serie(1, "2026-10-01", BANCA, "80", 5),
                serie(2, "2026-10-08", BANCA, "85", 3),
                serie(3, "2026-10-15", BANCA, "85", 4), // mismo peso, más reps: es el récord
                serie(4, "2026-10-22", BANCA, "85", 4), // repite el récord: cuenta la primera vez
                serie(5, "2026-10-29", BANCA, "70", 12), // mejor 1RM estimado: 70 × 1,4 = 98
                serie(1, "2026-10-01", REMO, "60", 8));

        List<RecordPersonalDto> records = CalculadoraMetricas.records(series);

        assertThat(records).hasSize(2);
        RecordPersonalDto banca = records.get(0);
        assertThat(banca.ejercicioId()).isEqualTo(BANCA);
        assertThat(banca.pesoMaxKg()).isEqualByComparingTo("85");
        assertThat(banca.repeticiones()).isEqualTo(4);
        assertThat(banca.fecha()).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(banca.unoRmEstimadoKg()).isEqualByComparingTo("98.00");
        assertThat(banca.wgerId()).isEqualTo(101L);
        // Ordenados del récord más reciente al más antiguo.
        assertThat(records.get(1).ejercicioId()).isEqualTo(REMO);
    }

    @Test
    void progresoTieneUnPuntoPorSesionEnOrdenCronologico() {
        List<SerieHistorial> series = List.of(
                serie(2, "2026-11-05", BANCA, "82.5", 5),
                serie(1, "2026-11-01", BANCA, "80", 6),
                serie(1, "2026-11-01", BANCA, "80", 5),
                serie(2, "2026-11-05", BANCA, "85", 3));

        List<PuntoProgresoDto> puntos = CalculadoraMetricas.progreso(series);

        assertThat(puntos).extracting(PuntoProgresoDto::sesionId).containsExactly(1L, 2L);
        PuntoProgresoDto primero = puntos.get(0);
        assertThat(primero.pesoMaxKg()).isEqualByComparingTo("80");
        assertThat(primero.volumenKg()).isEqualByComparingTo("880"); // 480 + 400
        assertThat(primero.unoRmEstimadoKg()).isEqualByComparingTo("96.00"); // 80 × 1,2
        assertThat(primero.series()).isEqualTo(2);
        PuntoProgresoDto segundo = puntos.get(1);
        assertThat(segundo.pesoMaxKg()).isEqualByComparingTo("85");
        // max(82,5 × 1,1667 = 96,25; 85 × 1,1 = 93,5)
        assertThat(segundo.unoRmEstimadoKg()).isEqualByComparingTo("96.25");
    }

    @Test
    void progresoSinSeriesEsVacio() {
        assertThat(CalculadoraMetricas.progreso(List.of())).isEmpty();
        assertThat(CalculadoraMetricas.records(List.of())).isEmpty();
    }

    @Test
    void ejerciciosConHistorialDelMasRecienteAlMasAntiguo() {
        List<SerieHistorial> series = List.of(
                serie(1, "2026-11-01", BANCA, "80", 6),
                serie(1, "2026-11-01", REMO, "70", 8),
                serie(2, "2026-11-08", REMO, "72.5", 8));

        List<EjercicioConHistorialDto> ejercicios = CalculadoraMetricas.ejerciciosConHistorial(series);

        assertThat(ejercicios).extracting(EjercicioConHistorialDto::ejercicioId).containsExactly(REMO, BANCA);
        assertThat(ejercicios.get(0).series()).isEqualTo(2);
        assertThat(ejercicios.get(0).ultimaFecha()).isEqualTo(LocalDate.of(2026, 11, 8));
    }
}
