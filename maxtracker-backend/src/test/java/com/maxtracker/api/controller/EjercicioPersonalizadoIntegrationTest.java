package com.maxtracker.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioService;

/**
 * /api/v1/ejercicios/personalizados con la aplicación real sobre H2 (perfil "test"): JWT real, JPA, Flyway y
 * aislamiento entre usuarios. También comprueba que se usan en series y rutinas por {@code ejercicioId}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EjercicioPersonalizadoIntegrationTest {

    private static final String URL = "/api/v1/ejercicios/personalizados";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JdbcTemplate jdbc;

    /** El catálogo de wger no se usa aquí; se simula para no depender de internet. */
    @MockitoBean
    private EjercicioService catalogo;

    private String tokenMax;
    private String tokenOtro;

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
        tokenMax = token(usuarioRepository.save(new Usuario("max@example.com", "$2a$10$noSeUsaEnEsteTest")));
        tokenOtro = token(usuarioRepository.save(new Usuario("otro@example.com", "$2a$10$noSeUsaEnEsteTest")));
    }

    private String token(Usuario usuario) {
        return jwtService.generarToken(UsuarioAutenticado.desde(usuario)).valor();
    }

    private ResultActions como(String token, MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private static MockHttpServletRequestBuilder conJson(MockHttpServletRequestBuilder peticion, String json) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private long crear(String token, String json) throws Exception {
        String cuerpo = como(token, conJson(post(URL), json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(cuerpo).get("ejercicioId").asLong();
    }

    @Test
    void creaYListaLosEjerciciosPropiosOrdenadosPorNombre() throws Exception {
        como(tokenMax, conJson(post(URL), """
                {"nombre": "  Remo en   máquina Hammer ", "categoria": "Espalda"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(URL + "/")))
                .andExpect(jsonPath("$.nombre").value("Remo en máquina Hammer"))
                .andExpect(jsonPath("$.categoria").value("Espalda"));
        crear(tokenMax, """
                {"nombre": "Curl en polea baja"}
                """);

        como(tokenMax, get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", contains("Curl en polea baja", "Remo en máquina Hammer")))
                .andExpect(jsonPath("$[0].categoria").doesNotExist());
    }

    @Test
    void cadaUsuarioSoloVeLosSuyos() throws Exception {
        crear(tokenMax, """
                {"nombre": "Hip thrust en máquina", "categoria": "Piernas"}
                """);

        como(tokenOtro, get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rechazaNombreRepetidoSinDistinguirMayusculas() throws Exception {
        crear(tokenMax, """
                {"nombre": "Prensa 45°"}
                """);

        como(tokenMax, conJson(post(URL), """
                {"nombre": "PRENSA 45°"}
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errores/ejercicio-duplicado"));
        // Otro usuario sí puede usar el mismo nombre.
        crear(tokenOtro, """
                {"nombre": "Prensa 45°"}
                """);
    }

    @Test
    void validaNombreYGrupoMuscular() throws Exception {
        como(tokenMax, conJson(post(URL), """
                {"nombre": "   ", "categoria": "Glúteos"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("nombre: es obligatorio")))
                .andExpect(jsonPath("$.detail", containsString("categoria: debe ser un grupo muscular del catálogo")));
    }

    @Test
    void seUsaEnSeriesPorEjercicioIdYNoSePuedeBorrarSiTieneHistorial() throws Exception {
        long id = crear(tokenMax, """
                {"nombre": "Press en máquina convergente", "categoria": "Pecho"}
                """);
        String sesion = como(tokenMax, conJson(post("/api/v1/sesiones"), "{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long sesionId = objectMapper.readTree(sesion).get("id").asLong();

        como(tokenMax, conJson(post("/api/v1/sesiones/" + sesionId + "/series"), """
                {"ejercicioId": %d, "pesoKg": 40, "repeticiones": 10}
                """.formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ejercicioNombre").value("Press en máquina convergente"));

        como(tokenMax, delete(URL + "/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errores/ejercicio-en-uso"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ejercicio WHERE id = ?", Integer.class, id)).isEqualTo(1);
    }

    @Test
    void eliminaUnoSinUsoYNoPermiteBorrarLosDeOtroUsuario() throws Exception {
        long id = crear(tokenMax, """
                {"nombre": "Ejercicio con error de tipeo"}
                """);

        como(tokenOtro, delete(URL + "/" + id)).andExpect(status().isNotFound());
        como(tokenMax, delete(URL + "/" + id)).andExpect(status().isNoContent());
        como(tokenMax, get(URL)).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void elOtroUsuarioNoPuedeRegistrarSeriesConUnEjercicioAjeno() throws Exception {
        long id = crear(tokenMax, """
                {"nombre": "Solo de Max"}
                """);
        String sesion = como(tokenOtro, conJson(post("/api/v1/sesiones"), "{}"))
                .andReturn().getResponse().getContentAsString();
        long sesionId = objectMapper.readTree(sesion).get("id").asLong();

        como(tokenOtro, conJson(post("/api/v1/sesiones/" + sesionId + "/series"), """
                {"ejercicioId": %d, "pesoKg": 10, "repeticiones": 10}
                """.formatted(id)))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }
}
