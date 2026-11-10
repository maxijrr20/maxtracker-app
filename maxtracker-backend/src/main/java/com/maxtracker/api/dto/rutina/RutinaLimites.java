package com.maxtracker.api.dto.rutina;

/** Límites compartidos por los DTOs de rutina. */
final class RutinaLimites {

    /** Evita peticiones enormes (cada ejercicio puede requerir una consulta a wger). */
    static final int MAX_EJERCICIOS = 30;

    private RutinaLimites() {
    }
}
