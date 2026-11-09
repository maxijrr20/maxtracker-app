package com.maxtracker.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.service.EjercicioService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/ejercicios")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @GetMapping
    public ResponseEntity<PaginaDto<EjercicioDto>> listar(
            @RequestParam(name = "pagina", defaultValue = "0")
            @Min(value = 0, message = "debe ser mayor o igual a 0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "20")
            @Min(value = 1, message = "debe ser al menos 1")
            @Max(value = 100, message = "debe ser como máximo 100") int tamanio,
            @RequestParam(name = "categoria", required = false)
            @Positive(message = "debe ser un ID positivo") Integer categoria) {
        return ResponseEntity.ok(ejercicioService.listar(pagina, tamanio, categoria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EjercicioDto> obtener(
            @PathVariable("id") @Positive(message = "debe ser un ID positivo") long id) {
        return ResponseEntity.ok(ejercicioService.obtenerPorId(id));
    }
}
