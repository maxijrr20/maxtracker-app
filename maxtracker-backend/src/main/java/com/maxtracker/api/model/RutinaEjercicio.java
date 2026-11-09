package com.maxtracker.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Ejercicio dentro de una rutina, con su posición y objetivos. Se gestiona a través de {@link Rutina}. */
@Entity
@Table(name = "rutina_ejercicio")
public class RutinaEjercicio extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rutina_id", nullable = false)
    private Rutina rutina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(nullable = false)
    private short orden;

    @Column(name = "series_objetivo")
    private Short seriesObjetivo;

    @Column(name = "repeticiones_objetivo")
    private Short repeticionesObjetivo;

    protected RutinaEjercicio() {
        // Requerido por JPA
    }

    RutinaEjercicio(Rutina rutina, Ejercicio ejercicio, int orden, Integer seriesObjetivo,
            Integer repeticionesObjetivo) {
        this.rutina = rutina;
        this.ejercicio = ejercicio;
        setOrden(orden);
        this.seriesObjetivo = aShort(seriesObjetivo);
        this.repeticionesObjetivo = aShort(repeticionesObjetivo);
    }

    private static Short aShort(Integer valor) {
        return valor == null ? null : valor.shortValue();
    }

    public Rutina getRutina() {
        return rutina;
    }

    public Ejercicio getEjercicio() {
        return ejercicio;
    }

    public int getOrden() {
        return orden;
    }

    void setOrden(int orden) {
        this.orden = (short) orden;
    }

    public Integer getSeriesObjetivo() {
        return seriesObjetivo == null ? null : seriesObjetivo.intValue();
    }

    public void setSeriesObjetivo(Integer seriesObjetivo) {
        this.seriesObjetivo = aShort(seriesObjetivo);
    }

    public Integer getRepeticionesObjetivo() {
        return repeticionesObjetivo == null ? null : repeticionesObjetivo.intValue();
    }

    public void setRepeticionesObjetivo(Integer repeticionesObjetivo) {
        this.repeticionesObjetivo = aShort(repeticionesObjetivo);
    }
}
