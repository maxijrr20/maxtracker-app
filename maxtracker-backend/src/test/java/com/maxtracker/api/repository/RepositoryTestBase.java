package com.maxtracker.api.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Usuario;

/**
 * Base para los tests de integración de repositorios: usa el H2 en modo PostgreSQL del perfil "test"
 * con el esquema creado por las migraciones de Flyway (no por Hibernate). Cada test hace rollback.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class RepositoryTestBase {

    @Autowired
    protected TestEntityManager em;

    protected Usuario persistirUsuario(String email) {
        return em.persist(new Usuario(email, "$2a$10$hashDePrueba"));
    }

    protected Ejercicio persistirEjercicioWger(long wgerId, String nombre) {
        return em.persist(Ejercicio.desdeWger(wgerId, nombre, "Pecho"));
    }

    /** Escribe los cambios pendientes y vacía el contexto para que las lecturas vayan a la base de datos. */
    protected void sincronizar() {
        em.flush();
        em.clear();
    }
}
