package com.maxtracker.api.exception;

/** El usuario ya tiene un ejercicio propio con ese nombre (sin distinguir mayúsculas). Se responde como 409. */
public class EjercicioPersonalizadoDuplicadoException extends RuntimeException {

    public EjercicioPersonalizadoDuplicadoException(String nombre) {
        super("Ya tienes un ejercicio llamado \"" + nombre + "\"");
    }
}
