package com.maxtracker.api.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.dto.sesion.ActualizarSesionRequest;
import com.maxtracker.api.dto.sesion.IniciarSesionRequest;
import com.maxtracker.api.dto.sesion.RegistrarSerieRequest;
import com.maxtracker.api.dto.sesion.SerieDto;
import com.maxtracker.api.dto.sesion.SesionDto;
import com.maxtracker.api.dto.sesion.SesionResumenDto;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.SesionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/** Sesiones de entrenamiento del usuario autenticado y sus series. */
@RestController
@RequestMapping("/api/v1/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final SesionService sesionService;

    @PostMapping
    public ResponseEntity<SesionDto> iniciar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody(required = false) IniciarSesionRequest request) {
        SesionDto sesion = sesionService.iniciar(usuario.id(),
                request != null ? request : new IniciarSesionRequest(null, null, null));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(sesion.id()).toUri();
        return ResponseEntity.created(ubicacion).body(sesion);
    }

    @GetMapping
    public ResponseEntity<PaginaDto<SesionResumenDto>> listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(name = "pagina", defaultValue = "0")
            @Min(value = 0, message = "debe ser mayor o igual a 0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "20")
            @Min(value = 1, message = "debe ser al menos 1")
            @Max(value = 100, message = "debe ser como máximo 100") int tamanio) {
        return ResponseEntity.ok(sesionService.listar(usuario.id(), pagina, tamanio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SesionDto> obtener(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id) {
        return ResponseEntity.ok(sesionService.obtener(usuario.id(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SesionDto> actualizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id,
            @Valid @RequestBody ActualizarSesionRequest request) {
        return ResponseEntity.ok(sesionService.actualizar(usuario.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id) {
        sesionService.eliminar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<SesionDto> finalizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id) {
        return ResponseEntity.ok(sesionService.finalizar(usuario.id(), id));
    }

    @PostMapping("/{id}/series")
    public ResponseEntity<SerieDto> registrarSerie(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id,
            @Valid @RequestBody RegistrarSerieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sesionService.registrarSerie(usuario.id(), id, request));
    }

    @DeleteMapping("/{id}/series/{serieId}")
    public ResponseEntity<Void> eliminarSerie(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id,
            @PathVariable("serieId") @Positive(message = "debe ser un ID positivo") long serieId) {
        sesionService.eliminarSerie(usuario.id(), id, serieId);
        return ResponseEntity.noContent().build();
    }
}
