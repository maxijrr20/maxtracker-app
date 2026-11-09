package com.maxtracker.api.model;

import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Identificador autogenerado y {@code equals}/{@code hashCode} basados en él,
 * seguros con proxies de Hibernate y con entidades aún no persistidas.
 */
@MappedSuperclass
public abstract class EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    @Override
    public final boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || claseReal(this) != claseReal(otro)) {
            return false;
        }
        Long idOtro = ((EntidadBase) otro).getId();
        return getId() != null && getId().equals(idOtro);
    }

    @Override
    public final int hashCode() {
        return claseReal(this).hashCode();
    }

    private static Class<?> claseReal(Object objeto) {
        return objeto instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : objeto.getClass();
    }
}
