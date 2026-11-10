package com.maxtracker.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.SesionEntrenamiento;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioService;

/**
 * Con el adaptador real de Gemini y sin GEMINI_API_KEY (perfil "test"): la app arranca y el análisis responde
 * 503 explicando qué falta, sin intentar conectarse.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalisisSinIaConfiguradaTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private Clock clock;

    /** Mismo conjunto de mocks que los otros tests de integración: comparten el contexto de Spring. */
    @MockitoBean
    private EjercicioService catalogo;

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM serie_registro");
        jdbc.update("DELETE FROM sesion_entrenamiento");
        jdbc.update("DELETE FROM ejercicio");
        jdbc.update("DELETE FROM usuario");
    }

    @Test
    void sinClaveResponde503ExplicandoQueFalta() throws Exception {
        Usuario max = usuarioRepository.save(new Usuario("max@example.com", "$2a$10$noSeUsaEnEsteTest"));
        Ejercicio banca = ejercicioRepository.save(Ejercicio.desdeWger(192, "Press de banca", "Pecho"));
        SesionEntrenamiento sesion = new SesionEntrenamiento(max, LocalDate.now(clock));
        sesion.registrarSerie(banca, new BigDecimal("80"), 6, 2);
        sesionRepository.save(sesion);
        String token = jwtService.generarToken(UsuarioAutenticado.desde(max)).valor();

        mockMvc.perform(post("/api/v1/analisis/rutina").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.type").value("/errores/ia-no-disponible"))
                .andExpect(jsonPath("$.detail").value(containsString("GEMINI_API_KEY")));
    }
}
