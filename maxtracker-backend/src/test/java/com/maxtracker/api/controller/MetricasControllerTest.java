package com.maxtracker.api.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.maxtracker.api.dto.metricas.EjercicioConHistorialDto;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse;
import com.maxtracker.api.dto.metricas.ProgresoEjercicioResponse.PuntoProgresoDto;
import com.maxtracker.api.dto.metricas.RecordPersonalDto;
import com.maxtracker.api.dto.metricas.ResumenMetricasResponse;
import com.maxtracker.api.dto.metricas.ResumenMetricasResponse.PeriodoDto;
import com.maxtracker.api.dto.metricas.VolumenSemanalDto;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.security.ConSeguridadJwt;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.MetricasService;

/** Contrato HTTP de /api/v1/metricas con la seguridad real y el servicio simulado. */
@WebMvcTest(MetricasController.class)
@ConSeguridadJwt
class MetricasControllerTest {

    private static final long USUARIO_ID = 7L;
    private static final UsuarioAutenticado MAX = new UsuarioAutenticado(USUARIO_ID, "max@example.com", "$2a$10$x");
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 9);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricasService metricasService;

    private ResultActions ejecutar(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.with(user(MAX)));
    }

    private static PeriodoDto periodo(String volumen, int sesiones, int series) {
        return new PeriodoDto(new BigDecimal(volumen), sesiones, series);
    }

    @Test
    void exigeAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/metricas/resumen")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/metricas/ejercicios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/metricas/ejercicios/5/progreso")).andExpect(status().isUnauthorized());

        verifyNoInteractions(metricasService);
    }

    @Test
    void resumenUsaDoceSemanasPorDefectoYSerializaPeriodosVolumenYRecords() throws Exception {
        given(metricasService.resumen(USUARIO_ID, 12)).willReturn(new ResumenMetricasResponse(
                periodo("1452.50", 2, 3), periodo("900.00", 1, 2), periodo("5000.00", 6, 30),
                periodo("4000.00", 5, 25),
                List.of(new VolumenSemanalDto(LUNES, new BigDecimal("1452.50"), 2)),
                List.of(new RecordPersonalDto(5, 192L, "Press de banca", "Pecho", new BigDecimal("85.00"), 4,
                        LocalDate.of(2026, 11, 3), new BigDecimal("96.25")))));

        ejecutar(get("/api/v1/metricas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semanaActual.volumenKg").value(1452.50))
                .andExpect(jsonPath("$.semanaActual.sesiones").value(2))
                .andExpect(jsonPath("$.semanaAnterior.volumenKg").value(900.00))
                .andExpect(jsonPath("$.mesActual.series").value(30))
                .andExpect(jsonPath("$.mesAnterior.sesiones").value(5))
                .andExpect(jsonPath("$.volumenSemanal[0].inicioSemana").value("2026-11-09"))
                .andExpect(jsonPath("$.records[0].ejercicioNombre").value("Press de banca"))
                .andExpect(jsonPath("$.records[0].pesoMaxKg").value(85.00))
                .andExpect(jsonPath("$.records[0].repeticiones").value(4))
                .andExpect(jsonPath("$.records[0].fecha").value("2026-11-03"))
                .andExpect(jsonPath("$.records[0].unoRmEstimadoKg").value(96.25));
    }

    @Test
    void resumenValidaElRangoDeSemanas() throws Exception {
        ejecutar(get("/api/v1/metricas/resumen").param("semanas", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errores/validacion"))
                .andExpect(jsonPath("$.detail").value("semanas: debe ser al menos 4"));
        ejecutar(get("/api/v1/metricas/resumen").param("semanas", "53"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("semanas: debe ser como máximo 52"));

        verifyNoInteractions(metricasService);
    }

    @Test
    void ejerciciosConHistorial() throws Exception {
        given(metricasService.ejerciciosConHistorial(USUARIO_ID)).willReturn(List.of(
                new EjercicioConHistorialDto(5, 192L, "Press de banca", "Pecho", 12, LocalDate.of(2026, 11, 10))));

        ejecutar(get("/api/v1/metricas/ejercicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ejercicioId").value(5))
                .andExpect(jsonPath("$[0].series").value(12))
                .andExpect(jsonPath("$[0].ultimaFecha").value("2026-11-10"));
    }

    @Test
    void progresoDeUnEjercicio() throws Exception {
        given(metricasService.progreso(USUARIO_ID, 5)).willReturn(new ProgresoEjercicioResponse(5, "Press de banca",
                List.of(new PuntoProgresoDto(42, LocalDate.of(2026, 11, 10), new BigDecimal("85.00"),
                        new BigDecimal("1200.00"), new BigDecimal("96.25"), 4))));

        ejecutar(get("/api/v1/metricas/ejercicios/5/progreso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ejercicioNombre").value("Press de banca"))
                .andExpect(jsonPath("$.puntos[0].fecha").value("2026-11-10"))
                .andExpect(jsonPath("$.puntos[0].pesoMaxKg").value(85.00))
                .andExpect(jsonPath("$.puntos[0].volumenKg").value(1200.00))
                .andExpect(jsonPath("$.puntos[0].series").value(4));
    }

    @Test
    void progresoResponde404SiElEjercicioNoEsAccesibleY400SiElIdNoEsPositivo() throws Exception {
        given(metricasService.progreso(USUARIO_ID, 99)).willThrow(new EjercicioNoEncontradoException(99));

        ejecutar(get("/api/v1/metricas/ejercicios/99/progreso"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errores/no-encontrado"));
        ejecutar(get("/api/v1/metricas/ejercicios/0/progreso"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("ejercicioId: debe ser un ID positivo"));
    }
}
