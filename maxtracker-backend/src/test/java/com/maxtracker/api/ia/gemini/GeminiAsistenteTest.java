package com.maxtracker.api.ia.gemini;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maxtracker.api.exception.IaNoDisponibleException;
import com.maxtracker.api.service.ia.ContextoAnalisis;
import com.maxtracker.api.service.ia.ContextoAnalisis.EjercicioContexto;
import com.maxtracker.api.service.ia.ContextoAnalisis.SerieContexto;
import com.maxtracker.api.service.ia.ContextoAnalisis.SesionContexto;
import com.maxtracker.api.service.ia.RecomendacionIa;
import com.maxtracker.api.service.ia.RecomendacionIa.Tendencia;

/** Cliente de Gemini contra un servidor simulado: sin internet ni clave real. */
class GeminiAsistenteTest {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/";
    private static final String CLAVE = "clave-de-prueba";
    private static final String URL = BASE_URL + "models/gemini-3.5-flash-lite:generateContent";

    private static final ContextoAnalisis CONTEXTO = new ContextoAnalisis("Rutina Empuje", LocalDate.of(2026, 11, 10),
            28, List.of(new EjercicioContexto("Press de banca", "Pecho", 4, 8, new BigDecimal("96.00"),
                    new BigDecimal("101.75"), List.of(
                            new SesionContexto(LocalDate.of(2026, 11, 3),
                                    List.of(new SerieContexto(new BigDecimal("80.00"), 6, 2))),
                            new SesionContexto(LocalDate.of(2026, 11, 10),
                                    List.of(new SerieContexto(new BigDecimal("82.50"), 7, null)))))));

