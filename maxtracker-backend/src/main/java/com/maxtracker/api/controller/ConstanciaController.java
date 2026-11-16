package com.maxtracker.api.controller;

import java.time.YearMonth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxtracker.api.dto.constancia.ConstanciaResponse;
import com.maxtracker.api.dto.constancia.PlanEntrenamientoRequest;
import com.maxtracker.api.dto.constancia.PlanEntrenamientoResponse;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.ConstanciaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

/** Calendario de constancia y plan semanal del usuario autenticado. */
@RestController
@RequestMapping("/api/v1/constancia")
@RequiredArgsConstructor
public class ConstanciaController {

    private final ConstanciaService constanciaService;

    /** Calendario de un mes ("AAAA-MM"; por defecto el actual) y estadísticas. */
    @GetMapping
    public ResponseEntity<ConstanciaResponse> consultar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(name = "mes", required = false)
            @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "debe tener el formato AAAA-MM") String mes) {
        YearMonth consultado = mes == null ? null : YearMonth.parse(mes);
        return ResponseEntity.ok(constanciaService.consultar(usuario.id(), consultado));
    }

    /** Plan vigente; 204 si el usuario aún no configuró uno. */
    @GetMapping("/plan")
    public ResponseEntity<PlanEntrenamientoResponse> plan(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return constanciaService.planVigente(usuario.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** Crea o cambia el plan semanal; rige desde hoy. */
    @PutMapping("/plan")
    public ResponseEntity<PlanEntrenamientoResponse> guardarPlan(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody PlanEntrenamientoRequest request) {
        return ResponseEntity.ok(constanciaService.guardarPlan(usuario.id(), request));
    }
}
