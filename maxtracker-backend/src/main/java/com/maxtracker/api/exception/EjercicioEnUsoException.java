package com.maxtracker.api.exception;

/**
 * El ejercicio propio aparece en una rutina o en series registradas: borrarlo dejaría historial huérfano
 * (la FK no tiene cascada a propósito). Se responde como 409.
 */
public class EjercicioEnUsoException extends RuntimeException {

    public EjercicioEnUsoException(String nombre) {
        super("\"" + nombre + "\" se usa en una rutina o en tu historial; quítalo de ahí antes de eliminarlo");
    }
}
