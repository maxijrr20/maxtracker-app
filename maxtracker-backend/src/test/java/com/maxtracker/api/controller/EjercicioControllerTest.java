package com.maxtracker.api.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.exception.WgerApiException;
import com.maxtracker.api.security.ConSeguridadJwt;
import com.maxtracker.api.service.EjercicioService;

@WebMvcTest(EjercicioController.class)
@ConSeguridadJwt
@WithMockUser
class EjercicioControllerTest {

    private static final EjercicioDto PRESS_BANCA = new EjercicioDto(192, "Press de banca", "Empuja la barra", "es",
            11, "Pecho", List.of("Chest"), List.of("Triceps"), List.of("Barbell"), null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EjercicioService ejercicioService;

    @Test
    void listarUsaValoresPorDefectoYDevuelveLaPagina() throws Exception {
        given(ejercicioService.listar(0, 20, null))
                .willReturn(new PaginaDto<>(List.of(PRESS_BANCA), 0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/ejercicios"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contenido[0].id").value(192))
                .andExpect(jsonPath("$.contenido[0].nombre").value("Press de banca"))
                .andExpect(jsonPath("$.contenido[0].idioma").value("es"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(20))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void listarPasaPaginaTamanioYCategoriaAlServicio() throws Exception {
        given(ejercicioService.listar(2, 50, 11)).willReturn(new PaginaDto<>(List.of(), 2, 50, 0, 0));

        mockMvc.perform(get("/api/v1/ejercicios").param("pagina", "2").param("tamanio", "50").param("categoria", "11"))
                .andExpect(status().isOk());

        verify(ejercicioService).listar(2, 50, 11);
    }

    @Test
    void listarRechazaTamanioFueraDeRango() throws Exception {
        mockMvc.perform(get("/api/v1/ejercicios").param("tamanio", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Parámetros inválidos"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("tamanio: debe ser como máximo 100"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(ejercicioService);
    }

    @Test
    void listarRechazaPaginaNegativa() throws Exception {
        mockMvc.perform(get("/api/v1/ejercicios").param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("pagina: debe ser mayor o igual a 0"));
    }

    @Test
    void listarRechazaParametroNoNumerico() throws Exception {
        mockMvc.perform(get("/api/v1/ejercicios").param("pagina", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void listarResponde502SiWgerNoEstaDisponible() throws Exception {
        given(ejercicioService.listar(anyInt(), anyInt(), any()))
                .willThrow(new WgerApiException("No se pudo obtener el catálogo de ejercicios desde wger"));

        mockMvc.perform(get("/api/v1/ejercicios"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errores/wger-no-disponible"))
                .andExpect(jsonPath("$.title").value("Servicio externo no disponible"))
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void obtenerDevuelveElEjercicio() throws Exception {
        given(ejercicioService.obtenerPorId(192)).willReturn(PRESS_BANCA);

        mockMvc.perform(get("/api/v1/ejercicios/192"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Press de banca"))
                .andExpect(jsonPath("$.categoria").value("Pecho"));
    }

    @Test
    void obtenerResponde404SiNoExiste() throws Exception {
        given(ejercicioService.obtenerPorId(999)).willThrow(new EjercicioNoEncontradoException(999));

        mockMvc.perform(get("/api/v1/ejercicios/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("No existe un ejercicio con id 999"));
    }
}