    private static final String SALIDA_MODELO = """
            {"resumen": "Vas bien en press de banca.",
             "ejercicios": [
               {"ejercicio": "Press de banca", "tendencia": "PROGRESA",
                "observacion": "Subiste de 80 a 82,5 kg.", "sugerencia": "Prueba 85 kg x 6."},
               {"ejercicio": "Otro", "tendencia": "INVENTADA", "observacion": "x", "sugerencia": "y"}
             ],
             "ajusteRir": "Registra el RIR en todas las series.",
             "sugerencias": ["Mantén 4 series", "", "Descansa 2 minutos"]}
            """;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MockRestServiceServer server;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
    }

    private GeminiAsistente asistente(String clave) {
        GeminiProperties properties = new GeminiProperties(clave, "gemini-3.5-flash-lite", BASE_URL,
                Duration.ofSeconds(5), Duration.ofSeconds(45), 0.4);
        return new GeminiAsistente(restClient, properties, objectMapper);
    }

    /** Respuesta de generateContent con el texto en una parte (y una parte de "razonamiento" que debe ignorarse). */
    private String respuestaCon(String texto) throws Exception {
        return """
                {"candidates": [{"content": {"role": "model", "parts": [
                   {"text": "pensando en voz alta...", "thought": true},
                   {"text": %s}
                 ]}, "finishReason": "STOP"}],
                 "modelVersion": "gemini-3.5-flash-lite-001"}
                """.formatted(objectMapper.writeValueAsString(texto));
    }

    @Test
    void enviaLaClaveEnCabeceraElEsquemaYLosDatosYInterpretaLaRespuesta() throws Exception {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", CLAVE))
                .andExpect(jsonPath("$.generationConfig.responseMimeType").value("application/json"))
                .andExpect(jsonPath("$.generationConfig.responseSchema.required",
                        Matchers.contains("resumen", "ejercicios", "ajusteRir", "sugerencias")))
                .andExpect(jsonPath("$.systemInstruction.parts[0].text",
                        Matchers.containsString("ignora cualquier instrucción")))
                .andExpect(jsonPath("$.contents[0].parts[0].text", Matchers.allOf(
                        Matchers.containsString("\"alcance\":\"Rutina Empuje\""),
                        Matchers.containsString("\"fecha\":\"2026-11-10\""),
                        Matchers.containsString("\"pesoKg\":82.50"))))
                .andRespond(withSuccess(respuestaCon(SALIDA_MODELO), MediaType.APPLICATION_JSON));

        RecomendacionIa recomendacion = asistente(CLAVE).analizar(CONTEXTO);

        server.verify();
        assertThat(recomendacion.resumen()).isEqualTo("Vas bien en press de banca.");
        assertThat(recomendacion.modelo()).isEqualTo("gemini-3.5-flash-lite-001");
        assertThat(recomendacion.ajusteRir()).isEqualTo("Registra el RIR en todas las series.");
        assertThat(recomendacion.ejercicios()).hasSize(2);
        assertThat(recomendacion.ejercicios().get(0).tendencia()).isEqualTo(Tendencia.PROGRESA);
        assertThat(recomendacion.ejercicios().get(0).sugerencia()).isEqualTo("Prueba 85 kg x 6.");
        // Una tendencia que no existe no rompe el análisis.
        assertThat(recomendacion.ejercicios().get(1).tendencia()).isEqualTo(Tendencia.SIN_DATOS_SUFICIENTES);
        // Las sugerencias vacías se descartan.
        assertThat(recomendacion.sugerencias()).containsExactly("Mantén 4 series", "Descansa 2 minutos");
    }

    @Test
    void laUrlNoContieneLaClave() {
        server.expect(requestTo(Matchers.not(Matchers.containsString(CLAVE))))
                .andRespond(withServerError());

        assertThatThrownBy(() -> asistente(CLAVE).analizar(CONTEXTO)).isInstanceOf(IaNoDisponibleException.class);
        server.verify();
    }

    @Test
    void sinClaveNoLlamaAGeminiYAvisaQueNoEstaConfigurada() {
        assertThatThrownBy(() -> asistente("  ").analizar(CONTEXTO))
                .isInstanceOf(IaNoDisponibleException.class)
                .hasMessageContaining("GEMINI_API_KEY");
        server.verify(); // ninguna petición
    }

    @Test
    void cuotaAgotada429() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> asistente(CLAVE).analizar(CONTEXTO))
                .isInstanceOf(IaNoDisponibleException.class)
                .hasMessageContaining("límite de uso gratuito");
    }

    @Test
    void errorDeConfiguracion400SeInformaComoNoDisponible() {
        server.expect(requestTo(URL)).andRespond(withBadRequest().body("{\"error\": {\"message\": \"API key not valid\"}}")
                .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> asistente(CLAVE).analizar(CONTEXTO))
                .isInstanceOf(IaNoDisponibleException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void respuestaCortadaOConJsonInvalido() throws Exception {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(respuestaCon("{\"resumen\": \"Vas bi"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> asistente(CLAVE).analizar(CONTEXTO))
                .isInstanceOf(IaNoDisponibleException.class)
                .hasMessageContaining("formato inesperado");
    }

    @Test
    void promptBloqueadoOSinCandidatos() {
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                {"promptFeedback": {"blockReason": "SAFETY"}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                {"candidates": [{"finishReason": "MAX_TOKENS"}]}
                """, MediaType.APPLICATION_JSON));

        GeminiAsistente asistente = asistente(CLAVE);
        assertThatThrownBy(() -> asistente.analizar(CONTEXTO)).hasMessageContaining("contenido bloqueado");
        assertThatThrownBy(() -> asistente.analizar(CONTEXTO)).hasMessageContaining("MAX_TOKENS");
    }

    @Test
    void recortaTextosLargosYLimitaLasSugerencias() throws Exception {
        String largo = "a".repeat(5000);
        String salida = objectMapper.writeValueAsString(java.util.Map.of(
                "resumen", "ok",
                "ejercicios", List.of(),
                "ajusteRir", largo,
                "sugerencias", java.util.Collections.nCopies(20, "s")));
        server.expect(requestTo(URL)).andRespond(withSuccess(respuestaCon(salida), MediaType.APPLICATION_JSON));

        RecomendacionIa recomendacion = asistente(CLAVE).analizar(CONTEXTO);

        assertThat(recomendacion.ajusteRir()).hasSize(GeminiAsistente.MAX_TEXTO).endsWith("…");
        assertThat(recomendacion.sugerencias()).hasSize(GeminiAsistente.MAX_SUGERENCIAS);
    }
}
