package com.maxtracker.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxtracker.api.exception.IaNoDisponibleException;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.Rutina;
import com.maxtracker.api.model.SesionEntrenamiento;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.repository.RutinaRepository;
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioService;
import com.maxtracker.api.service.ia.AsistenteEntrenamiento;
import com.maxtracker.api.service.ia.ContextoAnalisis;
import com.maxtracker.api.service.ia.ContextoAnalisis.EjercicioContexto;
import com.maxtracker.api.service.ia.OpcionesAnalisis;
import com.maxtracker.api.service.ia.RecomendacionIa;
import com.maxtracker.api.service.ia.RecomendacionIa.EjercicioRecomendacion;
import com.maxtracker.api.service.ia.RecomendacionIa.Tendencia;

/**
 * POST /api/v1/analisis/rutina con la aplicación real sobre H2 y el puerto de IA simulado: verifica qué historial
 * se arma (y que no incluye datos de la cuenta), el aislamiento entre usuarios y los errores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalisisIntegrationTest {

    private static final RecomendacionIa RECOMENDACION = new RecomendacionIa("Vas bien.",
            List.of(new EjercicioRecomendacion("Press de banca", Tendencia.PROGRESA, "Subiste 2,5 kg.",
                    "Prueba 85 kg.")),
            "Registra el RIR.", List.of("Mantén 4 series"), "modelo-de-prueba");

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
    private SesionEntrenamientoRepository sesionRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Clock clock;

    @MockitoBean
    private EjercicioService catalogo;

    @MockitoBean
    private AsistenteEntrenamiento asistente;

    private Usuario max;
    private Usuario otro;
    private Ejercicio banca;
    private Ejercicio remo;
    private LocalDate hoy;

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
        // Cada test usa usuarios nuevos (ids nuevos): el límite de frecuencia por usuario no se cruza entre tests.
        max = usuarioRepository.save(new Usuario("max@example.com", "$2a$10$noSeUsaEnEsteTest"));
        otro = usuarioRepository.save(new Usuario("otro@example.com", "$2a$10$noSeUsaEnEsteTest"));
        banca = ejercicioRepository.save(Ejercicio.desdeWger(192, "Press de banca", "Pecho"));
        remo = ejercicioRepository.save(Ejercicio.desdeWger(106, "Remo con barra", "Espalda"));
        hoy = LocalDate.now(clock);
        given(asistente.analizar(any(), any())).willReturn(RECOMENDACION);
    }

    private ResultActions analizar(Usuario usuario, String json) throws Exception {
        String token = jwtService.generarToken(UsuarioAutenticado.desde(usuario)).valor();
        var peticion = post("/api/v1/analisis/rutina").header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return mockMvc.perform(json == null ? peticion
                : peticion.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void laClavePersonalYElEnfoqueLleganAlPuertoYNoAlPrompt() throws Exception {
        sesion(max, hoy, null, banca, "80x6");
        String token = jwtService.generarToken(UsuarioAutenticado.desde(max)).valor();

        mockMvc.perform(post("/api/v1/analisis/rutina")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-Gemini-Api-Key", "AIza-clave-personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enfoque\": \"HIPERTROFIA\", \"unidadPeso\": \"LB\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<ContextoAnalisis> contexto = ArgumentCaptor.forClass(ContextoAnalisis.class);
        ArgumentCaptor<OpcionesAnalisis> opciones = ArgumentCaptor.forClass(OpcionesAnalisis.class);
        verify(asistente).analizar(contexto.capture(), opciones.capture());
        assertThat(opciones.getValue().claveApiUsuario()).isEqualTo("AIza-clave-personal");
        assertThat(opciones.getValue().enfoque()).isEqualTo(OpcionesAnalisis.Enfoque.HIPERTROFIA);
        assertThat(opciones.getValue().unidadPeso()).isEqualTo(OpcionesAnalisis.UnidadPeso.LB);
        // El contexto (lo que se serializa en el prompt) nunca lleva la clave.
        assertThat(objectMapper.writeValueAsString(contexto.getValue())).doesNotContain("AIza-clave-personal");
    }

    private SesionEntrenamiento sesion(Usuario usuario, LocalDate fecha, Rutina rutina, Ejercicio ejercicio,
            String... seriesPesoXReps) {
        SesionEntrenamiento sesion = new SesionEntrenamiento(usuario, fecha);
        sesion.setRutina(rutina);
        for (String serie : seriesPesoXReps) {
            String[] partes = serie.split("x");
            sesion.registrarSerie(ejercicio, new BigDecimal(partes[0]), Integer.parseInt(partes[1]), 2);
        }
        return sesionRepository.save(sesion);
    }

    private Rutina rutinaEmpuje() {
        Rutina rutina = new Rutina(max, "Empuje", null);
        rutina.agregarEjercicio(remo, null, null);
        rutina.agregarEjercicio(banca, 4, 8);
        return rutinaRepository.save(rutina);
    }

    private ContextoAnalisis contextoEnviado() {
        ArgumentCaptor<ContextoAnalisis> captor = ArgumentCaptor.forClass(ContextoAnalisis.class);
        verify(asistente).analizar(captor.capture(), any());
        return captor.getValue();
    }

    @Test
    void analisisDeRutinaEnviaSusEjerciciosConObjetivosYSoloDelPeriodo() throws Exception {
        Rutina rutina = rutinaEmpuje();
        sesion(max, hoy.minusDays(60), rutina, banca, "60x10"); // fuera de los 28 días
        sesion(max, hoy.minusDays(7), rutina, banca, "80x6", "80x6");
        sesion(max, hoy, rutina, banca, "82.5x6");
        sesion(max, hoy, null, remo, "70x8");
        sesion(otro, hoy, null, banca, "200x1"); // de otro usuario

        analizar(max, "{\"rutinaId\": %d}".formatted(rutina.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alcance").value("Rutina Empuje"))
                .andExpect(jsonPath("$.resumen").value("Vas bien."))
                .andExpect(jsonPath("$.ejercicios[0].tendencia").value("PROGRESA"))
                .andExpect(jsonPath("$.dias").value(28))
                .andExpect(jsonPath("$.seriesAnalizadas").value(4))
                .andExpect(jsonPath("$.modelo").value("modelo-de-prueba"))
                .andExpect(jsonPath("$.generadoEn").isNotEmpty());

        ContextoAnalisis contexto = contextoEnviado();
        assertThat(contexto.alcance()).isEqualTo("Rutina Empuje");
        assertThat(contexto.dias()).isEqualTo(28);
        // En el orden de la rutina (remo primero).
        assertThat(contexto.ejercicios()).extracting(EjercicioContexto::nombre)
                .containsExactly("Remo con barra", "Press de banca");
        EjercicioContexto bancaCtx = contexto.ejercicios().get(1);
        assertThat(bancaCtx.seriesObjetivo()).isEqualTo(4);
        assertThat(bancaCtx.repeticionesObjetivo()).isEqualTo(8);
        assertThat(bancaCtx.sesiones()).hasSize(2);
        assertThat(bancaCtx.sesiones().get(1).series().get(0).rir()).isEqualTo(2);
        assertThat(bancaCtx.unoRmInicialKg()).isEqualByComparingTo("96.00");
        assertThat(bancaCtx.unoRmFinalKg()).isEqualByComparingTo("99.00");
        // Nunca datos de la cuenta.
        assertThat(objectMapper.writeValueAsString(contexto)).doesNotContain("max@example.com", "@");
    }

    @Test
    void analisisDeUnaSesionIncluyeSusEjerciciosYElHistorialReciente() throws Exception {
        sesion(max, hoy.minusDays(3), null, banca, "80x6");
        SesionEntrenamiento recien = sesion(max, hoy, null, banca, "82.5x6");
        sesion(max, hoy, null, remo, "70x8"); // otra sesión: su ejercicio no entra

        analizar(max, "{\"sesionId\": %d, \"dias\": 7}".formatted(recien.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alcance").value("Sesión del " + hoy))
                .andExpect(jsonPath("$.dias").value(7));

        ContextoAnalisis contexto = contextoEnviado();
        assertThat(contexto.ejercicios()).extracting(EjercicioContexto::nombre).containsExactly("Press de banca");
        assertThat(contexto.ejercicios().get(0).sesiones()).hasSize(2);
        assertThat(contexto.ejercicios().get(0).seriesObjetivo()).isNull();
    }

    @Test
    void sinCuerpoAnalizaTodoLoEntrenadoEnElPeriodo() throws Exception {
        sesion(max, hoy.minusDays(2), null, banca, "80x6");
        sesion(max, hoy, null, remo, "70x8");

        analizar(max, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alcance").value("Últimos 28 días"));

        // Sin rutina, del ejercicio entrenado más recientemente al más antiguo.
        assertThat(contextoEnviado().ejercicios()).extracting(EjercicioContexto::nombre)
                .containsExactly("Remo con barra", "Press de banca");
    }

    @Test
    void sinSeriesEnElPeriodoResponde422YNoConsultaALaIa() throws Exception {
        sesion(max, hoy.minusDays(60), null, banca, "80x6");

        analizar(max, "{}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("/errores/sin-datos-para-analisis"));
        analizar(max, "{\"rutinaId\": %d}".formatted(rutinaRepository.save(new Rutina(max, "Vacía", null)).getId()))
                .andExpect(status().isUnprocessableEntity());

        verify(asistente, never()).analizar(any(), any());
    }

    @Test
    void laRutinaOSesionDeOtroUsuarioEs404() throws Exception {
        Rutina rutina = rutinaEmpuje();
        SesionEntrenamiento sesion = sesion(max, hoy, rutina, banca, "80x6");

        analizar(otro, "{\"rutinaId\": %d}".formatted(rutina.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una rutina con id " + rutina.getId()));
        analizar(otro, "{\"sesionId\": %d}".formatted(sesion.getId()))
                .andExpect(status().isNotFound());

        verify(asistente, never()).analizar(any(), any());
    }

    @Test
    void unSegundoAnalisisInmediatoResponde429ConRetryAfter() throws Exception {
        sesion(max, hoy, null, banca, "80x6");

        analizar(max, null).andExpect(status().isOk());
        analizar(max, null)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.type").value("/errores/demasiadas-solicitudes"));
        // El límite es por usuario.
        sesion(otro, hoy, null, banca, "80x6");
        analizar(otro, null).andExpect(status().isOk());
    }

    @Test
    void siLaIaFallaResponde503() throws Exception {
        sesion(max, hoy, null, banca, "80x6");
        given(asistente.analizar(any(), any())).willThrow(IaNoDisponibleException.cuotaAgotada(null));

        analizar(max, null)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.type").value("/errores/ia-no-disponible"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("límite de uso gratuito")));
    }
}
