package com.maxtracker.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxtracker.api.dto.metricas.EjercicioConHistorialDto;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse;
import com.maxtracker.api.dto.metricas.ResumenMetricasResponse;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.MetricasService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/** Métricas de progreso del usuario autenticado. */
@RestController
@RequestMapping("/api/v1/metricas")
@RequiredArgsConstructor
public class MetricasController {

    private final MetricasService metricasService;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenMetricasResponse> resumen(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(name = "semanas", defaultValue = "12")
            @Min(value = 4, message = "debe ser al menos 4")
            @Max(value = 52, message = "debe ser como máximo 52") int semanas) {
        return ResponseEntity.ok(metricasService.resumen(usuario.id(), semanas));
    }

    @GetMapping("/ejercicios")
    public ResponseEntity<List<EjercicioConHistorialDto>> ejercicios(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(metricasService.ejerciciosConHistorial(usuario.id()));
    }

    @GetMapping("/ejercicios/{ejercicioId}/progreso")
    public ResponseEntity<ProgresoEjercicioResponse> progreso(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("ejercicioId") @Positive(message = "debe ser un ID positivo") long ejercicioId) {
        return ResponseEntity.ok(metricasService.progreso(usuario.id(), ejercicioId));
    }
}
