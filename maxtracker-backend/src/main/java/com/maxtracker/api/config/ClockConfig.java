package com.maxtracker.api.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj de la aplicación. Los instantes ({@code Instant}) no dependen de la zona; la zona solo se usa para
 * calcular la fecha "de hoy" del usuario (p. ej. una sesión iniciada a las 22:00 en Chile es de ese día,
 * aunque en UTC ya sea el siguiente). Inyectarlo permite fijar el tiempo en los tests.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${app.zona-horaria:America/Santiago}") String zonaHoraria) {
        return Clock.system(ZoneId.of(zonaHoraria));
    }
}
