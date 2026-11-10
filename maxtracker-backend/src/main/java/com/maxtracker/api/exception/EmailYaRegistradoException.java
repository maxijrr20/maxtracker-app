package com.maxtracker.api.exception;

/** Ya existe una cuenta con ese email. Se responde como 409. */
public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException() {
        super("Ya existe una cuenta con ese email");
    }
}
