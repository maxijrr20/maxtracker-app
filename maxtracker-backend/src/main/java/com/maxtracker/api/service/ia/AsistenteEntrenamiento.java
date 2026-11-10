package com.maxtracker.api.service.ia;

/**
 * Puerto hacia el "entrenador virtual" (un modelo de lenguaje). El caso de uso solo conoce esta interfaz;
 * la implementación concreta (Gemini, otro proveedor o un doble en los tests) vive en la capa de infraestructura.
 */
public interface AsistenteEntrenamiento {

    /**
     * Analiza el historial y devuelve recomendaciones.
     *
     * @param opciones enfoque y unidad del análisis, y la clave personal del usuario si configuró una
     * @throws com.maxtracker.api.exception.IaNoDisponibleException si el proveedor no está configurado,
     *                                                                la clave es inválida, falla, agota su cuota
     *                                                                o no responde
     */
    RecomendacionIa analizar(ContextoAnalisis contexto, OpcionesAnalisis opciones);
}
