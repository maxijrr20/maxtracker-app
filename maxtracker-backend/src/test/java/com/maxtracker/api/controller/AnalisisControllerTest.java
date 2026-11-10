package com.maxtracker.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.maxtracker.api.dto.analisis.AnalisisRutinaRequest;
import com.maxtracker.api.dto.analisis.AnalisisRutinaResponse;
import com.maxtracker.api.dto.analisis.AnalisisRutinaResponse.EjercicioAnalisisDto;
import com.maxtracker.api.exception.AnalisisDemasiadoFrecuenteException;
import com.maxtracker.api.exception.IaNoDisponibleException;
import com.maxtracker.api.exception.RecursoNoEncontradoException;
import com.maxtracker.api.exception.SinDatosParaAnalisisException;
import com.maxtracker.api.security.ConSeguridadJwt;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.AnalisisService;

/** Contrato HTTP de /api/v1/analisis con la seguridad real y el servicio simulado. */
@WebMvcTest(AnalisisController.class)
@ConSeguridadJwt
class AnalisisControllerTest {

    private static final long USUARIO_ID = 7L;
    private static final UsuarioAutenticado MAX = new UsuarioAutenticado(USUARIO_ID, "max@example.com", "$2a$10$x");
    private static final AnalisisRutinaResponse ANALISIS = new AnalisisRutinaResponse("Rutina Empuje",
            "Vas bien.", List.of(new EjercicioAnalisisDto("Press de banca", "PROGRESA", "Subiste 2,5 kg.",
                    "Prueba 85 kg.")),
            "Registra el RIR.", List.of("Mantén 4 series"), 28, 12, "gemini-3.5-flash-lite",
            Instant.parse("2026-11-10T22:00:00Z"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalisisService analisisService;

    private ResultActions ejecutar(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.with(user(MAX)));
    }

    private static MockHttpServletRequestBuilder conJson(String json) {
        return post("/api/v1/analisis/rutina").contentType(MediaType.APPLICATION_JSON).content(json);
    }

    @Test
    void exigeAutenticacion() throws Exception {
        mockMvc.perform(post("/api/v1/analisis/rutina")).andExpect(status().isUnauthorized());
        verifyNoInteractions(analisisService);
    }

    @Test
    void analizaUnaRutinaYDevuelveLasRecomendaciones() throws Exception {
        given(analisisService.analizar(USUARIO_ID, new AnalisisRutinaRequest(3L, null, 14, null, null), null)).willReturn(ANALISIS);

        ejecutar(conJson("{\"rutinaId\": 3, \"dias\": 14}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alcance").value("Rutina Empuje"))
                .andExpect(jsonPath("$.resumen").value("Vas bien."))
                .andExpect(jsonPath("$.ejercicios[0].tendencia").value("PROGRESA"))
                .andExpect(jsonPath("$.ejercicios[0].sugerencia").value("Prueba 85 kg."))
                .andExpect(jsonPath("$.ajusteRir").value("Registra el RIR."))
                .andExpect(jsonPath("$.sugerencias[0]").value("Mantén 4 series"))
                .andExpect(jsonPath("$.seriesAnalizadas").value(12))
                .andExpect(jsonPath("$.modelo").value("gemini-3.5-flash-lite"))
                .andExpect(jsonPath("$.generadoEn").value("2026-11-10T22:00:00Z"));
    }

    @Test
    void sinCuerpoAnalizaTodoElPeriodoPorDefecto() throws Exception {
        given(analisisService.analizar(USUARIO_ID, AnalisisRutinaRequest.general(), null)).willReturn(ANALISIS);

        ejecutar(post("/api/v1/analisis/rutina")).andExpect(status().isOk());

        verify(analisisService).analizar(USUARIO_ID, AnalisisRutinaRequest.general(), null);
    }

    @Test
    void validaElCuerpo() throws Exception {
        ejecutar(conJson("{\"rutinaId\": 3, \"sesionId\": 4, \"dias\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(
                        "alcance: indica como máximo uno de rutinaId o sesionId; dias: debe estar entre 7 y 90"));
        ejecutar(conJson("{\"sesionId\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("sesionId: debe ser un ID positivo"));

        verifyNoInteractions(analisisService);
    }

    @Test
    void pasaLaClavePersonalElEnfoqueYLaUnidad() throws Exception {
        AnalisisRutinaRequest esperado = new AnalisisRutinaRequest(null, 9L, null,
                com.maxtracker.api.service.ia.OpcionesAnalisis.Enfoque.FUERZA,
                com.maxtracker.api.service.ia.OpcionesAnalisis.UnidadPeso.LB);
        given(analisisService.analizar(USUARIO_ID, esperado, "AIza-clave_personal")).willReturn(ANALISIS);

        ejecutar(conJson("{\"sesionId\": 9, \"enfoque\": \"FUERZA\", \"unidadPeso\": \"LB\"}")
                .header("X-Gemini-Api-Key", "AIza-clave_personal"))
                .andExpect(status().isOk());

        verify(analisisService).analizar(USUARIO_ID, esperado, "AIza-clave_personal");
    }

    @Test
    void rechazaUnaClaveConCaracteresInvalidosSinRepetirla() throws Exception {
        ejecutar(conJson("{}").header("X-Gemini-Api-Key", "clave con espacios;"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("claveGemini: tiene caracteres no válidos"));

        verifyNoInteractions(analisisService);
    }

    @Test
    void rechazaUnEnfoqueDesconocido() throws Exception {
        ejecutar(conJson("{\"enfoque\": \"CARDIO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(analisisService);
    }

    @Test
    void erroresDelServicioComoProblemDetail() throws Exception {
        given(analisisService.analizar(eq(USUARIO_ID), any(), any()))
                .willThrow(RecursoNoEncontradoException.rutina(3))
                .willThrow(new SinDatosParaAnalisisException(28))
                .willThrow(new AnalisisDemasiadoFrecuenteException(Duration.ofSeconds(12)))
                .willThrow(IaNoDisponibleException.noConfigurada());

        ejecutar(conJson("{\"rutinaId\": 3}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errores/no-encontrado"));
        ejecutar(conJson("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("/errores/sin-datos-para-analisis"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.startsWith(
                        "No hay series registradas en los últimos 28 días")));
        ejecutar(conJson("{}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "12"))
                .andExpect(jsonPath("$.type").value("/errores/demasiadas-solicitudes"));
        ejecutar(conJson("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.type").value("/errores/ia-no-disponible"))
                .andExpect(jsonPath("$.title").value("Análisis con IA no disponible"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
