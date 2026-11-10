package com.maxtracker.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.maxtracker.api.config.AuthProperties;
import com.maxtracker.api.dto.auth.AuthResponse;
import com.maxtracker.api.dto.auth.LoginRequest;
import com.maxtracker.api.dto.auth.RegisterRequest;
import com.maxtracker.api.exception.EmailYaRegistradoException;
import com.maxtracker.api.exception.RegistroDeshabilitadoException;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.TokenJwt;
import com.maxtracker.api.security.UsuarioAutenticado;

class JwtAuthServiceTest {

    private static final RegisterRequest REGISTRO = new RegisterRequest("max@example.com", "secreta123");
    private static final TokenJwt TOKEN = new TokenJwt("jwt", Instant.parse("2026-11-11T00:00:00Z"));

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtService jwtService = mock(JwtService.class);

    private JwtAuthService servicio(boolean registroHabilitado) {
        return new JwtAuthService(usuarioRepository, passwordEncoder, authenticationManager, jwtService,
                new AuthProperties(registroHabilitado));
    }

    @Test
    void registrarGuardaElHashYNoLaPasswordEnClaro() {
        given(passwordEncoder.encode("secreta123")).willReturn("$2a$10$hash");
        given(usuarioRepository.saveAndFlush(any(Usuario.class))).willAnswer(invocacion -> invocacion.getArgument(0));
        given(jwtService.generarToken(any())).willReturn(TOKEN);

        AuthResponse respuesta = servicio(true).registrar(REGISTRO);

        assertThat(respuesta).isEqualTo(new AuthResponse("jwt", "Bearer", TOKEN.expiraEn(), "max@example.com"));
        verify(usuarioRepository).saveAndFlush(argThat(
                usuario -> usuario.getPasswordHash().equals("$2a$10$hash")));
    }

    @Test
    void registrarFallaSiElEmailYaExiste() {
        given(usuarioRepository.existeEmail("max@example.com")).willReturn(true);

        assertThatThrownBy(() -> servicio(true).registrar(REGISTRO)).isInstanceOf(EmailYaRegistradoException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrarTraduceLaViolacionDeUnicidadPorCarreraA409() {
        given(passwordEncoder.encode(any())).willReturn("$2a$10$hash");
        given(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .willThrow(new DataIntegrityViolationException("uk_usuario_email"));

        assertThatThrownBy(() -> servicio(true).registrar(REGISTRO)).isInstanceOf(EmailYaRegistradoException.class);
    }

    @Test
    void registrarFallaSiElRegistroEstaDeshabilitado() {
        assertThatThrownBy(() -> servicio(false).registrar(REGISTRO))
                .isInstanceOf(RegistroDeshabilitadoException.class);
        verifyNoInteractions(usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginAutenticaConElAuthenticationManagerYEmiteElToken() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(7L, "max@example.com", "$2a$10$hash");
        given(authenticationManager.authenticate(any())).willReturn(
                UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities()));
        given(jwtService.generarToken(usuario)).willReturn(TOKEN);

        AuthResponse respuesta = servicio(true).login(new LoginRequest("max@example.com", "secreta123"));

        assertThat(respuesta.token()).isEqualTo("jwt");
        assertThat(respuesta.email()).isEqualTo("max@example.com");
    }
}
