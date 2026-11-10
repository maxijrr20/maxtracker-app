package com.maxtracker.api.exception;

/** No hay series en el periodo pedido: no tiene sentido consultar a la IA. Se responde como 422. */
public class SinDatosParaAnalisisException extends RuntimeException {

    public SinDatosParaAnalisisException(int dias) {
        super("No hay series registradas en los últimos " + dias
                + " días para analizar. Registra algunos entrenamientos e intenta de nuevo");
    }
}
