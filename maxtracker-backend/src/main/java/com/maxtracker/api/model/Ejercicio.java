package com.maxtracker.api.model;

import java.time.Instant;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Ejercicio usado en rutinas y series. Puede ser:
 * <ul>
 *   <li>una copia local de un ejercicio de wger ({@code wgerId} no nulo, sin usuario), o</li>
 *   <li>un ejercicio personalizado de un usuario ({@code usuario} no nulo, sin {@code wgerId}).</li>
 * </ul>
 * La base de datos garantiza que se cumpla exactamente una de las dos (CHECK {@code ck_ejercicio_origen}).
 */
@Entity
@Table(name = "ejercicio")
public class Ejercicio extends EntidadBase {

    @Column(name = "wger_id", unique = true)
    private Long wgerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 50)
    private String categoria;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected Ejercicio() {
        // Requerido por JPA
    }

    private Ejercicio(Long wgerId, Usuario usuario, String nombre, String categoria) {
        this.wgerId = wgerId;
        this.usuario = usuario;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public static Ejercicio desdeWger(long wgerId, String nombre, String categoria) {
        return new Ejercicio(wgerId, null, nombre, categoria);
    }

    public static Ejercicio personalizado(Usuario usuario, String nombre, String categoria) {
        return new Ejercicio(null, Objects.requireNonNull(usuario, "usuario"), nombre, categoria);
    }

    public boolean esPersonalizado() {
        return usuario != null;
    }

    public Long getWgerId() {
        return wgerId;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
