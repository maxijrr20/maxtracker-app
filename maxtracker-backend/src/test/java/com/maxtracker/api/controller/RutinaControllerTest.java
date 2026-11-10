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

import com.maxtracker.api.dto.rutina.ActualizarRutinaRequest;
import com.maxtracker.api.dto.rutina.CrearRutinaRequest;
import com.maxtracker.api.dto.rutina.RutinaEjercicioRequest;
import com.maxtracker.api.dto.rutina.RutinaEjercicioResponse;
import com.maxtracker.api.dto.rutina.RutinaResponse;
import com.maxtracker.api.dto.rutina.RutinaResumenResponse;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.exception.RecursoNoEncontradoException;
import com.maxtracker.api.exception.WgerApiException;
import com.maxtracker.api.security.ConSeguridadJwt;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.RutinaService;

/** Contrato HTTP de /api/v1/rutinas con la seguridad real y el servicio simulado. */
@WebMvcTest(RutinaController.class)
@ConSeguridadJwt
class RutinaControllerTest {

    private static final long USUARIO_ID = 7L;
    private static final UsuarioAutenticado MAX = new UsuarioAutenticado(USUARIO_ID, "max@example.com", "$2a$10$x");
    private static final Instant CREADA = Instant.parse("2026-11-10T21:00:00Z");
    private static final RutinaEjercicioResponse BANCA =
            new RutinaEjercicioResponse(1, 5, 192L, "Press de banca", "Pecho", 4, 8);
    private static final RutinaEjercicioResponse CURL =
            new RutinaEjercicioResponse(2, 9, null, "Curl martillo", "Brazos", null, null);
    private static final RutinaResponse RUTINA =
            new RutinaResponse(3, "Empuje", "Pecho y tríceps", CREADA, List.of(BANCA, CURL));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RutinaService rutinaService;

