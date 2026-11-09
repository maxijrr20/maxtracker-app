package com.maxtracker.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.maxtracker.api.model.Usuario;

class UsuarioRepositoryTest extends RepositoryTestBase {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void guardaYConsultaPorEmailSinDistinguirMayusculas() {
        Usuario guardado = repository.save(new Usuario("  Max@Ejemplo.CL ", "hash"));
        sincronizar();

        assertThat(guardado.getId()).isNotNull();
        assertThat(repository.buscarPorEmail("MAX@ejemplo.cl")).hasValueSatisfying(u -> {
            assertThat(u.getEmail()).isEqualTo("max@ejemplo.cl");
            assertThat(u.getPasswordHash()).isEqualTo("hash");
            assertThat(u.getCreadoEn()).isNotNull();
        });
        assertThat(repository.existeEmail("max@EJEMPLO.cl")).isTrue();
        assertThat(repository.buscarPorEmail("otro@ejemplo.cl")).isEmpty();
    }

    @Test
    void rechazaEmailDuplicado() {
        repository.saveAndFlush(new Usuario("max@ejemplo.cl", "hash"));

        assertThatThrownBy(() -> repository.saveAndFlush(new Usuario("MAX@ejemplo.cl", "otro")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
