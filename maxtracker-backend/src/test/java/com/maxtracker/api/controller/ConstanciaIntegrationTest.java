package com.maxtracker.api.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;

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

import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.model.PlanEntrenamiento;
import com.maxtracker.api.model.SesionEntrenamiento;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.repository.PlanEntrenamientoRepository;
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.EjercicioService;

/**
 * /api/v1/constancia con la aplicación real sobre H2 (perfil "test"). Las fechas se calculan desde el reloj de la
 * app, así el test no depende del día en que se ejecute.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConstanciaIntegrationTest {

    private static final String URL = "/api/v1/constancia";

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
    private PlanEntrenamientoRepository planRepository;

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
    private LocalDate hoy;

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM plan_entrenamiento");
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
        hoy = LocalDate.now(clock);
    }

    private ResultActions como(Usuario usuario, MockHttpServletRequestBuilder peticion) throws Exception {
        String token = jwtService.generarToken(UsuarioAutenticado.desde(usuario)).valor();
        return mockMvc.perform(peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private static MockHttpServletRequestBuilder plan(String json) {
        return put(URL + "/plan").contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private void entreno(Usuario usuario, LocalDate fecha) {
        SesionEntrenamiento sesion = new SesionEntrenamiento(usuario, fecha);
        sesion.registrarSerie(banca, new BigDecimal("60"), 8, null);
        sesionRepository.save(sesion);
    }

    /** Plan de todos los días desde hace dos semanas: cada día pasado sin entrenar es un fallo. */
    private void planDiarioDesde(LocalDate desde) {
        planRepository.save(new PlanEntrenamiento(max, EnumSet.allOf(DayOfWeek.class), desde));
    }

    @Test
    void sinPlanMuestraLosDiasEntrenadosPeroNoRachaNiFallos() throws Exception {
        entreno(max, hoy);

        como(max, get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes").value(YearMonth.from(hoy).toString()))
                .andExpect(jsonPath("$.dias.length()").value(hoy.lengthOfMonth()))
                .andExpect(jsonPath("$.dias[%d].estado".formatted(hoy.getDayOfMonth() - 1)).value("EXTRA"))
                .andExpect(jsonPath("$.resumenTotal.entrenados").value(1))
                .andExpect(jsonPath("$.resumenTotal.fallados").value(0))
                .andExpect(jsonPath("$.rachaActual").doesNotExist())
                .andExpect(jsonPath("$.plan").doesNotExist());
        como(max, get(URL + "/plan")).andExpect(status().isNoContent());
    }

    @Test
    void guardaElPlanQueRigeDesdeHoyYNoDuplicaSiNoCambia() throws Exception {
        como(max, plan("""
                {"dias": [5, 1, 3]}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias", contains(1, 3, 5)))
                .andExpect(jsonPath("$.vigenteDesde").value(hoy.toString()));

        como(max, plan("""
                {"dias": [1, 3, 5]}
                """)).andExpect(status().isOk());
        // Cambiarlo otra vez el mismo día reemplaza el de hoy (no crea historial ni choca con el UNIQUE).
        como(max, plan("""
                {"dias": [2, 4]}
                """)).andExpect(jsonPath("$.dias", contains(2, 4)));

        como(max, get(URL + "/plan")).andExpect(jsonPath("$.dias", contains(2, 4)));
        org.assertj.core.api.Assertions.assertThat(planRepository.count()).isEqualTo(1);
    }

    @Test
    void validaLosDiasDelPlan() throws Exception {
        como(max, plan("""
                {"dias": []}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("elige al menos un día")));
        como(max, plan("""
                {"dias": [0, 8]}
                """)).andExpect(status().isBadRequest());
        como(max, plan("""
                {"dias": [1, 1]}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("no puede repetir días")));
    }

    @Test
    void cuentaFallosYRachaConElPlanVigenteDeCadaDia() throws Exception {
        planDiarioDesde(hoy.minusDays(14));
        // Entrenó los últimos 3 días; los 11 anteriores del plan quedan fallados. Hoy está pendiente.
        entreno(max, hoy.minusDays(3));
        entreno(max, hoy.minusDays(2));
        entreno(max, hoy.minusDays(1));
        // Una sesión sin series (iniciada sin querer) no cuenta como día entrenado.
        sesionRepository.save(new SesionEntrenamiento(max, hoy.minusDays(5)));
        // Los entrenos de otro usuario no cuentan.
        entreno(otro, hoy.minusDays(6));

        como(max, get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumenTotal.entrenados").value(3))
                .andExpect(jsonPath("$.resumenTotal.fallados").value(11))
                .andExpect(jsonPath("$.resumenTotal.cumplimiento").value(21))
                .andExpect(jsonPath("$.rachaActual").value(3))
                .andExpect(jsonPath("$.mejorRacha").value(3))
                .andExpect(jsonPath("$.plan.dias", contains(1, 2, 3, 4, 5, 6, 7)))
                .andExpect(jsonPath("$.dias[%d].estado".formatted(hoy.getDayOfMonth() - 1)).value("PENDIENTE"));

        // Un mes anterior se consulta con ?mes=AAAA-MM.
        LocalDate haceDosSemanas = hoy.minusDays(14);
        como(max, get(URL).param("mes", YearMonth.from(haceDosSemanas).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias[%d].estado".formatted(haceDosSemanas.getDayOfMonth() - 1)).value("FALLADO"));
    }

    @Test
    void rechazaUnMesConFormatoInvalido() throws Exception {
        como(max, get(URL).param("mes", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("AAAA-MM")));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }
}
