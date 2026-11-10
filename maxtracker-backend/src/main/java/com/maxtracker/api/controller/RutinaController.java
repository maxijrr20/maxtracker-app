package com.maxtracker.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.maxtracker.api.dto.rutina.ActualizarRutinaRequest;
import com.maxtracker.api.dto.rutina.CrearRutinaRequest;
import com.maxtracker.api.dto.rutina.RutinaResponse;
import com.maxtracker.api.dto.rutina.RutinaResumenResponse;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.RutinaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/** Rutinas (plantillas de entrenamiento) del usuario autenticado. */
@RestController
@RequestMapping("/api/v1/rutinas")
@RequiredArgsConstructor
public class RutinaController {

    private final RutinaService rutinaService;

    @PostMapping
    public ResponseEntity<RutinaResponse> crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CrearRutinaRequest request) {
        RutinaResponse rutina = rutinaService.crear(usuario.id(), request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(rutina.id()).toUri();
        return ResponseEntity.created(ubicacion).body(rutina);
    }

    @GetMapping
    public ResponseEntity<List<RutinaResumenResponse>> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(rutinaService.listar(usuario.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutinaResponse> obtener(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id) {
        return ResponseEntity.ok(rutinaService.obtener(usuario.id(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutinaResponse> actualizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id,
            @Valid @RequestBody ActualizarRutinaRequest request) {
        return ResponseEntity.ok(rutinaService.actualizar(usuario.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id) {
        rutinaService.eliminar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
