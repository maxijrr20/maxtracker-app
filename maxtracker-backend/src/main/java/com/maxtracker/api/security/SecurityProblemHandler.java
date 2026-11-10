package com.maxtracker.api.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Los errores 401/403 de la cadena de filtros ocurren antes de llegar a un controlador; aquí se delegan
 * al {@link HandlerExceptionResolver} de Spring MVC para que los formatee el mismo
 * {@code GlobalExceptionHandler} que el resto de la API ({@code application/problem+json}).
 */
@Component
@RequiredArgsConstructor
public class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException ex) throws IOException {
        delegar(request, response, ex, HttpStatus.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException ex) throws IOException {
        delegar(request, response, ex, HttpStatus.FORBIDDEN);
    }

    private void delegar(HttpServletRequest request, HttpServletResponse response, Exception ex,
            HttpStatus respaldo) throws IOException {
        if (handlerExceptionResolver.resolveException(request, response, null, ex) == null) {
            response.sendError(respaldo.value());
        }
    }
}
