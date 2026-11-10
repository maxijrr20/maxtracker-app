package com.maxtracker.api.service.impl;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxtracker.api.config.AuthProperties;
import com.maxtracker.api.dto.auth.AuthResponse;
import com.maxtracker.api.dto.auth.LoginRequest;
import com.maxtracker.api.dto.auth.RegisterRequest;
import com.maxtracker.api.exception.EmailYaRegistradoException;
import com.maxtracker.api.exception.RegistroDeshabilitadoException;
import com.maxtracker.api.model.Usuario;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.security.JwtService;
import com.maxtracker.api.security.UsuarioAutenticado;
import com.maxtracker.api.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtAuthService implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        if (!authProperties.registroHabilitado()) {
            throw new RegistroDeshabilitadoException();
        }
        if (usuarioRepository.existeEmail(request.email())) {
            throw new EmailYaRegistradoException();
        }
        Usuario usuario;
        try {
            usuario = usuarioRepository.saveAndFlush(
                    new Usuario(request.email(), passwordEncoder.encode(request.password())));
        } catch (DataIntegrityViolationException ex) {
            // Dos registros simultáneos con el mismo email: la restricción UNIQUE decide.
            throw new EmailYaRegistradoException();
        }
        return emitirToken(UsuarioAutenticado.desde(usuario));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication autenticacion = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        return emitirToken((UsuarioAutenticado) autenticacion.getPrincipal());
    }

    private AuthResponse emitirToken(UsuarioAutenticado usuario) {
        return AuthResponse.bearer(jwtService.generarToken(usuario), usuario.email());
    }
}
