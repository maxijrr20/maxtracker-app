package com.maxtracker.api.exception;

/** El ejercicio solicitado no existe en el catálogo. Se responde como 404. */
public class EjercicioNoEncontradoException extends RuntimeException {

    public EjercicioNoEncontradoException(long id) {
        super("No existe un ejercicio con id " + id);
    }
}
