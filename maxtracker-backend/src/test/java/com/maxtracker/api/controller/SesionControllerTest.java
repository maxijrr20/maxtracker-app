package com.maxtracker.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.dto.rutina.RutinaEjercicioResponse;
import com.maxtracker.api.dto.sesion.ActualizarSesionRequest;
import com.maxtracker.api.dto.sesion.IniciarSesionRequest;
import com.maxtracker.api.dto.sesion.RegistrarSerieRequest;
import com.maxtracker.api.dto.sesion.SerieDto;
import com.maxtracker.api.dto.sesion.SesionDto;
import com.maxtracker.api.dto.sesion.SesionResumenDto;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.exception.RecursoNoEncontradoException;
import com.maxtracker.api.exception.SesionFinalizadaException;
import com.maxtracker.api.exception.WgerApiException;
import com.maxtracker.api.security.ConSeguridadJwt;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.SesionService;

/** Contrato HTTP de /api/v1/sesiones con la seguridad real y el servicio simulado. */
@WebMvcTest(SesionController.class)
@ConSeguridadJwt
class SesionControllerTest {

    private static final long USUARIO_ID = 7L;
    private static final UsuarioAutenticado MAX = new UsuarioAutenticado(USUARIO_ID, "max@example.com", "$2a$10$x");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 10);
    private static final Instant INICIO = Instant.parse("2026-11-10T21:00:00Z");
    private static final SerieDto SERIE_BANCA =
            new SerieDto(100, 5, 192L, "Press de banca", 1, new BigDecimal("80.00"), 6, 2);
    private static final SesionDto SESION =
            new SesionDto(42, HOY, null, "Pecho", INICIO, null, false, List.of(), List.of(SERIE_BANCA));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SesionService sesionService;

    private ResultActions ejecutar(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.with(user(MAX)));
    }

    private static MockHttpServletRequestBuilder conJson(MockHttpServletRequestBuilder peticion, String json) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    // --- Autenticación ---

    @Test
    void todasLasRutasExigenAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/sesiones"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/no-autenticado"));
        mockMvc.perform(conJson(post("/api/v1/sesiones/42/series"), "{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sesionService);
    }

    // --- Iniciar ---

    @Test
    void iniciarResponde201ConLocationYUsaElIdDelUsuarioAutenticado() throws Exception {
        IniciarSesionRequest request = new IniciarSesionRequest(HOY, 3L, "Pecho");
        given(sesionService.iniciar(USUARIO_ID, request)).willReturn(SESION);

        ejecutar(conJson(post("/api/v1/sesiones"), """
                        {"fecha": "2026-11-10", "rutinaId": 3, "notas": "Pecho"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/v1/sesiones/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.fecha").value("2026-11-10"))
                .andExpect(jsonPath("$.iniciadaEn").value("2026-11-10T21:00:00Z"))
                .andExpect(jsonPath("$.finalizadaEn").isEmpty())
                .andExpect(jsonPath("$.finalizada").value(false))
                .andExpect(jsonPath("$.series[0].ejercicioNombre").value("Press de banca"));
    }

    @Test
    void iniciarSinCuerpoUsaValoresPorDefecto() throws Exception {
        given(sesionService.iniciar(USUARIO_ID, new IniciarSesionRequest(null, null, null))).willReturn(SESION);

        ejecutar(post("/api/v1/sesiones")).andExpect(status().isCreated());

        verify(sesionService).iniciar(USUARIO_ID, new IniciarSesionRequest(null, null, null));
    }

    @Test
    void iniciarRechazaNotasLargasYRutinaInvalida() throws Exception {
        String notas = "x".repeat(1001);

        ejecutar(conJson(post("/api/v1/sesiones"), """
                        {"rutinaId": 0, "notas": "%s"}
                        """.formatted(notas)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errores/validacion"))
                .andExpect(jsonPath("$.detail").value(
                        "notas: debe tener como máximo 1000 caracteres; rutinaId: debe ser un ID positivo"));

        verifyNoInteractions(sesionService);
    }

    @Test
    void iniciarRechazaFechaConFormatoInvalido() throws Exception {
        ejecutar(conJson(post("/api/v1/sesiones"), """
                        {"fecha": "10/11/2026"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(sesionService);
    }

    @Test
    void iniciarResponde404SiLaRutinaNoEsDelUsuario() throws Exception {
        given(sesionService.iniciar(eq(USUARIO_ID), any())).willThrow(RecursoNoEncontradoException.rutina(3));

        ejecutar(conJson(post("/api/v1/sesiones"), """
                        {"rutinaId": 3}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una rutina con id 3"));
    }

    @Test
    void iniciarConRutinaDevuelveSusEjerciciosParaPrecargarlos() throws Exception {
        RutinaEjercicioResponse banca = new RutinaEjercicioResponse(1, 5, 192L, "Press de banca", "Pecho", 4, 8);
        given(sesionService.iniciar(USUARIO_ID, new IniciarSesionRequest(null, 3L, null)))
                .willReturn(new SesionDto(43, HOY, 3L, null, INICIO, null, false, List.of(banca), List.of()));

        ejecutar(conJson(post("/api/v1/sesiones"), """
                        {"rutinaId": 3}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rutinaId").value(3))
                .andExpect(jsonPath("$.ejerciciosRutina[0].orden").value(1))
                .andExpect(jsonPath("$.ejerciciosRutina[0].ejercicioId").value(5))
                .andExpect(jsonPath("$.ejerciciosRutina[0].wgerId").value(192))
                .andExpect(jsonPath("$.ejerciciosRutina[0].ejercicioNombre").value("Press de banca"))
                .andExpect(jsonPath("$.ejerciciosRutina[0].seriesObjetivo").value(4))
                .andExpect(jsonPath("$.ejerciciosRutina[0].repeticionesObjetivo").value(8))
                .andExpect(jsonPath("$.series").isEmpty());
    }

    // --- Listar / obtener ---

    @Test
    void listarUsaPaginacionPorDefecto() throws Exception {
        SesionResumenDto resumen = new SesionResumenDto(42, HOY, null, "Pecho", INICIO, null, false);
        given(sesionService.listar(USUARIO_ID, 0, 20)).willReturn(new PaginaDto<>(List.of(resumen), 0, 20, 1, 1));

        ejecutar(get("/api/v1/sesiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(42))
                .andExpect(jsonPath("$.contenido[0].series").doesNotExist())
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void listarRechazaTamanioFueraDeRango() throws Exception {
        ejecutar(get("/api/v1/sesiones").param("tamanio", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("tamanio: debe ser al menos 1"));

        verifyNoInteractions(sesionService);
    }

    @Test
    void obtenerDevuelveLaSesionConSusSeries() throws Exception {
        given(sesionService.obtener(USUARIO_ID, 42)).willReturn(SESION);

        ejecutar(get("/api/v1/sesiones/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series[0].id").value(100))
                .andExpect(jsonPath("$.series[0].ejercicioId").value(5))
                .andExpect(jsonPath("$.series[0].wgerId").value(192))
                .andExpect(jsonPath("$.series[0].numeroSerie").value(1))
                .andExpect(jsonPath("$.series[0].pesoKg").value(80.00))
                .andExpect(jsonPath("$.series[0].repeticiones").value(6))
                .andExpect(jsonPath("$.series[0].rir").value(2));
    }

    @Test
    void obtenerResponde404SiNoExisteOEsDeOtroUsuario() throws Exception {
        given(sesionService.obtener(USUARIO_ID, 99)).willThrow(RecursoNoEncontradoException.sesion(99));

        ejecutar(get("/api/v1/sesiones/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/no-encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe una sesión con id 99"));
    }

    @Test
    void obtenerRechazaIdNoPositivo() throws Exception {
        ejecutar(get("/api/v1/sesiones/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("id: debe ser un ID positivo"));
    }

    // --- Actualizar / eliminar / finalizar ---

    @Test
    void actualizarExigeFecha() throws Exception {
        ejecutar(conJson(put("/api/v1/sesiones/42"), """
                        {"notas": "sin fecha"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("fecha: es obligatoria"));

        verifyNoInteractions(sesionService);
    }

    @Test
    void actualizarDevuelveLaSesionEditada() throws Exception {
        ActualizarSesionRequest request = new ActualizarSesionRequest(LocalDate.of(2026, 11, 9), "Corregida");
        given(sesionService.actualizar(USUARIO_ID, 42, request)).willReturn(SESION);

        ejecutar(conJson(put("/api/v1/sesiones/42"), """
                        {"fecha": "2026-11-09", "notas": "Corregida"}
                        """))
                .andExpect(status().isOk());

        verify(sesionService).actualizar(USUARIO_ID, 42, request);
    }

    @Test
    void eliminarResponde204() throws Exception {
        ejecutar(delete("/api/v1/sesiones/42")).andExpect(status().isNoContent());

        verify(sesionService).eliminar(USUARIO_ID, 42);
    }

    @Test
    void finalizarDevuelveLaSesionFinalizada() throws Exception {
        Instant fin = INICIO.plusSeconds(3600);
        given(sesionService.finalizar(USUARIO_ID, 42))
                .willReturn(new SesionDto(42, HOY, null, null, INICIO, fin, true, List.of(), List.of()));

        ejecutar(post("/api/v1/sesiones/42/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalizada").value(true))
                .andExpect(jsonPath("$.finalizadaEn").value("2026-11-10T22:00:00Z"));
    }

    @Test
    void finalizarDosVecesResponde409() throws Exception {
        given(sesionService.finalizar(USUARIO_ID, 42)).willThrow(SesionFinalizadaException.yaFinalizada());

        ejecutar(post("/api/v1/sesiones/42/finalizar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errores/sesion-finalizada"))
                .andExpect(jsonPath("$.detail").value("La sesión ya está finalizada"));
    }

    // --- Series ---

    @Test
    void registrarSeriePorWgerIdResponde201() throws Exception {
        RegistrarSerieRequest request = new RegistrarSerieRequest(null, 192L, new BigDecimal("80"), 6, 2);
        given(sesionService.registrarSerie(USUARIO_ID, 42, request)).willReturn(SERIE_BANCA);

        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 192, "pesoKg": 80, "repeticiones": 6, "rir": 2}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.numeroSerie").value(1))
                .andExpect(jsonPath("$.ejercicio").doesNotExist());
    }

    @Test
    void registrarSerieAceptaRirAusenteYEjercicioLocal() throws Exception {
        given(sesionService.registrarSerie(eq(USUARIO_ID), eq(42L), any())).willReturn(SERIE_BANCA);

        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"ejercicioId": 5, "pesoKg": 82.5, "repeticiones": 5}
                        """))
                .andExpect(status().isCreated());

        verify(sesionService).registrarSerie(USUARIO_ID, 42,
                new RegistrarSerieRequest(5L, null, new BigDecimal("82.5"), 5, null));
    }

    @Test
    void registrarSerieExigeExactamenteUnEjercicio() throws Exception {
        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"pesoKg": 80, "repeticiones": 6}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("ejercicio: indica exactamente uno de ejercicioId o wgerId"));

        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"ejercicioId": 5, "wgerId": 192, "pesoKg": 80, "repeticiones": 6}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("ejercicio: indica exactamente uno de ejercicioId o wgerId"));

        verifyNoInteractions(sesionService);
    }

    @Test
    void registrarSerieValidaPesoRepeticionesYRir() throws Exception {
        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 192, "pesoKg": -1, "repeticiones": 0, "rir": 11}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("pesoKg: debe ser mayor o igual a 0; "
                        + "repeticiones: debe ser al menos 1; rir: debe estar entre 0 y 10"));

        verifyNoInteractions(sesionService);
    }

    @Test
    void registrarSerieRechazaPesoConMasDeDosDecimalesOFueraDeRango() throws Exception {
        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 192, "pesoKg": 80.125, "repeticiones": 6}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("pesoKg: admite hasta 4 enteros y 2 decimales"));

        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 192, "pesoKg": 10000, "repeticiones": 6}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("pesoKg: admite hasta 4 enteros y 2 decimales"));
    }

    @Test
    void registrarSerieExigePesoYRepeticiones() throws Exception {
        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 192}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("pesoKg: es obligatorio; repeticiones: es obligatorio"));
    }

    @Test
    void registrarSerieEnSesionFinalizadaResponde409() throws Exception {
        given(sesionService.registrarSerie(eq(USUARIO_ID), eq(42L), any()))
                .willThrow(SesionFinalizadaException.noAdmiteCambiosEnSeries());

        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 192, "pesoKg": 80, "repeticiones": 6}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errores/sesion-finalizada"));
    }

    @Test
    void registrarSerieConEjercicioInexistenteResponde404() throws Exception {
        given(sesionService.registrarSerie(eq(USUARIO_ID), eq(42L), any()))
                .willThrow(new EjercicioNoEncontradoException(999));

        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 999, "pesoKg": 80, "repeticiones": 6}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un ejercicio con id 999"));
    }

    @Test
    void registrarSerieResponde502SiWgerNoEstaDisponible() throws Exception {
        given(sesionService.registrarSerie(eq(USUARIO_ID), eq(42L), any()))
                .willThrow(new WgerApiException("No se pudo obtener el ejercicio desde wger"));

        ejecutar(conJson(post("/api/v1/sesiones/42/series"), """
                        {"wgerId": 192, "pesoKg": 80, "repeticiones": 6}
                        """))
                .andExpect(status().isBadGateway());
    }

    @Test
    void eliminarSerieResponde204Y404SiNoExiste() throws Exception {
        ejecutar(delete("/api/v1/sesiones/42/series/100")).andExpect(status().isNoContent());
        verify(sesionService).eliminarSerie(USUARIO_ID, 42, 100);

        willThrow(RecursoNoEncontradoException.serie(101)).given(sesionService)
                .eliminarSerie(anyLong(), anyLong(), eq(101L));
        ejecutar(delete("/api/v1/sesiones/42/series/101"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una serie con id 101 en esta sesión"));
    }
}
