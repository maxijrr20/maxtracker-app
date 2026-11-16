package com.maxtracker.api.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Días de la semana que el usuario planea entrenar, vigente desde una fecha. Cada cambio de plan es una fila nueva
 * (historial), de modo que los días pasados se evalúan con el plan que regía entonces.
 */
@Entity
@Table(name = "plan_entrenamiento")
public class PlanEntrenamiento extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Máscara de bits ISO-8601: bit 0 = lunes ... bit 6 = domingo. */
    @Column(name = "dias_semana", nullable = false)
    private short diasSemana;

    @Column(name = "vigente_desde", nullable = false, updatable = false)
    private LocalDate vigenteDesde;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected PlanEntrenamiento() {
        // Requerido por JPA
    }

    public PlanEntrenamiento(Usuario usuario, Collection<DayOfWeek> dias, LocalDate vigenteDesde) {
        this.usuario = Objects.requireNonNull(usuario, "usuario");
        this.vigenteDesde = Objects.requireNonNull(vigenteDesde, "vigenteDesde");
        setDias(dias);
    }

    public static short aMascara(Collection<DayOfWeek> dias) {
        if (dias == null || dias.isEmpty()) {
            throw new IllegalArgumentException("El plan debe tener al menos un día");
        }
        int mascara = 0;
        for (DayOfWeek dia : dias) {
            mascara |= 1 << (dia.getValue() - 1);
        }
        return (short) mascara;
    }

    public static Set<DayOfWeek> desdeMascara(int mascara) {
        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek dia : DayOfWeek.values()) {
            if ((mascara & (1 << (dia.getValue() - 1))) != 0) {
                dias.add(dia);
            }
        }
        return dias;
    }

    public Set<DayOfWeek> getDias() {
        return desdeMascara(diasSemana);
    }

    public void setDias(Collection<DayOfWeek> dias) {
        this.diasSemana = aMascara(dias);
    }

    public boolean mismosDias(Collection<DayOfWeek> dias) {
        return diasSemana == aMascara(dias);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
