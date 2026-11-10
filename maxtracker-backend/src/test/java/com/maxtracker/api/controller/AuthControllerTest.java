package com.maxtracker.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.maxtracker.api.dto.auth.AuthResponse;
import com.maxtracker.api.dto.auth.LoginRequest;
import com.maxtracker.api.dto.auth.RegisterRequest;
import com.maxtracker.api.exception.EmailYaRegistradoException;
import com.maxtracker.api.exception.RegistroDeshabilitadoException;
import com.maxtracker.api.security.ConSeguridadJwt;
import com.maxtracker.api.service.AuthService;

/** Contrato HTTP de /api/v1/auth con la configuración de seguridad real y el servicio simulado. */
@WebMvcTest(AuthController.class)
@ConSeguridadJwt
class AuthControllerTest {

    private static final AuthResponse RESPUESTA =
            new AuthResponse("eyJ.token.firmado", "Bearer", Instant.parse("2026-11-11T00:00:00Z"), "max@example.com");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private static MockHttpServletRequestBuilder postJson(String url, String json) {
        return post(url).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    @Test
    void registerEsPublicoYResponde201ConElToken() throws Exception {
        given(authService.registrar(new RegisterRequest("max@example.com", "secreta123"))).willReturn(RESPUESTA);

        mockMvc.perform(postJson("/api/v1/auth/register", """
                        {"email": "max@example.com", "password": "secreta123"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("eyJ.token.firmado"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").value("2026-11-11T00:00:00Z"))
                .andExpect(jsonPath("$.email").value("max@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerRechazaEmailInvalidoYPasswordCorta() throws Exception {
        mockMvc.perform(postJson("/api/v1/auth/register", """
                        {"email": "no-es-email", "password": "corta"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/validacion"))
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.detail").value(
                        "email: debe ser un email válido; password: debe tener entre 8 y 72 caracteres"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void registerRechazaCamposAusentes() throws Exception {
        mockMvc.perform(postJson("/api/v1/auth/register", "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("email: es obligatorio; password: es obligatoria"));

        verifyNoInteractions(authService);
    }

    @Test
    void registerRechazaJsonMalFormado() throws Exception {
        mockMvc.perform(postJson("/api/v1/auth/register", "{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void registerResponde409SiElEmailYaExiste() throws Exception {
        given(authService.registrar(any())).willThrow(new EmailYaRegistradoException());

        mockMvc.perform(postJson("/api/v1/auth/register", """
                        {"email": "max@example.com", "password": "secreta123"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/email-ya-registrado"))
                .andExpect(jsonPath("$.detail").value("Ya existe una cuenta con ese email"));
    }

    @Test
    void registerResponde403SiElRegistroEstaDeshabilitado() throws Exception {
        given(authService.registrar(any())).willThrow(new RegistroDeshabilitadoException());

        mockMvc.perform(postJson("/api/v1/auth/register", """
                        {"email": "max@example.com", "password": "secreta123"}
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("/errores/registro-deshabilitado"));
    }

    @Test
    void loginEsPublicoYDevuelveElToken() throws Exception {
        given(authService.login(new LoginRequest("max@example.com", "secreta123"))).willReturn(RESPUESTA);

        mockMvc.perform(postJson("/api/v1/auth/login", """
                        {"email": "max@example.com", "password": "secreta123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJ.token.firmado"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void loginResponde401ConProblemDetailSiLasCredencialesSonIncorrectas() throws Exception {
        given(authService.login(any())).willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(postJson("/api/v1/auth/login", """
                        {"email": "max@example.com", "password": "incorrecta"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.type").value("/errores/credenciales-invalidas"))
                .andExpect(jsonPath("$.title").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos."))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginRechazaCamposVacios() throws Exception {
        mockMvc.perform(postJson("/api/v1/auth/login", """
                        {"email": "", "password": ""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("email: es obligatorio; password: es obligatoria"));

        verifyNoInteractions(authService);
    }
}
