package com.maxtracker.api.exception;

import java.time.Duration;

/**
 * El usuario pidió otro análisis antes del intervalo mínimo (protege la cuota gratuita de la IA de dobles toques).
 * Se responde como 429 con {@code Retry-After}.
 */
public class AnalisisDemasiadoFrecuenteException extends RuntimeException {

    private final Duration espera;

    public AnalisisDemasiadoFrecuenteException(Duration espera) {
        super("Ya pediste un análisis hace un momento. Espera " + Math.max(1, espera.toSeconds())
                + " segundos e intenta nuevamente");
        this.espera = espera;
    }

    public Duration getEspera() {
        return espera;
    }
}
