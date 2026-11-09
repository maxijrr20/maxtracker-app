package com.maxtracker.api.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Una serie realizada: peso (kg), repeticiones y RIR opcional. Se gestiona a través de {@link SesionEntrenamiento}. */
@Entity
@Table(name = "serie_registro")
public class SerieRegistro extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionEntrenamiento sesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(name = "numero_serie", nullable = false)
    private short numeroSerie;

    @Column(name = "peso_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(nullable = false)
    private short repeticiones;

    /** Repeticiones en reserva (0–10). */
    @Column
    private Short rir;

    protected SerieRegistro() {
        // Requerido por JPA
    }

    SerieRegistro(SesionEntrenamiento sesion, Ejercicio ejercicio, int numeroSerie, BigDecimal pesoKg,
            int repeticiones, Integer rir) {
        this.sesion = sesion;
        this.ejercicio = ejercicio;
        this.numeroSerie = (short) numeroSerie;
        this.pesoKg = pesoKg;
        this.repeticiones = (short) repeticiones;
        setRir(rir);
    }

    public SesionEntrenamiento getSesion() {
        return sesion;
    }

    public Ejercicio getEjercicio() {
        return ejercicio;
    }

    public int getNumeroSerie() {
        return numeroSerie;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(int repeticiones) {
        this.repeticiones = (short) repeticiones;
    }

    public Integer getRir() {
        return rir == null ? null : rir.intValue();
    }

    public void setRir(Integer rir) {
        this.rir = rir == null ? null : rir.shortValue();
    }
}
