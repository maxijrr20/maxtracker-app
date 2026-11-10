package com.maxtracker.api.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.annotations.CreationTimestamp;

import com.maxtracker.api.exception.SesionFinalizadaException;

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

    @Column(name = "iniciada_en", nullable = false, updatable = false)
    private Instant iniciadaEn;

    /** {@code null} mientras la sesión está en curso. */
    @Column(name = "finalizada_en")
    private Instant finalizadaEn;

    protected SesionEntrenamiento() {
        // Requerido por JPA
    }

    public SesionEntrenamiento(Usuario usuario, LocalDate fecha) {
        this(usuario, fecha, Instant.now());
    }

    public SesionEntrenamiento(Usuario usuario, LocalDate fecha, Instant iniciadaEn) {
        this.usuario = usuario;
        this.fecha = fecha;
        this.iniciadaEn = Objects.requireNonNull(iniciadaEn, "iniciadaEn");
    }

    /**
     * Registra una serie. El número de serie es correlativo por ejercicio dentro de la sesión: siguiente al mayor
     * existente (no a la cantidad), así borrar una serie intermedia no genera un número repetido.
     *
     * @throws SesionFinalizadaException si la sesión ya está finalizada
     */
    public SerieRegistro registrarSerie(Ejercicio ejercicio, BigDecimal pesoKg, int repeticiones, Integer rir) {
        exigirEnCurso();
        int numeroSerie = series.stream()
                .filter(s -> s.getEjercicio().equals(ejercicio))
                .mapToInt(SerieRegistro::getNumeroSerie)
                .max()
                .orElse(0) + 1;
        SerieRegistro serie = new SerieRegistro(this, ejercicio, numeroSerie, pesoKg, repeticiones, rir);
        series.add(serie);
        return serie;
    }

    /** @throws SesionFinalizadaException si la sesión ya está finalizada */
    public void quitarSerie(SerieRegistro serie) {
        exigirEnCurso();
        series.remove(serie);
    }

    public Optional<SerieRegistro> buscarSerie(long serieId) {
        return series.stream().filter(s -> s.getId() != null && s.getId() == serieId).findFirst();
    }

    /** @throws SesionFinalizadaException si ya estaba finalizada */
    public void finalizar(Instant instante) {
        if (estaFinalizada()) {
            throw SesionFinalizadaException.yaFinalizada();
        }
        // El reloj podría ir levemente atrás respecto del inicio; el CHECK de la BD exige finalizada >= iniciada.
        this.finalizadaEn = instante.isBefore(iniciadaEn) ? iniciadaEn : instante;
    }

    public boolean estaFinalizada() {
        return finalizadaEn != null;
    }

    /** @throws SesionFinalizadaException si la sesión ya está finalizada */
    public void exigirEnCurso() {
        if (estaFinalizada()) {
            throw SesionFinalizadaException.noAdmiteCambiosEnSeries();
        }
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

    public Instant getIniciadaEn() {
        return iniciadaEn;
    }

    public Instant getFinalizadaEn() {
        return finalizadaEn;
    }
}
