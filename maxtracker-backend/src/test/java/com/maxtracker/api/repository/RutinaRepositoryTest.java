package com.maxtracker.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Rutina;
import com.maxtracker.api.model.RutinaEjercicio;
import com.maxtracker.api.model.Usuario;

class RutinaRepositoryTest extends RepositoryTestBase {

    @Autowired
    private RutinaRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void guardaLaRutinaConSusEjerciciosEnCascadaYLosDevuelveOrdenados() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Ejercicio banca = persistirEjercicioWger(192, "Press de banca");
        Ejercicio remo = persistirEjercicioWger(106, "Remo con barra");
        Ejercicio dominadas = persistirEjercicioWger(475, "Dominadas");

        Rutina rutina = new Rutina(max, "Torso A", "Fuerza, 3 días por semana");
        rutina.agregarEjercicio(banca, 4, 6);
        rutina.agregarEjercicio(remo, 4, 8);
        rutina.agregarEjercicio(dominadas, 3, null);
        Long id = repository.save(rutina).getId();
        sincronizar();

        Rutina leida = repository.findConEjerciciosByIdAndUsuarioId(id, max.getId()).orElseThrow();
        assertThat(leida.getNombre()).isEqualTo("Torso A");
        assertThat(leida.getEjercicios())
                .extracting(RutinaEjercicio::getOrden, re -> re.getEjercicio().getNombre(),
                        RutinaEjercicio::getSeriesObjetivo, RutinaEjercicio::getRepeticionesObjetivo)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, "Press de banca", 4, 6),
                        org.assertj.core.groups.Tuple.tuple(2, "Remo con barra", 4, 8),
                        org.assertj.core.groups.Tuple.tuple(3, "Dominadas", 3, null));
    }

    @Test
    void noDevuelveRutinasDeOtroUsuario() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Usuario otro = persistirUsuario("otro@ejemplo.cl");
        Long id = repository.save(new Rutina(max, "Pierna", null)).getId();
        sincronizar();

        assertThat(repository.findConEjerciciosByIdAndUsuarioId(id, otro.getId())).isEmpty();
    }

    @Test
    void listaLasRutinasDelUsuarioPorNombre() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Usuario otro = persistirUsuario("otro@ejemplo.cl");
        repository.save(new Rutina(max, "Torso B", null));
        repository.save(new Rutina(max, "Pierna", null));
        repository.save(new Rutina(otro, "Full body", null));
        sincronizar();

        assertThat(repository.findByUsuarioIdOrderByNombreAsc(max.getId()))
                .extracting(Rutina::getNombre)
                .containsExactly("Pierna", "Torso B");
    }

    @Test
    void quitarUnEjercicioLoBorraYReenumeraElOrden() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Rutina rutina = new Rutina(max, "Torso A", null);
        rutina.agregarEjercicio(persistirEjercicioWger(192, "Press de banca"), 4, 6);
        rutina.agregarEjercicio(persistirEjercicioWger(106, "Remo con barra"), 4, 8);
        Long id = repository.save(rutina).getId();
        sincronizar();

        Rutina leida = repository.findConEjerciciosByIdAndUsuarioId(id, max.getId()).orElseThrow();
        leida.quitarEjercicio(leida.getEjercicios().get(0));
        sincronizar();

        assertThat(repository.findConEjerciciosByIdAndUsuarioId(id, max.getId()).orElseThrow().getEjercicios())
                .singleElement()
                .satisfies(re -> {
                    assertThat(re.getOrden()).isEqualTo(1);
                    assertThat(re.getEjercicio().getNombre()).isEqualTo("Remo con barra");
                });
        assertThat(jdbc.queryForObject("select count(*) from rutina_ejercicio", Integer.class)).isEqualTo(1);
    }

    @Test
    void borrarLaRutinaBorraSusEjerciciosPeroNoElCatalogo() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Rutina rutina = new Rutina(max, "Torso A", null);
        rutina.agregarEjercicio(persistirEjercicioWger(192, "Press de banca"), 4, 6);
        Long id = repository.save(rutina).getId();
        sincronizar();

        repository.deleteById(id);
        sincronizar();

        assertThat(jdbc.queryForObject("select count(*) from rutina_ejercicio", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from ejercicio", Integer.class)).isEqualTo(1);
    }
}
