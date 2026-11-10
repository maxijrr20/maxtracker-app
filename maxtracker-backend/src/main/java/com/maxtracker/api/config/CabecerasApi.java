package com.maxtracker.api.config;

/** Cabeceras HTTP propias de la API (compartidas por controladores y CORS). */
public final class CabecerasApi {

    /**
     * Clave personal de Gemini del usuario, opcional, solo en {@code POST /api/v1/analisis/rutina}. Si viene, se usa
     * en lugar de la clave del servidor. No se guarda ni se registra en logs.
     */
    public static final String CLAVE_GEMINI = "X-Gemini-Api-Key";

    private CabecerasApi() {
    }
}
