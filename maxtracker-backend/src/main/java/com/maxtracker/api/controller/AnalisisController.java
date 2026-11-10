package com.maxtracker.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxtracker.api.config.CabecerasApi;
import com.maxtracker.api.dto.analisis.AnalisisRutinaRequest;
import com.maxtracker.api.dto.analisis.AnalisisRutinaResponse;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.AnalisisService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
     *
     * @param claveGemini clave personal opcional (cabecera {@code X-Gemini-Api-Key}); se usa solo para esta
     *                    petición, sin guardarla
     */
    @PostMapping("/rutina")
    public ResponseEntity<AnalisisRutinaResponse> analizarRutina(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestHeader(name = CabecerasApi.CLAVE_GEMINI, required = false)
            @Size(max = 200, message = "es demasiado larga")
            @Pattern(regexp = "[A-Za-z0-9_\\-]*", message = "tiene caracteres no válidos") String claveGemini,
            @Valid @RequestBody(required = false) AnalisisRutinaRequest request) {
        return ResponseEntity.ok(analisisService.analizar(usuario.id(),
                request != null ? request : AnalisisRutinaRequest.general(), claveGemini));
    }
}
