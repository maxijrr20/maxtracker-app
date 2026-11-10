package com.maxtracker.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.service.EjercicioService;

/**
 * Flujo completo con la aplicación real sobre H2 (perfil "test"): registro, login con BCrypt,
 * emisión del JWT y acceso a una ruta protegida con el filtro JWT. Solo se simula el catálogo de wger.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    private static final String EMAIL = "max@example.com";
    private static final String PASSWORD = "secreta123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EjercicioService ejercicioService;

    @BeforeEach
    void limpiarUsuarios() {
        usuarioRepository.deleteAllInBatch();
        given(ejercicioService.listar(0, 20, null)).willReturn(new PaginaDto<>(List.of(), 0, 20, 0, 0));
    }

    private ResultActions registrar(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(credenciales(email, password)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(credenciales(email, password)));
    }

    private String credenciales(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new Credenciales(email, password));
    }

    private String tokenDe(ResultActions resultado) throws Exception {
        JsonNode json = objectMapper.readTree(resultado.andReturn().getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private record Credenciales(String email, String password) {
    }

    @Test
    void registroGuardaElUsuarioConPasswordHasheadaYDevuelveUnTokenUtil() throws Exception {
        String token = tokenDe(registrar("  Max@Example.com ", PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.expiraEn").exists()));

        Usuario guardado = usuarioRepository.buscarPorEmail(EMAIL).orElseThrow();
        assertThat(guardado.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(passwordEncoder.matches(PASSWORD, guardado.getPasswordHash())).isTrue();

        mockMvc.perform(get("/api/v1/ejercicios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginConCredencialesCorrectasDaAccesoALasRutasProtegidas() throws Exception {
        registrar(EMAIL, PASSWORD).andExpect(status().isCreated());

        String token = tokenDe(login("MAX@example.com", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL)));

        mockMvc.perform(get("/api/v1/ejercicios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void registroConEmailRepetidoSinDistinguirMayusculasResponde409() throws Exception {
        registrar(EMAIL, PASSWORD).andExpect(status().isCreated());

        registrar("MAX@EXAMPLE.COM", "otraClave99")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errores/email-ya-registrado"));

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void loginConPasswordIncorrectaResponde401() throws Exception {
        registrar(EMAIL, PASSWORD).andExpect(status().isCreated());

        login(EMAIL, "incorrecta1")
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/credenciales-invalidas"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginConEmailInexistenteRespondeIgualQueConPasswordIncorrecta() throws Exception {
        login("nadie@example.com", PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("/errores/credenciales-invalidas"))
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos."));
    }

    @Test
    void rutaProtegidaSinTokenResponde401ConProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/ejercicios"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.type").value("/errores/no-autenticado"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void rutaProtegidaConTokenInvalidoResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/ejercicios").header(HttpHeaders.AUTHORIZATION, "Bearer no.es.valido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("/errores/no-autenticado"));
    }

    @Test
    void tokenDeUnUsuarioBorradoDejaDeServir() throws Exception {
        String token = tokenDe(registrar(EMAIL, PASSWORD).andExpect(status().isCreated()));
        usuarioRepository.deleteAllInBatch();

        mockMvc.perform(get("/api/v1/ejercicios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthCheckEsPublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
