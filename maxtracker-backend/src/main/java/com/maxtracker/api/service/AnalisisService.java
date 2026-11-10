package com.maxtracker.api.service;

import com.maxtracker.api.dto.analisis.AnalisisRutinaRequest;
import com.maxtracker.api.dto.analisis.AnalisisRutinaResponse;

/** Análisis del entrenamiento con IA (sobrecarga progresiva, RIR y sugerencias). */
public interface AnalisisService {

    /**
     * @param claveApiUsuario clave personal de Gemini del usuario (cabecera {@code X-Gemini-Api-Key}), o
     *                        {@code null} para usar la del servidor
     * @throws com.maxtracker.api.exception.RecursoNoEncontradoException       rutina o sesión inexistente o ajena (404)
     * @throws com.maxtracker.api.exception.SinDatosParaAnalisisException      no hay series en el periodo (422)
     * @throws com.maxtracker.api.exception.AnalisisDemasiadoFrecuenteException análisis pedido hace muy poco (429)
     * @throws com.maxtracker.api.exception.IaNoDisponibleException            el proveedor de IA falló (503)
     */
    AnalisisRutinaResponse analizar(long usuarioId, AnalisisRutinaRequest request, String claveApiUsuario);
}
