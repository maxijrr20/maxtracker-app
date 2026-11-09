package com.maxtracker.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Rutina;
import com.maxtracker.api.model.SerieRegistro;
import com.maxtracker.api.model.SesionEntrenamiento;
import com.maxtracker.api.model.Usuario;

class SesionEntrenamientoRepositoryTest extends RepositoryTestBase {

    @Autowired
    private SesionEntrenamientoRepository sesiones;

    @Autowired
    private SerieRegistroRepository series;

    @Autowired
    private RutinaRepository rutinas;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void guardaLaSesionConSusSeriesEnCascada() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Ejercicio banca = persistirEjercicioWger(192, "Press de banca");
        Ejercicio remo = persistirEjercicioWger(106, "Remo con barra");

        SesionEntrenamiento sesion = new SesionEntrenamiento(max, LocalDate.of(2026, 11, 10));
        sesion.setNotas("Buen día, banca subió 2,5 kg");
        sesion.registrarSerie(banca, new BigDecimal("80.00"), 6, 2);
        sesion.registrarSerie(remo, new BigDecimal("70.5"), 8, null);
        sesion.registrarSerie(banca, new BigDecimal("82.50"), 5, 1);
        Long id = sesiones.save(sesion).getId();
        sincronizar();

        SesionEntrenamiento leida = sesiones.findConSeriesByIdAndUsuarioId(id, max.getId()).orElseThrow();
        assertThat(leida.getFecha()).isEqualTo(LocalDate.of(2026, 11, 10));
        assertThat(leida.getNotas()).isEqualTo("Buen día, banca subió 2,5 kg");
        assertThat(leida.getCreadoEn()).isNotNull();
        assertThat(leida.getSeries())
                .extracting(s -> s.getEjercicio().getNombre(), SerieRegistro::getNumeroSerie,
                        SerieRegistro::getPesoKg, SerieRegistro::getRepeticiones, SerieRegistro::getRir)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Press de banca", 1, new BigDecimal("80.00"), 6, 2),
                        org.assertj.core.groups.Tuple.tuple("Remo con barra", 1, new BigDecimal("70.50"), 8, null),
                        org.assertj.core.groups.Tuple.tuple("Press de banca", 2, new BigDecimal("82.50"), 5, 1));
    }

    @Test
    void paginaElHistorialDelUsuarioDelMasRecienteAlMasAntiguo() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Usuario otro = persistirUsuario("otro@ejemplo.cl");
        sesiones.save(new SesionEntrenamiento(max, LocalDate.of(2026, 11, 3)));
        sesiones.save(new SesionEntrenamiento(max, LocalDate.of(2026, 11, 10)));
        sesiones.save(new SesionEntrenamiento(max, LocalDate.of(2026, 11, 5)));
        sesiones.save(new SesionEntrenamiento(otro, LocalDate.of(2026, 11, 11)));
        sincronizar();

        Page<SesionEntrenamiento> pagina =
                sesiones.findByUsuarioIdOrderByFechaDescIdDesc(max.getId(), PageRequest.of(0, 2));

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getContent())
                .extracting(SesionEntrenamiento::getFecha)
                .containsExactly(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 5));
    }

    @Test
    void elHistorialDeUnEjercicioVieneEnOrdenCronologico() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Ejercicio banca = persistirEjercicioWger(192, "Press de banca");
        Ejercicio remo = persistirEjercicioWger(106, "Remo con barra");

        SesionEntrenamiento semana2 = new SesionEntrenamiento(max, LocalDate.of(2026, 11, 10));
        semana2.registrarSerie(banca, new BigDecimal("82.50"), 6, null);
        semana2.registrarSerie(banca, new BigDecimal("82.50"), 5, null);
        SesionEntrenamiento semana1 = new SesionEntrenamiento(max, LocalDate.of(2026, 11, 3));
        semana1.registrarSerie(banca, new BigDecimal("80.00"), 6, null);
        semana1.registrarSerie(remo, new BigDecimal("70.00"), 8, null);
        sesiones.save(semana2);
        sesiones.save(semana1);
        sincronizar();

        assertThat(series.findHistorial(max.getId(), banca.getId()))
                .extracting(s -> s.getSesion().getFecha(), SerieRegistro::getNumeroSerie, SerieRegistro::getPesoKg)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(LocalDate.of(2026, 11, 3), 1, new BigDecimal("80.00")),
                        org.assertj.core.groups.Tuple.tuple(LocalDate.of(2026, 11, 10), 1, new BigDecimal("82.50")),
                        org.assertj.core.groups.Tuple.tuple(LocalDate.of(2026, 11, 10), 2, new BigDecimal("82.50")));
    }

    @Test
    void borrarLaSesionBorraSusSeries() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        SesionEntrenamiento sesion = new SesionEntrenamiento(max, LocalDate.of(2026, 11, 10));
        sesion.registrarSerie(persistirEjercicioWger(192, "Press de banca"), new BigDecimal("80"), 6, null);
        Long id = sesiones.save(sesion).getId();
        sincronizar();

        sesiones.deleteById(id);
        sincronizar();

        assertThat(series.count()).isZero();
    }

    @Test
    void borrarLaRutinaConservaLasSesionesQueLaUsaron() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Rutina rutina = rutinas.save(new Rutina(max, "Torso A", null));
        SesionEntrenamiento sesion = new SesionEntrenamiento(max, LocalDate.of(2026, 11, 10));
        sesion.setRutina(rutina);
        Long sesionId = sesiones.save(sesion).getId();
        sincronizar();

        // ON DELETE SET NULL se aplica en la base de datos
        jdbc.update("delete from rutina where id = ?", rutina.getId());
        sincronizar();

        assertThat(sesiones.findById(sesionId)).hasValueSatisfying(s -> assertThat(s.getRutina()).isNull());
    }

    @Test
    void laBaseDeDatosRechazaSeriesInvalidas() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Ejercicio banca = persistirEjercicioWger(192, "Press de banca");

        SesionEntrenamiento pesoNegativo = new SesionEntrenamiento(max, LocalDate.of(2026, 11, 10));
        pesoNegativo.registrarSerie(banca, new BigDecimal("-5"), 6, null);
        assertThatThrownBy(() -> sesiones.saveAndFlush(pesoNegativo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laBaseDeDatosRechazaRirFueraDeRango() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Ejercicio banca = persistirEjercicioWger(192, "Press de banca");

        SesionEntrenamiento sesion = new SesionEntrenamiento(max, LocalDate.of(2026, 11, 10));
        sesion.registrarSerie(banca, new BigDecimal("80"), 6, 11);
        assertThatThrownBy(() -> sesiones.saveAndFlush(sesion))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
