package com.maxtracker.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Arranca la aplicación completa: Flyway aplica las migraciones en H2 y Hibernate valida el esquema. */
@SpringBootTest
@ActiveProfiles("test")
class MaxtrackerBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
