package com.maxtracker.api.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Lee {@code Authorization: Bearer <jwt>} y, si el token es válido y el usuario sigue existiendo,
 * autentica la petición. Con un token ausente o inválido la petición sigue sin autenticar:
 * las rutas públicas responden normalmente y las protegidas devuelven 401 desde el entry point.
 *
 * <p>No es un {@code @Component} a propósito: lo instancia {@code SecurityConfig} para que Spring Boot
 * no lo registre además como filtro del contenedor de servlets.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            extraerToken(request)
                    .flatMap(jwtService::extraerEmail)
                    .flatMap(this::cargarUsuario)
                    .ifPresent(usuario -> autenticar(usuario, request));
        }
        filterChain.doFilter(request, response);
    }

    private static Optional<String> extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.regionMatches(true, 0, PREFIJO_BEARER, 0, PREFIJO_BEARER.length())) {
            return Optional.empty();
        }
        return Optional.of(cabecera.substring(PREFIJO_BEARER.length()).strip()).filter(token -> !token.isEmpty());
    }

    /** El usuario pudo haberse borrado después de emitir el token. */
    private Optional<UserDetails> cargarUsuario(String email) {
        try {
            return Optional.of(userDetailsService.loadUserByUsername(email));
        } catch (UsernameNotFoundException ex) {
            return Optional.empty();
        }
    }

    private static void autenticar(UserDetails usuario, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken autenticacion =
                UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());
        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
    }
}
