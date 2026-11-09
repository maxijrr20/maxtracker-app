package com.maxtracker.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Usuario;

class EjercicioRepositoryTest extends RepositoryTestBase {

    @Autowired
    private EjercicioRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void guardaYBuscaCopiaLocalDeEjercicioDeWger() {
        repository.save(Ejercicio.desdeWger(192, "Press de banca", "Pecho"));
        sincronizar();

        assertThat(repository.findByWgerId(192L)).hasValueSatisfying(e -> {
            assertThat(e.getNombre()).isEqualTo("Press de banca");
            assertThat(e.esPersonalizado()).isFalse();
        });
    }

    @Test
    void listaSoloLosEjerciciosPersonalizadosDelUsuarioOrdenadosPorNombre() {
        Usuario max = persistirUsuario("max@ejemplo.cl");
        Usuario otro = persistirUsuario("otro@ejemplo.cl");
        repository.save(Ejercicio.personalizado(max, "Remo en máquina del gym", "Espalda"));
        repository.save(Ejercicio.personalizado(max, "Curl araña", "Brazos"));
        repository.save(Ejercicio.personalizado(otro, "Hip thrust", "Piernas"));
        repository.save(Ejercicio.desdeWger(1, "Sentadilla", "Piernas"));
        sincronizar();

        assertThat(repository.findByUsuarioIdOrderByNombreAsc(max.getId()))
                .extracting(Ejercicio::getNombre)
                .containsExactly("Curl araña", "Remo en máquina del gym");
    }

    @Test
    void rechazaWgerIdDuplicado() {
        repository.saveAndFlush(Ejercicio.desdeWger(192, "Press de banca", "Pecho"));

        assertThatThrownBy(() -> repository.saveAndFlush(Ejercicio.desdeWger(192, "Bench press", "Chest")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laBaseDeDatosExigeQueElEjercicioSeaDeWgerOPersonalizado() {
        Long usuarioId = persistirUsuario("max@ejemplo.cl").getId();
        em.flush();

        // Ni wger ni usuario
        assertThatThrownBy(() -> jdbc.update("insert into ejercicio (nombre) values ('Huérfano')"))
                .isInstanceOf(DataIntegrityViolationException.class);
        // Ambos a la vez
        assertThatThrownBy(() -> jdbc.update(
                "insert into ejercicio (wger_id, usuario_id, nombre) values (5, ?, 'Ambiguo')", usuarioId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
