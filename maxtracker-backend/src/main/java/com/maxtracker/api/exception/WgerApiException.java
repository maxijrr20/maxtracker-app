package com.maxtracker.api.exception;

/** La API externa de wger falló, no respondió a tiempo o devolvió datos inválidos. Se responde como 502. */
public class WgerApiException extends RuntimeException {

    public WgerApiException(String message) {
        super(message);
    }

    public WgerApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
