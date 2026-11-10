package com.maxtracker.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import com.maxtracker.api.config.JwtConfig;
import com.maxtracker.api.config.JwtProperties;

import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-solo-para-tests-maxtracker-0123456789";
    private static final Instant AHORA = Instant.parse("2026-11-10T12:00:00Z");
    private static final JwtProperties PROPIEDADES = new JwtProperties(SECRETO, Duration.ofHours(12), "maxtracker-api");
    private static final UsuarioAutenticado USUARIO = new UsuarioAutenticado(1L, "max@example.com", "$2a$10$hash");

    private final SecretKey clave = new JwtConfig().jwtSigningKey(PROPIEDADES);

    private JwtService servicioEn(Instant instante) {
        return servicio(clave, PROPIEDADES, instante);
    }

    private static JwtService servicio(SecretKey clave, JwtProperties propiedades, Instant instante) {
        return new JwtService(clave, propiedades, Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void generaUnTokenQueSeValidaYDevuelveElEmail() {
        TokenJwt token = servicioEn(AHORA).generarToken(USUARIO);

        assertThat(token.expiraEn()).isEqualTo(AHORA.plus(Duration.ofHours(12)));
        assertThat(token.valor().split("\\.")).hasSize(3);
        assertThat(servicioEn(AHORA.plusSeconds(60)).extraerEmail(token.valor())).contains("max@example.com");
    }

    @Test
    void rechazaUnTokenVencido() {
        String token = servicioEn(AHORA).generarToken(USUARIO).valor();

        assertThat(servicioEn(AHORA.plus(Duration.ofHours(12)).plusSeconds(1)).extraerEmail(token)).isEmpty();
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        SecretKey otraClave = Keys.hmacShaKeyFor(
                "otra-clave-distinta-de-al-menos-32-bytes-xxxx".getBytes(StandardCharsets.UTF_8));
        String token = servicio(otraClave, PROPIEDADES, AHORA).generarToken(USUARIO).valor();

        assertThat(servicioEn(AHORA).extraerEmail(token)).isEmpty();
    }

    @Test
    void rechazaUnTokenDeOtroEmisor() {
        JwtProperties otroEmisor = new JwtProperties(SECRETO, Duration.ofHours(12), "otra-api");
        String token = servicio(clave, otroEmisor, AHORA).generarToken(USUARIO).valor();

        assertThat(servicioEn(AHORA).extraerEmail(token)).isEmpty();
    }

    @Test
    void rechazaUnTokenAlterado() {
        String token = servicioEn(AHORA).generarToken(USUARIO).valor();
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(servicioEn(AHORA).extraerEmail(alterado)).isEmpty();
    }

    @Test
    void rechazaTextoQueNoEsUnJwt() {
        assertThat(servicioEn(AHORA).extraerEmail("no-es-un-jwt")).isEmpty();
        assertThat(servicioEn(AHORA).extraerEmail("")).isEmpty();
    }

    @Test
    void laConfiguracionRechazaUnSecretoCorto() {
        JwtProperties debil = new JwtProperties("corto", Duration.ofHours(1), "maxtracker-api");

        assertThatThrownBy(() -> new JwtConfig().jwtSigningKey(debil))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 bytes");
    }
}
