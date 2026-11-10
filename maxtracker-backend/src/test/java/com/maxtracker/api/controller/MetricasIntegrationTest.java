package com.maxtracker.api.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.SesionEntrenamiento;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioService;
import com.maxtracker.api.service.metricas.CalculadoraMetricas;

/**
 * /api/v1/metricas con la aplicación real sobre H2: valida las consultas JPQL con proyección
 * ({@code SerieHistorial}), los cálculos y el aislamiento entre usuarios. Las fechas se calculan desde el
 * reloj de la app, así el test no depende del día en que se ejecute.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetricasIntegrationTest {

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

    private Usuario max;
    private Usuario otro;
    private Ejercicio banca;
    private Ejercicio remo;
    private LocalDate hoy;
    private LocalDate lunes;

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
        banca = ejercicioRepository.save(Ejercicio.desdeWger(192, "Press de banca", "Pecho"));
        remo = ejercicioRepository.save(Ejercicio.desdeWger(106, "Remo con barra", "Espalda"));
        hoy = LocalDate.now(clock);
        lunes = CalculadoraMetricas.inicioSemana(hoy);
    }

    private ResultActions como(Usuario usuario, MockHttpServletRequestBuilder peticion) throws Exception {
        String token = jwtService.generarToken(UsuarioAutenticado.desde(usuario)).valor();
        return mockMvc.perform(peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    /** Sesión con series {peso, reps} de un ejercicio. */
    private void sesion(Usuario usuario, LocalDate fecha, Ejercicio ejercicio, String... seriesPesoXReps) {
        SesionEntrenamiento sesion = new SesionEntrenamiento(usuario, fecha);
        for (String serie : seriesPesoXReps) {
            String[] partes = serie.split("x");
            sesion.registrarSerie(ejercicio, new BigDecimal(partes[0]), Integer.parseInt(partes[1]), null);
        }
        sesionRepository.save(sesion);
    }

    @Test
    void resumenSumaLaSemanaActualYLaAnteriorYCalculaRecords() throws Exception {
        sesion(max, hoy, banca, "80x6", "82.5x5"); // 480 + 412,5 = 892,5
        sesion(max, hoy, remo, "70x8"); // 560 (otra sesión el mismo día)
        sesion(max, lunes.minusDays(3), banca, "85x3"); // semana anterior: 255 (y récord de peso)
        sesion(max, hoy.minusYears(1), banca, "60x10"); // fuera de los periodos, cuenta para récords
        sesion(otro, hoy, banca, "200x1"); // de otro usuario: no cuenta

        como(max, get("/api/v1/metricas/resumen").param("semanas", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semanaActual.volumenKg").value(1452.50))
                .andExpect(jsonPath("$.semanaActual.sesiones").value(2))
                .andExpect(jsonPath("$.semanaActual.series").value(3))
                .andExpect(jsonPath("$.semanaAnterior.volumenKg").value(255.00))
                .andExpect(jsonPath("$.semanaAnterior.sesiones").value(1))
                .andExpect(jsonPath("$.volumenSemanal.length()").value(4))
                .andExpect(jsonPath("$.volumenSemanal[3].inicioSemana").value(lunes.toString()))
                .andExpect(jsonPath("$.volumenSemanal[3].volumenKg").value(1452.50))
                .andExpect(jsonPath("$.volumenSemanal[2].volumenKg").value(255.00))
                .andExpect(jsonPath("$.records.length()").value(2))
                .andExpect(jsonPath("$.records[?(@.ejercicioNombre == 'Press de banca')].pesoMaxKg").value(85.00))
                .andExpect(jsonPath("$.records[?(@.ejercicioNombre == 'Press de banca')].repeticiones").value(3))
                // Mejor 1RM estimado del historial: 82,5 × (1 + 5/30) = 96,25
                .andExpect(jsonPath("$.records[?(@.ejercicioNombre == 'Press de banca')].unoRmEstimadoKg")
                        .value(96.25))
                .andExpect(jsonPath("$.records[?(@.ejercicioNombre == 'Remo con barra')].pesoMaxKg").value(70.00));
    }

    @Test
    void sinSeriesTodoEnCero() throws Exception {
        como(max, get("/api/v1/metricas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semanaActual.volumenKg").value(0))
                .andExpect(jsonPath("$.mesActual.sesiones").value(0))
                .andExpect(jsonPath("$.volumenSemanal.length()").value(12))
                .andExpect(jsonPath("$.records").isEmpty());
        como(max, get("/api/v1/metricas/ejercicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void ejerciciosConHistorialYProgresoPorSesion() throws Exception {
        sesion(max, hoy.minusDays(14), banca, "80x6", "80x5");
        sesion(max, hoy.minusDays(7), banca, "82.5x5");
        sesion(max, hoy.minusDays(10), remo, "70x8");
        sesion(otro, hoy, banca, "200x1");

        como(max, get("/api/v1/metricas/ejercicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].ejercicioNombre").value(contains("Press de banca", "Remo con barra")))
                .andExpect(jsonPath("$[0].series").value(3))
                .andExpect(jsonPath("$[0].wgerId").value(192));

        como(max, get("/api/v1/metricas/ejercicios/{id}/progreso", banca.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ejercicioNombre").value("Press de banca"))
                .andExpect(jsonPath("$.puntos.length()").value(2))
                .andExpect(jsonPath("$.puntos[*].fecha")
                        .value(contains(hoy.minusDays(14).toString(), hoy.minusDays(7).toString())))
                .andExpect(jsonPath("$.puntos[0].pesoMaxKg").value(80.00))
                .andExpect(jsonPath("$.puntos[0].volumenKg").value(880.00))
                .andExpect(jsonPath("$.puntos[0].series").value(2))
                .andExpect(jsonPath("$.puntos[1].pesoMaxKg").value(82.50));
    }

    @Test
    void progresoDeEjercicioSinSeriesEsVacioYElPersonalizadoDeOtroEs404() throws Exception {
        Ejercicio ajeno = ejercicioRepository.save(Ejercicio.personalizado(otro, "Curl secreto", null));

        como(max, get("/api/v1/metricas/ejercicios/{id}/progreso", remo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntos").isEmpty());
        como(max, get("/api/v1/metricas/ejercicios/{id}/progreso", ajeno.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un ejercicio con id " + ajeno.getId()));
    }
}
