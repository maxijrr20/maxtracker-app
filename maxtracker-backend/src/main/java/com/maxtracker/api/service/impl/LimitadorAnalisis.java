package com.maxtracker.api.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.maxtracker.api.exception.AnalisisDemasiadoFrecuenteException;

/**
 * Intervalo mínimo entre análisis de un mismo usuario: evita que un doble toque o un reintento rápido gasten
 * la cuota gratuita del proveedor de IA. En memoria (una instancia del backend): si se escala a varias,
 * mover a un almacenamiento compartido.
 */
@Component
class LimitadorAnalisis {

    private final Duration intervaloMinimo;
    private final Clock clock;
    private final Map<Long, Instant> ultimoPorUsuario = new ConcurrentHashMap<>();

    LimitadorAnalisis(@Value("${app.analisis.intervalo-minimo:PT20S}") Duration intervaloMinimo, Clock clock) {
        this.intervaloMinimo = intervaloMinimo;
        this.clock = clock;
    }

    /**
     * Registra un análisis ahora, de forma atómica.
     *
     * @throws AnalisisDemasiadoFrecuenteException si el anterior fue hace menos del intervalo mínimo
     */
    void registrar(long usuarioId) {
        Instant ahora = clock.instant();
        ultimoPorUsuario.compute(usuarioId, (id, anterior) -> {
            if (anterior != null) {
                Duration transcurrido = Duration.between(anterior, ahora);
                if (transcurrido.compareTo(intervaloMinimo) < 0) {
                    throw new AnalisisDemasiadoFrecuenteException(intervaloMinimo.minus(transcurrido));
                }
            }
            return ahora;
        });
    }
}
