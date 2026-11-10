package com.maxtracker.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioService;

/**
 * /api/v1/sesiones con la aplicación real sobre H2 (perfil "test"): JWT real, JPA, Flyway (incluida la V2)
 * y aislamiento entre usuarios. Solo se simula el catálogo de wger.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SesionIntegrationTest {

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
    private SesionEntrenamientoRepository sesionRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private EjercicioService catalogo;

    private Usuario max;
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
        Usuario otro = usuarioRepository.save(new Usuario("otro@example.com", "$2a$10$noSeUsaEnEsteTest"));
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

    private long idDe(ResultActions resultado) throws Exception {
        JsonNode json = objectMapper.readTree(resultado.andReturn().getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private long iniciarSesion() throws Exception {
        return idDe(comoMax(conJson(post("/api/v1/sesiones"), """
                {"fecha": "2026-11-10", "notas": "Pecho y espalda"}
                """)).andExpect(status().isCreated()));
    }

    private ResultActions registrarSerie(long sesionId, String json) throws Exception {
        return comoMax(conJson(post("/api/v1/sesiones/{id}/series", sesionId), json));
    }

    @Test
    void flujoCompletoIniciarRegistrarSeriesYFinalizar() throws Exception {
        long sesionId = iniciarSesion();

        registrarSerie(sesionId, """
                {"wgerId": 192, "pesoKg": 80, "repeticiones": 6, "rir": 2}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.wgerId").value(192))
                .andExpect(jsonPath("$.ejercicioNombre").value("Press de banca"))
                .andExpect(jsonPath("$.numeroSerie").value(1))
                .andExpect(jsonPath("$.pesoKg").value(80.00));
        registrarSerie(sesionId, """
                {"wgerId": 106, "pesoKg": 70.5, "repeticiones": 8}
                """)
                .andExpect(jsonPath("$.numeroSerie").value(1))
                .andExpect(jsonPath("$.rir").isEmpty());
        registrarSerie(sesionId, """
                {"wgerId": 192, "pesoKg": 82.5, "repeticiones": 5, "rir": 1}
                """)
                .andExpect(jsonPath("$.numeroSerie").value(2));

        comoMax(post("/api/v1/sesiones/{id}/finalizar", sesionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalizada").value(true))
                .andExpect(jsonPath("$.finalizadaEn").isNotEmpty());

        comoMax(get("/api/v1/sesiones/{id}", sesionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notas").value("Pecho y espalda"))
                .andExpect(jsonPath("$.series.length()").value(3))
                .andExpect(jsonPath("$.series[*].ejercicioNombre")
                        .value(org.hamcrest.Matchers.contains("Press de banca", "Remo con barra", "Press de banca")))
                .andExpect(jsonPath("$.series[*].numeroSerie").value(org.hamcrest.Matchers.contains(1, 1, 2)))
                .andExpect(jsonPath("$.series[2].pesoKg").value(82.50));
    }

    @Test
    void laCopiaLocalDeWgerSeCreaUnaSolaVezYSeReutiliza() throws Exception {
        long sesionId = iniciarSesion();

        long ejercicioId = objectMapper.readTree(registrarSerie(sesionId, """
                        {"wgerId": 192, "pesoKg": 80, "repeticiones": 6}
                        """).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .get("ejercicioId").asLong();
        registrarSerie(sesionId, """
                {"wgerId": 192, "pesoKg": 80, "repeticiones": 6}
                """).andExpect(status().isCreated());
        registrarSerie(sesionId, """
                {"ejercicioId": %d, "pesoKg": 80, "repeticiones": 5}
                """.formatted(ejercicioId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroSerie").value(3));

        verify(catalogo, times(1)).obtenerPorId(192);
        Ejercicio copia = ejercicioRepository.findByWgerId(192L).orElseThrow();
        assertThat(copia.getNombre()).isEqualTo("Press de banca");
        assertThat(copia.getCategoria()).isEqualTo("Pecho");
        assertThat(copia.esPersonalizado()).isFalse();
    }

    @Test
    void iniciarSinFechaUsaLaDeHoy() throws Exception {
        comoMax(post("/api/v1/sesiones"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fecha").isNotEmpty())
                .andExpect(jsonPath("$.iniciadaEn").isNotEmpty())
                .andExpect(jsonPath("$.finalizada").value(false));
    }

    @Test
    void unaSesionFinalizadaNoAdmiteSeriesNuevasNiBorrarlas() throws Exception {
        long sesionId = iniciarSesion();
        long serieId = idDe(registrarSerie(sesionId, """
                {"wgerId": 192, "pesoKg": 80, "repeticiones": 6}
                """));
        comoMax(post("/api/v1/sesiones/{id}/finalizar", sesionId)).andExpect(status().isOk());

        registrarSerie(sesionId, """
                {"wgerId": 106, "pesoKg": 70, "repeticiones": 8}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errores/sesion-finalizada"));
        comoMax(delete("/api/v1/sesiones/{id}/series/{serieId}", sesionId, serieId))
                .andExpect(status().isConflict());
        comoMax(post("/api/v1/sesiones/{id}/finalizar", sesionId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("La sesión ya está finalizada"));

        // Ni siquiera se consultó wger para el ejercicio rechazado.
        verify(catalogo, times(0)).obtenerPorId(106);
        // Editar fecha y notas sí se permite después de finalizar.
        comoMax(conJson(put("/api/v1/sesiones/{id}", sesionId), """
                {"fecha": "2026-11-09", "notas": "Agregado después"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-11-09"))
                .andExpect(jsonPath("$.series.length()").value(1));
    }

    @Test
    void borrarUnaSerieIntermediaNoRepiteElNumeroDeSerie() throws Exception {
        long sesionId = iniciarSesion();
        registrarSerie(sesionId, "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}");
        long segunda = idDe(registrarSerie(sesionId, "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}"));
        registrarSerie(sesionId, "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}");

        comoMax(delete("/api/v1/sesiones/{id}/series/{serieId}", sesionId, segunda))
                .andExpect(status().isNoContent());

        registrarSerie(sesionId, "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroSerie").value(4));
        comoMax(get("/api/v1/sesiones/{id}", sesionId))
                .andExpect(jsonPath("$.series[*].numeroSerie").value(org.hamcrest.Matchers.contains(1, 3, 4)));
    }

    @Test
    void unUsuarioNoPuedeVerNiModificarSesionesDeOtro() throws Exception {
        long sesionId = iniciarSesion();
        long serieId = idDe(registrarSerie(sesionId, "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}"));

        comoOtro(get("/api/v1/sesiones/{id}", sesionId)).andExpect(status().isNotFound());
        comoOtro(conJson(put("/api/v1/sesiones/{id}", sesionId), "{\"fecha\": \"2026-01-01\"}"))
                .andExpect(status().isNotFound());
        comoOtro(post("/api/v1/sesiones/{id}/finalizar", sesionId)).andExpect(status().isNotFound());
        comoOtro(conJson(post("/api/v1/sesiones/{id}/series", sesionId),
                "{\"wgerId\": 192, \"pesoKg\": 1, \"repeticiones\": 1}")).andExpect(status().isNotFound());
        comoOtro(delete("/api/v1/sesiones/{id}/series/{serieId}", sesionId, serieId)).andExpect(status().isNotFound());
        comoOtro(delete("/api/v1/sesiones/{id}", sesionId)).andExpect(status().isNotFound());
        comoOtro(get("/api/v1/sesiones")).andExpect(jsonPath("$.totalElementos").value(0));

        comoMax(get("/api/v1/sesiones/{id}", sesionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-11-10"))
                .andExpect(jsonPath("$.finalizada").value(false))
                .andExpect(jsonPath("$.series.length()").value(1));
    }

    @Test
    void noSePuedeUsarElEjercicioPersonalizadoDeOtroUsuario() throws Exception {
        Usuario otro = usuarioRepository.buscarPorEmail("otro@example.com").orElseThrow();
        long ajeno = ejercicioRepository.save(Ejercicio.personalizado(otro, "Curl secreto", null)).getId();
        long propio = ejercicioRepository.save(Ejercicio.personalizado(max, "Curl martillo", "Brazos")).getId();
        long sesionId = iniciarSesion();

        registrarSerie(sesionId, "{\"ejercicioId\": %d, \"pesoKg\": 10, \"repeticiones\": 12}".formatted(ajeno))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un ejercicio con id " + ajeno));
        registrarSerie(sesionId, "{\"ejercicioId\": %d, \"pesoKg\": 10, \"repeticiones\": 12}".formatted(propio))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ejercicioNombre").value("Curl martillo"))
                .andExpect(jsonPath("$.wgerId").isEmpty());
    }

    @Test
    void siWgerNoEstaDisponibleNoSeGuardaNada() throws Exception {
        given(catalogo.obtenerPorId(anyLong())).willThrow(new WgerApiException("No se pudo obtener el ejercicio"));
        long sesionId = iniciarSesion();

        registrarSerie(sesionId, "{\"wgerId\": 300, \"pesoKg\": 50, \"repeticiones\": 10}")
                .andExpect(status().isBadGateway());

        assertThat(ejercicioRepository.findByWgerId(300L)).isEmpty();
        comoMax(get("/api/v1/sesiones/{id}", sesionId)).andExpect(jsonPath("$.series.length()").value(0));
    }

    @Test
    void iniciarConRutinaInexistenteResponde404YNoCreaLaSesion() throws Exception {
        comoMax(conJson(post("/api/v1/sesiones"), "{\"rutinaId\": 999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una rutina con id 999999"));

        assertThat(sesionRepository.count()).isZero();
    }

    @Test
    void listarDevuelveElHistorialDelUsuarioDelMasRecienteAlMasAntiguo() throws Exception {
        for (String fecha : List.of("2026-11-03", "2026-11-10", "2026-11-05")) {
            comoMax(conJson(post("/api/v1/sesiones"), "{\"fecha\": \"%s\"}".formatted(fecha)))
                    .andExpect(status().isCreated());
        }

        comoMax(get("/api/v1/sesiones").param("tamanio", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[*].fecha")
                        .value(org.hamcrest.Matchers.contains("2026-11-10", "2026-11-05")))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void eliminarBorraLaSesionYSusSeries() throws Exception {
        long sesionId = iniciarSesion();
        registrarSerie(sesionId, "{\"wgerId\": 192, \"pesoKg\": 80, \"repeticiones\": 6}");

        comoMax(delete("/api/v1/sesiones/{id}", sesionId)).andExpect(status().isNoContent());

        comoMax(get("/api/v1/sesiones/{id}", sesionId)).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM serie_registro", Long.class)).isZero();
        // La copia local del ejercicio se conserva: es compartida.
        assertThat(ejercicioRepository.findByWgerId(192L)).isPresent();
    }
}
