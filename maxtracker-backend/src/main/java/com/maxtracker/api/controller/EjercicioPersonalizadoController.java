package com.maxtracker.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.maxtracker.api.dto.ejercicio.CrearEjercicioPersonalizadoRequest;
import com.maxtracker.api.dto.ejercicio.EjercicioPersonalizadoResponse;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioPersonalizadoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * Ejercicios propios del usuario autenticado. La ruta literal {@code /personalizados} tiene prioridad sobre
 * {@code GET /api/v1/ejercicios/{id}} (detalle del catálogo de wger) en el mapeo de Spring MVC.
 */
@RestController
@RequestMapping("/api/v1/ejercicios/personalizados")
@RequiredArgsConstructor
public class EjercicioPersonalizadoController {

    private final EjercicioPersonalizadoService service;

    @PostMapping
    public ResponseEntity<EjercicioPersonalizadoResponse> crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CrearEjercicioPersonalizadoRequest request) {
        EjercicioPersonalizadoResponse creado = service.crear(usuario.id(), request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.ejercicioId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<EjercicioPersonalizadoResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(service.listar(usuario.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id) {
        service.eliminar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
