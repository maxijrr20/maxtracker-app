package com.maxtracker.api.config;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.maxtracker.api.controller.EjercicioController;
import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.security.ConSeguridadJwt;
import com.maxtracker.api.service.EjercicioService;

@WebMvcTest(controllers = EjercicioController.class,
        properties = "app.cors.allowed-origins=http://localhost:5173, https://maxtracker.vercel.app")
@ConSeguridadJwt
@WithMockUser
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EjercicioService ejercicioService;

    @Test
    void preflightDesdeOrigenPermitidoEsAceptado() throws Exception {
        mockMvc.perform(options("/api/v1/ejercicios")
                        .header(HttpHeaders.ORIGIN, "https://maxtracker.vercel.app")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://maxtracker.vercel.app"));
    }

    @Test
    void peticionDesdeOrigenPermitidoIncluyeCabeceraCors() throws Exception {
        given(ejercicioService.listar(0, 20, null)).willReturn(new PaginaDto<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/v1/ejercicios").header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void preflightDesdeOrigenNoPermitidoEsRechazado() throws Exception {
        mockMvc.perform(options("/api/v1/ejercicios")
                        .header(HttpHeaders.ORIGIN, "https://sitio-malicioso.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
