package com.maxtracker.api.exception;

/**
 * El recurso no existe o pertenece a otro usuario. Se responde como 404 en ambos casos,
 * para no revelar qué IDs existen.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException sesion(long id) {
        return new RecursoNoEncontradoException("No existe una sesión con id " + id);
    }

    public static RecursoNoEncontradoException serie(long id) {
        return new RecursoNoEncontradoException("No existe una serie con id " + id + " en esta sesión");
    }

    public static RecursoNoEncontradoException rutina(long id) {
        return new RecursoNoEncontradoException("No existe una rutina con id " + id);
    }
}
