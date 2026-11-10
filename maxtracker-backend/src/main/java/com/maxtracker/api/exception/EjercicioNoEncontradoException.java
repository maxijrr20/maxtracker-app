package com.maxtracker.api.exception;

/** El ejercicio solicitado no existe en el catálogo (o es un personalizado de otro usuario). Se responde como 404. */
public class EjercicioNoEncontradoException extends RecursoNoEncontradoException {

    public EjercicioNoEncontradoException(long id) {
        super("No existe un ejercicio con id " + id);
    }
}