    private ResultActions ejecutar(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.with(user(MAX)));
    }

    private static MockHttpServletRequestBuilder conJson(MockHttpServletRequestBuilder peticion, String json) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    // --- Autenticación ---

    @Test
    void todasLasRutasExigenAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/rutinas"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/no-autenticado"));
        mockMvc.perform(conJson(post("/api/v1/rutinas"), "{\"nombre\": \"Empuje\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/rutinas/3")).andExpect(status().isUnauthorized());
        mockMvc.perform(conJson(put("/api/v1/rutinas/3"), "{\"nombre\": \"Empuje\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/rutinas/3")).andExpect(status().isUnauthorized());

        verifyNoInteractions(rutinaService);
    }

    // --- Crear ---

    @Test
    void crearResponde201ConLocationYUsaElIdDelUsuarioAutenticado() throws Exception {
        CrearRutinaRequest request = new CrearRutinaRequest("Empuje", "Pecho y tríceps", List.of(
                new RutinaEjercicioRequest(null, 192L, 4, 8),
                new RutinaEjercicioRequest(9L, null, null, null)));
        given(rutinaService.crear(USUARIO_ID, request)).willReturn(RUTINA);

        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "Empuje", "descripcion": "Pecho y tríceps", "ejercicios": [
                          {"wgerId": 192, "seriesObjetivo": 4, "repeticionesObjetivo": 8},
                          {"ejercicioId": 9}
                        ]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/v1/rutinas/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("Empuje"))
                .andExpect(jsonPath("$.descripcion").value("Pecho y tríceps"))
                .andExpect(jsonPath("$.creadoEn").value("2026-11-10T21:00:00Z"))
                .andExpect(jsonPath("$.ejercicios[0].orden").value(1))
                .andExpect(jsonPath("$.ejercicios[0].ejercicioId").value(5))
                .andExpect(jsonPath("$.ejercicios[0].wgerId").value(192))
                .andExpect(jsonPath("$.ejercicios[0].ejercicioNombre").value("Press de banca"))
                .andExpect(jsonPath("$.ejercicios[0].categoria").value("Pecho"))
                .andExpect(jsonPath("$.ejercicios[0].seriesObjetivo").value(4))
                .andExpect(jsonPath("$.ejercicios[0].repeticionesObjetivo").value(8))
                .andExpect(jsonPath("$.ejercicios[1].wgerId").isEmpty())
                .andExpect(jsonPath("$.ejercicios[1].seriesObjetivo").isEmpty());
    }

    @Test
    void crearSinEjerciciosLosTomaComoListaVacia() throws Exception {
        given(rutinaService.crear(eq(USUARIO_ID), any()))
                .willReturn(new RutinaResponse(4, "Vacía", null, CREADA, List.of()));

        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "Vacía"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ejercicios").isEmpty());

        verify(rutinaService).crear(USUARIO_ID, new CrearRutinaRequest("Vacía", null, List.of()));
    }

    @Test
    void crearExigeNombre() throws Exception {
        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "   "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/validacion"))
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("nombre: es obligatorio"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verifyNoInteractions(rutinaService);
    }

    @Test
    void crearRechazaNombreYDescripcionLargos() throws Exception {
        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "%s", "descripcion": "%s"}
                        """.formatted("x".repeat(101), "x".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("descripcion: debe tener como máximo 500 caracteres; "
                        + "nombre: debe tener como máximo 100 caracteres"));

        verifyNoInteractions(rutinaService);
    }

    @Test
    void crearExigeExactamenteUnEjercicioPorItem() throws Exception {
        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "Empuje", "ejercicios": [{"seriesObjetivo": 3}, {"ejercicioId": 5, "wgerId": 192}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "ejercicios[0].ejercicio: indica exactamente uno de ejercicioId o wgerId; "
                                + "ejercicios[1].ejercicio: indica exactamente uno de ejercicioId o wgerId"));

        verifyNoInteractions(rutinaService);
    }

    @Test
    void crearValidaLosObjetivosYLosIdsDeCadaEjercicio() throws Exception {
        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "Empuje", "ejercicios": [
                          {"wgerId": 0, "seriesObjetivo": 0, "repeticionesObjetivo": 1000}
                        ]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("ejercicios[0].repeticionesObjetivo: debe estar entre 1 y 999; "
                        + "ejercicios[0].seriesObjetivo: debe estar entre 1 y 20; "
                        + "ejercicios[0].wgerId: debe ser un ID positivo"));

        verifyNoInteractions(rutinaService);
    }

    @Test
    void crearLimitaLaCantidadDeEjercicios() throws Exception {
        String items = String.join(",", java.util.Collections.nCopies(31, "{\"wgerId\": 192}"));

        ejecutar(conJson(post("/api/v1/rutinas"), "{\"nombre\": \"Larga\", \"ejercicios\": [%s]}".formatted(items)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("ejercicios: admite como máximo 30 ejercicios"));

        verifyNoInteractions(rutinaService);
    }

    @Test
    void crearRechazaJsonMalFormado() throws Exception {
        ejecutar(conJson(post("/api/v1/rutinas"), "{\"nombre\": "))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(rutinaService);
    }

    @Test
    void crearResponde404SiUnEjercicioNoExisteOEsDeOtroUsuario() throws Exception {
        given(rutinaService.crear(eq(USUARIO_ID), any())).willThrow(new EjercicioNoEncontradoException(9));

        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "Empuje", "ejercicios": [{"ejercicioId": 9}]}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errores/no-encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe un ejercicio con id 9"));
    }

    @Test
    void crearResponde502SiWgerNoEstaDisponible() throws Exception {
        given(rutinaService.crear(eq(USUARIO_ID), any()))
                .willThrow(new WgerApiException("No se pudo obtener el ejercicio desde wger"));

        ejecutar(conJson(post("/api/v1/rutinas"), """
                        {"nombre": "Empuje", "ejercicios": [{"wgerId": 192}]}
                        """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.type").value("/errores/wger-no-disponible"));
    }

    // --- Listar / obtener ---

    @Test
    void listarDevuelveLasRutinasDelUsuarioSinSusEjercicios() throws Exception {
        given(rutinaService.listar(USUARIO_ID)).willReturn(List.of(
                new RutinaResumenResponse(3, "Empuje", "Pecho y tríceps", CREADA, 2),
                new RutinaResumenResponse(4, "Tirón", null, CREADA, 0)));

        ejecutar(get("/api/v1/rutinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].cantidadEjercicios").value(2))
                .andExpect(jsonPath("$[0].ejercicios").doesNotExist())
                .andExpect(jsonPath("$[1].nombre").value("Tirón"));
    }

    @Test
    void obtenerDevuelveLaRutinaConSusEjercicios() throws Exception {
        given(rutinaService.obtener(USUARIO_ID, 3)).willReturn(RUTINA);

        ejecutar(get("/api/v1/rutinas/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ejercicios.length()").value(2))
                .andExpect(jsonPath("$.ejercicios[1].ejercicioNombre").value("Curl martillo"));
    }

    @Test
    void obtenerResponde404SiNoExisteOEsDeOtroUsuario() throws Exception {
        given(rutinaService.obtener(USUARIO_ID, 99)).willThrow(RecursoNoEncontradoException.rutina(99));

        ejecutar(get("/api/v1/rutinas/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/no-encontrado"))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe una rutina con id 99"));
    }

    @Test
    void lasRutasConIdRechazanIdNoPositivo() throws Exception {
        ejecutar(get("/api/v1/rutinas/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parámetros inválidos"))
                .andExpect(jsonPath("$.detail").value("id: debe ser un ID positivo"));
        ejecutar(conJson(put("/api/v1/rutinas/-1"), "{\"nombre\": \"Empuje\"}"))
                .andExpect(status().isBadRequest());
        ejecutar(delete("/api/v1/rutinas/0")).andExpect(status().isBadRequest());

        verifyNoInteractions(rutinaService);
    }

    @Test
    void obtenerRechazaIdNoNumerico() throws Exception {
        ejecutar(get("/api/v1/rutinas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(rutinaService);
    }

    // --- Actualizar / eliminar ---

    @Test
    void actualizarDevuelveLaRutinaEditada() throws Exception {
        ActualizarRutinaRequest request = new ActualizarRutinaRequest("Empuje B", null,
                List.of(new RutinaEjercicioRequest(5L, null, 5, 5)));
        RutinaResponse editada = new RutinaResponse(3, "Empuje B", null, CREADA,
                List.of(new RutinaEjercicioResponse(1, 5, 192L, "Press de banca", "Pecho", 5, 5)));
        given(rutinaService.actualizar(USUARIO_ID, 3, request)).willReturn(editada);

        ejecutar(conJson(put("/api/v1/rutinas/3"), """
                        {"nombre": "Empuje B", "ejercicios": [
                          {"ejercicioId": 5, "seriesObjetivo": 5, "repeticionesObjetivo": 5}
                        ]}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empuje B"))
                .andExpect(jsonPath("$.descripcion").isEmpty())
                .andExpect(jsonPath("$.ejercicios[0].seriesObjetivo").value(5));

        verify(rutinaService).actualizar(USUARIO_ID, 3, request);
    }

    @Test
    void actualizarValidaElCuerpo() throws Exception {
        ejecutar(conJson(put("/api/v1/rutinas/3"), """
                        {"descripcion": "sin nombre", "ejercicios": [{}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errores/validacion"))
                .andExpect(jsonPath("$.detail").value(
                        "ejercicios[0].ejercicio: indica exactamente uno de ejercicioId o wgerId; nombre: es obligatorio"));

        verifyNoInteractions(rutinaService);
    }

    @Test
    void actualizarResponde404SiNoExisteOEsDeOtroUsuario() throws Exception {
        given(rutinaService.actualizar(eq(USUARIO_ID), eq(99L), any()))
                .willThrow(RecursoNoEncontradoException.rutina(99));

        ejecutar(conJson(put("/api/v1/rutinas/99"), "{\"nombre\": \"Empuje\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una rutina con id 99"));
    }

    @Test
    void eliminarResponde204() throws Exception {
        ejecutar(delete("/api/v1/rutinas/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(rutinaService).eliminar(USUARIO_ID, 3);
    }

    @Test
    void eliminarResponde404SiNoExisteOEsDeOtroUsuario() throws Exception {
        willThrow(RecursoNoEncontradoException.rutina(99)).given(rutinaService).eliminar(anyLong(), eq(99L));

        ejecutar(delete("/api/v1/rutinas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errores/no-encontrado"));
    }
}
