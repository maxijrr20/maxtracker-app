package com.maxtracker.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.maxtracker.api.config.WgerProperties;
import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.dto.PaginaDto;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.exception.WgerApiException;
import com.maxtracker.api.mapper.WgerEjercicioMapper;

/** Prueba el cliente HTTP de wger contra un servidor simulado (sin acceso a internet). */
class WgerExerciseServiceTest {

    private static final String BASE_URL = "https://wger.de/api/v2/";

    private static final String EJERCICIO_JSON = """
            {
              "id": 192,
              "uuid": "c2078aac-e4e2-4103-a845-6252a3eb795e",
              "category": {"id": 11, "name": "Chest"},
              "muscles": [{"id": 4, "name": "Pectoralis major", "name_en": "Chest", "is_front": true}],
              "muscles_secondary": [{"id": 5, "name": "Triceps brachii", "name_en": "Triceps", "is_front": false}],
              "equipment": [{"id": 1, "name": "Barbell"}],
              "translations": [
                {"id": 1, "name": "Bench Press", "description": "<p>Push</p>", "language": 2},
                {"id": 2, "name": "Press de banca", "description": "<p>Empuja</p>", "language": 4}
              ],
              "images": [{"id": 1, "image": "https://wger.de/media/banca.png", "is_main": true}],
              "variations": null,
              "license_author": "wger"
            }
            """;

    private MockRestServiceServer server;
    private WgerExerciseService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        WgerProperties properties = new WgerProperties(BASE_URL, 4, 2, Duration.ofSeconds(5), Duration.ofSeconds(10));
        service = new WgerExerciseService(builder.build(), new WgerEjercicioMapper(properties));
    }

    @Test
    void listarConsultaExerciseinfoConLimitYOffsetYMapeaLaPagina() {
        server.expect(requestTo(BASE_URL + "exerciseinfo/?limit=20&offset=40"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"count": 45, "next": null, "previous": null, "results": [%s]}
                        """.formatted(EJERCICIO_JSON), MediaType.APPLICATION_JSON));

        PaginaDto<EjercicioDto> pagina = service.listar(2, 20, null);

        server.verify();
        assertThat(pagina.pagina()).isEqualTo(2);
        assertThat(pagina.tamanio()).isEqualTo(20);
        assertThat(pagina.totalElementos()).isEqualTo(45);
        assertThat(pagina.totalPaginas()).isEqualTo(3);
        assertThat(pagina.contenido()).singleElement().satisfies(e -> {
            assertThat(e.nombre()).isEqualTo("Press de banca");
            assertThat(e.idioma()).isEqualTo("es");
            assertThat(e.categoria()).isEqualTo("Pecho");
            assertThat(e.musculosPrincipales()).containsExactly("Chest");
        });
    }

    @Test
    void listarEnviaLaCategoriaCuandoSeIndica() {
        server.expect(requestTo(BASE_URL + "exerciseinfo/?limit=10&offset=0&category=11"))
                .andRespond(withSuccess("""
                        {"count": 0, "next": null, "previous": null, "results": []}
                        """, MediaType.APPLICATION_JSON));

        PaginaDto<EjercicioDto> pagina = service.listar(0, 10, 11);

        server.verify();
        assertThat(pagina.contenido()).isEmpty();
        assertThat(pagina.totalPaginas()).isZero();
    }

    @Test
    void listarLanzaWgerApiExceptionSiWgerFalla() {
        server.expect(requestTo(BASE_URL + "exerciseinfo/?limit=20&offset=0")).andRespond(withServerError());

        assertThatThrownBy(() -> service.listar(0, 20, null))
                .isInstanceOf(WgerApiException.class)
                .hasMessageContaining("catálogo");
    }

    @Test
    void obtenerPorIdDevuelveElEjercicio() {
        server.expect(requestTo(BASE_URL + "exerciseinfo/192/"))
                .andRespond(withSuccess(EJERCICIO_JSON, MediaType.APPLICATION_JSON));

        EjercicioDto dto = service.obtenerPorId(192);

        server.verify();
        assertThat(dto.id()).isEqualTo(192);
        assertThat(dto.nombre()).isEqualTo("Press de banca");
        assertThat(dto.imagenUrl()).isEqualTo("https://wger.de/media/banca.png");
    }

    @Test
    void obtenerPorIdLanzaNoEncontradoSiWgerResponde404() {
        server.expect(requestTo(BASE_URL + "exerciseinfo/999999/")).andRespond(withResourceNotFound());

        assertThatThrownBy(() -> service.obtenerPorId(999999))
                .isInstanceOf(EjercicioNoEncontradoException.class)
                .hasMessageContaining("999999");
    }

    @Test
    void obtenerPorIdLanzaWgerApiExceptionSiWgerFalla() {
        server.expect(requestTo(BASE_URL + "exerciseinfo/192/")).andRespond(withServerError());

        assertThatThrownBy(() -> service.obtenerPorId(192)).isInstanceOf(WgerApiException.class);
    }
}
