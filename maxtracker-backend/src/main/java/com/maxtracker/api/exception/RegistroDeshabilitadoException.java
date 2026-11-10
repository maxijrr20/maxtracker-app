package com.maxtracker.api.exception;

/** El registro de cuentas está desactivado ({@code AUTH_REGISTRO_HABILITADO=false}). Se responde como 403. */
public class RegistroDeshabilitadoException extends RuntimeException {

    public RegistroDeshabilitadoException() {
        super("El registro de nuevas cuentas está deshabilitado");
    }
}
