package com.maxtracker.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.exception.WgerApiException;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.repository.RutinaRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioService;

/**
 * /api/v1/rutinas (y su uso desde /api/v1/sesiones) con la aplicación real sobre H2 (perfil "test"):
 * JWT real, JPA, Flyway y aislamiento entre usuarios. Solo se simula el catálogo de wger.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RutinaIntegrationTest {

    private static final EjercicioDto BANCA_WGER = new EjercicioDto(192, "Press de banca", null, "es", 11, "Pecho",
            List.of(), List.of(), List.of(), null);
    private static final EjercicioDto REMO_WGER = new EjercicioDto(106, "Remo con barra", null, "es", 12, "Espalda",
            List.of(), List.of(), List.of(), null);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private EjercicioService catalogo;

    private Usuario max;
    private Usuario otro;
    private String tokenMax;
    private String tokenOtro;

    /** La base H2 en memoria se comparte con los demás tests de la JVM: no dejar datos al terminar. */
    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM serie_registro");
        jdbc.update("DELETE FROM sesion_entrenamiento");
        jdbc.update("DELETE FROM rutina_ejercicio");
        jdbc.update("DELETE FROM rutina");
        jdbc.update("DELETE FROM ejercicio");
        jdbc.update("DELETE FROM usuario");
    }

    @BeforeEach
    void preparar() {
        limpiar();
        max = usuarioRepository.save(new Usuario("max@example.com", "$2a$10$noSeUsaEnEsteTest"));
        otro = usuarioRepository.save(new Usuario("otro@example.com", "$2a$10$noSeUsaEnEsteTest"));
        tokenMax = token(max);
        tokenOtro = token(otro);

        given(catalogo.obtenerPorId(192)).willReturn(BANCA_WGER);
        given(catalogo.obtenerPorId(106)).willReturn(REMO_WGER);
    }

    private String token(Usuario usuario) {
        return jwtService.generarToken(UsuarioAutenticado.desde(usuario)).valor();
    }

    private ResultActions comoMax(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMax));
    }

    private ResultActions comoOtro(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOtro));
    }

    private static MockHttpServletRequestBuilder conJson(MockHttpServletRequestBuilder peticion, String json) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private JsonNode json(ResultActions resultado) throws Exception {
        return objectMapper.readTree(resultado.andReturn().getResponse().getContentAsString());
    }

    private long crearRutina(String json) throws Exception {
        return json(comoMax(conJson(post("/api/v1/rutinas"), json)).andExpect(status().isCreated()))
                .get("id").asLong();
    }

    private long curlPropio() {
        return ejercicioRepository.save(Ejercicio.personalizado(max, "Curl martillo", "Brazos")).getId();
    }

    @Test
    void flujoCompletoCrearVerListarActualizarYEliminar() throws Exception {
        long curl = curlPropio();

        ResultActions creada = comoMax(conJson(post("/api/v1/rutinas"), """
                {"nombre": "  Empuje  ", "descripcion": "Pecho y tríceps", "ejercicios": [
                  {"wgerId": 192, "seriesObjetivo": 4, "repeticionesObjetivo": 8},
                  {"ejercicioId": %d, "seriesObjetivo": 3}
                ]}
                """.formatted(curl)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Empuje"))
                .andExpect(jsonPath("$.creadoEn").isNotEmpty())
                .andExpect(jsonPath("$.ejercicios[*].orden").value(contains(1, 2)))
                .andExpect(jsonPath("$.ejercicios[*].ejercicioNombre").value(contains("Press de banca", "Curl martillo")))
                .andExpect(jsonPath("$.ejercicios[0].wgerId").value(192))
                .andExpect(jsonPath("$.ejercicios[0].categoria").value("Pecho"))
                .andExpect(jsonPath("$.ejercicios[1].wgerId").isEmpty())
                .andExpect(jsonPath("$.ejercicios[1].repeticionesObjetivo").isEmpty());
        long id = json(creada).get("id").asLong();
        creada.andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/v1/rutinas/" + id));

        comoMax(get("/api/v1/rutinas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Pecho y tríceps"))
                .andExpect(jsonPath("$.ejercicios.length()").value(2));

        comoMax(get("/api/v1/rutinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].cantidadEjercicios").value(2));

        // PUT reemplaza todo: nuevo orden, ejercicio nuevo, uno quitado y descripción borrada.
        comoMax(conJson(put("/api/v1/rutinas/{id}", id), """
                {"nombre": "Empuje B", "descripcion": "  ", "ejercicios": [
                  {"wgerId": 106, "seriesObjetivo": 5, "repeticionesObjetivo": 5},
                  {"wgerId": 192}
                ]}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empuje B"))
                .andExpect(jsonPath("$.descripcion").isEmpty())
                .andExpect(jsonPath("$.ejercicios[*].ejercicioNombre").value(contains("Remo con barra", "Press de banca")))
                .andExpect(jsonPath("$.ejercicios[*].orden").value(contains(1, 2)))
                .andExpect(jsonPath("$.ejercicios[1].seriesObjetivo").isEmpty());
        comoMax(get("/api/v1/rutinas/{id}", id))
                .andExpect(jsonPath("$.ejercicios[*].wgerId").value(contains(106, 192)));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rutina_ejercicio", Long.class)).isEqualTo(2);

        comoMax(delete("/api/v1/rutinas/{id}", id)).andExpect(status().isNoContent());

        comoMax(get("/api/v1/rutinas/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errores/no-encontrado"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rutina_ejercicio", Long.class)).isZero();
        // Los ejercicios se conservan: las copias de wger son compartidas y el personalizado es del usuario.
        assertThat(ejercicioRepository.findById(curl)).isPresent();
        assertThat(ejercicioRepository.findByWgerId(192L)).isPresent();
    }

    @Test
    void actualizarConListaVaciaQuitaTodosLosEjercicios() throws Exception {
        long id = crearRutina("{\"nombre\": \"Pierna\", \"ejercicios\": [{\"wgerId\": 192}, {\"wgerId\": 106}]}");

        comoMax(conJson(put("/api/v1/rutinas/{id}", id), "{\"nombre\": \"Pierna\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ejercicios").isEmpty());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rutina_ejercicio", Long.class)).isZero();
    }

    @Test
    void listarOrdenaPorNombreYSoloIncluyeLasDelUsuario() throws Exception {
        crearRutina("{\"nombre\": \"Tirón\", \"ejercicios\": [{\"wgerId\": 106}]}");
        crearRutina("{\"nombre\": \"Empuje\", \"ejercicios\": [{\"wgerId\": 192}, {\"wgerId\": 192}]}");
        crearRutina("{\"nombre\": \"Pierna\"}");

        comoMax(get("/api/v1/rutinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre").value(contains("Empuje", "Pierna", "Tirón")))
                .andExpect(jsonPath("$[*].cantidadEjercicios").value(contains(2, 0, 1)));
        comoOtro(get("/api/v1/rutinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void laCopiaLocalDeWgerSeCreaUnaSolaVezYSeComparteConLasSeries() throws Exception {
        crearRutina("{\"nombre\": \"A\", \"ejercicios\": [{\"wgerId\": 192}, {\"wgerId\": 192}]}");
        long otraRutina = crearRutina("{\"nombre\": \"B\", \"ejercicios\": [{\"wgerId\": 192}]}");
        long sesionId = json(comoMax(conJson(post("/api/v1/sesiones"), "{\"rutinaId\": %d}".formatted(otraRutina)))
                .andExpect(status().isCreated())).get("id").asLong();
        comoMax(conJson(post("/api/v1/sesiones/{id}/series", sesionId),
                "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}"))
                .andExpect(status().isCreated());

        verify(catalogo, times(1)).obtenerPorId(192);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ejercicio WHERE wger_id = 192", Long.class))
                .isEqualTo(1);
    }

    @Test
    void unUsuarioNoPuedeVerNiModificarRutinasDeOtro() throws Exception {
        long id = crearRutina("{\"nombre\": \"Empuje\", \"ejercicios\": [{\"wgerId\": 192}]}");

        comoOtro(get("/api/v1/rutinas/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una rutina con id " + id));
        comoOtro(conJson(put("/api/v1/rutinas/{id}", id), "{\"nombre\": \"Robada\"}"))
                .andExpect(status().isNotFound());
        comoOtro(delete("/api/v1/rutinas/{id}", id)).andExpect(status().isNotFound());
        // Tampoco puede basar una sesión en ella.
        comoOtro(conJson(post("/api/v1/sesiones"), "{\"rutinaId\": %d}".formatted(id)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una rutina con id " + id));

        comoMax(get("/api/v1/rutinas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empuje"))
                .andExpect(jsonPath("$.ejercicios.length()").value(1));
    }

    @Test
    void noSePuedeUsarElEjercicioPersonalizadoDeOtroUsuario() throws Exception {
        long ajeno = ejercicioRepository.save(Ejercicio.personalizado(otro, "Curl secreto", null)).getId();

        comoMax(conJson(post("/api/v1/rutinas"), """
                {"nombre": "Brazos", "ejercicios": [{"wgerId": 192}, {"ejercicioId": %d}]}
                """.formatted(ajeno)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un ejercicio con id " + ajeno));

        assertThat(rutinaRepository.count()).isZero();
    }

    @Test
    void siWgerNoEstaDisponibleNoSeGuardaLaRutina() throws Exception {
        given(catalogo.obtenerPorId(anyLong())).willThrow(new WgerApiException("No se pudo obtener el ejercicio"));

        comoMax(conJson(post("/api/v1/rutinas"), "{\"nombre\": \"Empuje\", \"ejercicios\": [{\"wgerId\": 300}]}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.type").value("/errores/wger-no-disponible"));

        assertThat(rutinaRepository.count()).isZero();
        assertThat(ejercicioRepository.findByWgerId(300L)).isEmpty();
    }

    @Test
    void unaActualizacionFallidaDejaLaRutinaComoEstaba() throws Exception {
        long id = crearRutina("""
                {"nombre": "Empuje", "descripcion": "Original", "ejercicios": [{"wgerId": 192, "seriesObjetivo": 4}]}
                """);

        comoMax(conJson(put("/api/v1/rutinas/{id}", id), """
                {"nombre": "Cambiada", "ejercicios": [{"wgerId": 106}, {"ejercicioId": 999999}]}
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un ejercicio con id 999999"));

        comoMax(get("/api/v1/rutinas/{id}", id))
                .andExpect(jsonPath("$.nombre").value("Empuje"))
                .andExpect(jsonPath("$.descripcion").value("Original"))
                .andExpect(jsonPath("$.ejercicios[*].wgerId").value(contains(192)))
                .andExpect(jsonPath("$.ejercicios[0].seriesObjetivo").value(4));
    }

    @Test
    void validacionDelCuerpoRespondeProblemDetail() throws Exception {
        comoMax(conJson(post("/api/v1/rutinas"), "{\"nombre\": \"\", \"ejercicios\": [{\"seriesObjetivo\": 0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type").value("/errores/validacion"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("ejercicios[0].ejercicio: indica exactamente uno de ejercicioId "
                        + "o wgerId; ejercicios[0].seriesObjetivo: debe estar entre 1 y 20; nombre: es obligatorio"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        assertThat(rutinaRepository.count()).isZero();
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/rutinas"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
    }

    // --- Sesiones basadas en una rutina ---

    @Test
    void iniciarUnaSesionConRutinaPrecargaSusEjercicios() throws Exception {
        long curl = curlPropio();
        long rutinaId = crearRutina("""
                {"nombre": "Empuje", "ejercicios": [
                  {"wgerId": 192, "seriesObjetivo": 4, "repeticionesObjetivo": 8},
                  {"ejercicioId": %d, "seriesObjetivo": 3, "repeticionesObjetivo": 12}
                ]}
                """.formatted(curl));

        JsonNode sesion = json(comoMax(conJson(post("/api/v1/sesiones"), """
                {"fecha": "2026-11-10", "rutinaId": %d}
                """.formatted(rutinaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rutinaId").value(rutinaId))
                .andExpect(jsonPath("$.series").isEmpty())
                .andExpect(jsonPath("$.ejerciciosRutina[*].orden").value(contains(1, 2)))
                .andExpect(jsonPath("$.ejerciciosRutina[*].ejercicioNombre")
                        .value(contains("Press de banca", "Curl martillo")))
                .andExpect(jsonPath("$.ejerciciosRutina[0].seriesObjetivo").value(4))
                .andExpect(jsonPath("$.ejerciciosRutina[0].repeticionesObjetivo").value(8))
                .andExpect(jsonPath("$.ejerciciosRutina[1].ejercicioId").value(curl)));
        long sesionId = sesion.get("id").asLong();
        long bancaId = sesion.get("ejerciciosRutina").get(0).get("ejercicioId").asLong();

        // El ejercicioId precargado sirve directamente para registrar series.
        comoMax(conJson(post("/api/v1/sesiones/{id}/series", sesionId), """
                {"ejercicioId": %d, "pesoKg": 80, "repeticiones": 8}
                """.formatted(bancaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ejercicioNombre").value("Press de banca"));

        // El plan también se devuelve al consultar, editar y finalizar la sesión.
        comoMax(get("/api/v1/sesiones/{id}", sesionId))
                .andExpect(jsonPath("$.ejerciciosRutina.length()").value(2))
                .andExpect(jsonPath("$.series.length()").value(1));
        comoMax(conJson(put("/api/v1/sesiones/{id}", sesionId), "{\"fecha\": \"2026-11-10\", \"notas\": \"Bien\"}"))
                .andExpect(jsonPath("$.ejerciciosRutina.length()").value(2));
        comoMax(post("/api/v1/sesiones/{id}/finalizar", sesionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ejerciciosRutina.length()").value(2));
    }

    @Test
    void unaSesionSinRutinaNoTieneEjerciciosPrecargados() throws Exception {
        comoMax(conJson(post("/api/v1/sesiones"), "{\"fecha\": \"2026-11-10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rutinaId").isEmpty())
                .andExpect(jsonPath("$.ejerciciosRutina").isEmpty());
    }

    @Test
    void eliminarUnaRutinaConservaLasSesionesQueLaUsaban() throws Exception {
        long rutinaId = crearRutina("{\"nombre\": \"Empuje\", \"ejercicios\": [{\"wgerId\": 192}]}");
        long sesionId = json(comoMax(conJson(post("/api/v1/sesiones"), "{\"rutinaId\": %d}".formatted(rutinaId)))
                .andExpect(status().isCreated())).get("id").asLong();
        comoMax(conJson(post("/api/v1/sesiones/{id}/series", sesionId),
                "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}"))
                .andExpect(status().isCreated());

        comoMax(delete("/api/v1/rutinas/{id}", rutinaId)).andExpect(status().isNoContent());

        comoMax(get("/api/v1/sesiones/{id}", sesionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rutinaId").isEmpty())
                .andExpect(jsonPath("$.ejerciciosRutina").isEmpty())
                .andExpect(jsonPath("$.series.length()").value(1));
    }

    @Test
    void editarLaRutinaNoAlteraLasSeriesYaRegistradas() throws Exception {
        long rutinaId = crearRutina("{\"nombre\": \"Empuje\", \"ejercicios\": [{\"wgerId\": 192}]}");
        long sesionId = json(comoMax(conJson(post("/api/v1/sesiones"), "{\"rutinaId\": %d}".formatted(rutinaId))))
                .get("id").asLong();
        comoMax(conJson(post("/api/v1/sesiones/{id}/series", sesionId),
                "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}"));

        comoMax(conJson(put("/api/v1/rutinas/{id}", rutinaId), "{\"nombre\": \"Empuje\", \"ejercicios\": [{\"wgerId\": 106}]}"))
                .andExpect(status().isOk());

        comoMax(get("/api/v1/sesiones/{id}", sesionId))
                .andExpect(jsonPath("$.series[0].ejercicioNombre").value("Press de banca"))
                .andExpect(jsonPath("$.ejerciciosRutina[*].ejercicioNombre").value(contains("Remo con barra")));
    }
}
