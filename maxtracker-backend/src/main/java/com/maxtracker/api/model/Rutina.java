package com.maxtracker.api.model;

import java.time.Instant;
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

/** Plantilla de entrenamiento de un usuario: lista ordenada de ejercicios con series y repeticiones objetivo. */
@Entity
@Table(name = "rutina")
public class Rutina extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<RutinaEjercicio> ejercicios = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected Rutina() {
        // Requerido por JPA
    }

    public Rutina(Usuario usuario, String nombre, String descripcion) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Agrega un ejercicio al final de la rutina. */
    public RutinaEjercicio agregarEjercicio(Ejercicio ejercicio, Integer seriesObjetivo, Integer repeticionesObjetivo) {
        RutinaEjercicio item = new RutinaEjercicio(this, ejercicio, ejercicios.size() + 1,
                seriesObjetivo, repeticionesObjetivo);
        ejercicios.add(item);
        return item;
    }

    /** Quita un ejercicio y reenumera el orden de los restantes. */
    public void quitarEjercicio(RutinaEjercicio item) {
        if (ejercicios.remove(item)) {
            for (int i = 0; i < ejercicios.size(); i++) {
                ejercicios.get(i).setOrden(i + 1);
            }
        }
    }

    /** Quita todos los ejercicios (con {@code orphanRemoval} se borran sus filas al hacer flush). */
    public void quitarTodosLosEjercicios() {
        ejercicios.clear();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<RutinaEjercicio> getEjercicios() {
        return Collections.unmodifiableList(ejercicios);
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
