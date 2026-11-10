package com.maxtracker.api.exception;

/** La operación no se permite porque la sesión ya está finalizada. Se responde como 409. */
public class SesionFinalizadaException extends RuntimeException {

    private SesionFinalizadaException(String mensaje) {
        super(mensaje);
    }

    public static SesionFinalizadaException yaFinalizada() {
        return new SesionFinalizadaException("La sesión ya está finalizada");
    }

    public static SesionFinalizadaException noAdmiteCambiosEnSeries() {
        return new SesionFinalizadaException("La sesión está finalizada: no se pueden agregar ni quitar series");
    }
}
