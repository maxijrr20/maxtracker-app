package com.maxtracker.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxtracker.api.dto.analisis.AnalisisRutinaRequest;
import com.maxtracker.api.dto.analisis.AnalisisRutinaResponse;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.AnalisisService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Entrenador virtual: análisis del entrenamiento con IA. */
@RestController
@RequestMapping("/api/v1/analisis")
@RequiredArgsConstructor
public class AnalisisController {

    private final AnalisisService analisisService;

    /**
     * Analiza el historial reciente (sobrecarga progresiva, RIR y sugerencias). Es un POST porque genera un
     * análisis nuevo cada vez (no es idempotente ni cacheable). El cuerpo es opcional: sin él, analiza todo
     * lo entrenado en los últimos 28 días.
     */
    @PostMapping("/rutina")
    public ResponseEntity<AnalisisRutinaResponse> analizarRutina(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody(required = false) AnalisisRutinaRequest request) {
        return ResponseEntity.ok(analisisService.analizar(usuario.id(),
                request != null ? request : AnalisisRutinaRequest.general()));
    }
}
