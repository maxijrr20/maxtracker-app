package com.maxtracker.api.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Entrenamiento realizado en una fecha; opcionalmente basado en una {@link Rutina}. */
@Entity
@Table(name = "sesion_entrenamiento")
public class SesionEntrenamiento extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Si se borra la rutina, la sesión se conserva con {@code rutina_id = NULL}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id")
    private Rutina rutina;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 1000)
    private String notas;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SerieRegistro> series = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected SesionEntrenamiento() {
        // Requerido por JPA
    }

    public SesionEntrenamiento(Usuario usuario, LocalDate fecha) {
        this.usuario = usuario;
        this.fecha = fecha;
    }

    /** Registra una serie; el número de serie es correlativo por ejercicio dentro de la sesión. */
    public SerieRegistro registrarSerie(Ejercicio ejercicio, BigDecimal pesoKg, int repeticiones, Integer rir) {
        int numeroSerie = (int) series.stream().filter(s -> s.getEjercicio().equals(ejercicio)).count() + 1;
        SerieRegistro serie = new SerieRegistro(this, ejercicio, numeroSerie, pesoKg, repeticiones, rir);
        series.add(serie);
        return serie;
    }

    public void quitarSerie(SerieRegistro serie) {
        series.remove(serie);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Rutina getRutina() {
        return rutina;
    }

    public void setRutina(Rutina rutina) {
        this.rutina = rutina;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public List<SerieRegistro> getSeries() {
        return Collections.unmodifiableList(series);
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
