package com.maxtracker.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.maxtracker.api.exception.SesionFinalizadaException;

/** Reglas de dominio de la sesión, sin base de datos. */
class SesionEntrenamientoTest {

    private static final Instant INICIO = Instant.parse("2026-11-10T21:00:00Z");
    private static final Usuario MAX = new Usuario("max@ejemplo.cl", "$2a$10$hash");
    private static final Ejercicio BANCA = Ejercicio.desdeWger(192, "Press de banca", "Pecho");
    private static final Ejercicio REMO = Ejercicio.desdeWger(106, "Remo con barra", "Espalda");

    private static SesionEntrenamiento nueva() {
        return new SesionEntrenamiento(MAX, LocalDate.of(2026, 11, 10), INICIO);
    }

    @Test
    void numeraLasSeriesPorEjercicio() {
        SesionEntrenamiento sesion = nueva();

        assertThat(sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null).getNumeroSerie()).isEqualTo(1);
        assertThat(sesion.registrarSerie(REMO, BigDecimal.TEN, 5, null).getNumeroSerie()).isEqualTo(1);
        assertThat(sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null).getNumeroSerie()).isEqualTo(2);
    }

    @Test
    void trasQuitarUnaSerieIntermediaElSiguienteNumeroNoSeRepite() {
        SesionEntrenamiento sesion = nueva();
        sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null);
        SerieRegistro segunda = sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null);
        sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null);

        sesion.quitarSerie(segunda);

        assertThat(sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null).getNumeroSerie()).isEqualTo(4);
    }

    @Test
    void empiezaEnCursoYSeFinalizaUnaSolaVez() {
        SesionEntrenamiento sesion = nueva();
        assertThat(sesion.estaFinalizada()).isFalse();
        assertThat(sesion.getIniciadaEn()).isEqualTo(INICIO);

        sesion.finalizar(INICIO.plusSeconds(3600));

        assertThat(sesion.estaFinalizada()).isTrue();
        assertThat(sesion.getFinalizadaEn()).isEqualTo(INICIO.plusSeconds(3600));
        assertThatThrownBy(() -> sesion.finalizar(INICIO.plusSeconds(7200)))
                .isInstanceOf(SesionFinalizadaException.class)
                .hasMessage("La sesión ya está finalizada");
    }

    @Test
    void unaSesionFinalizadaNoAdmiteCambiosEnSeries() {
        SesionEntrenamiento sesion = nueva();
        SerieRegistro serie = sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null);
        sesion.finalizar(INICIO.plusSeconds(60));

        assertThatThrownBy(() -> sesion.registrarSerie(BANCA, BigDecimal.TEN, 5, null))
                .isInstanceOf(SesionFinalizadaException.class);
        assertThatThrownBy(() -> sesion.quitarSerie(serie)).isInstanceOf(SesionFinalizadaException.class);
        assertThat(sesion.getSeries()).hasSize(1);
    }

    @Test
    void finalizarNuncaQuedaAntesDelInicio() {
        SesionEntrenamiento sesion = nueva();

        sesion.finalizar(INICIO.minusSeconds(5));

        assertThat(sesion.getFinalizadaEn()).isEqualTo(INICIO);
    }
}
