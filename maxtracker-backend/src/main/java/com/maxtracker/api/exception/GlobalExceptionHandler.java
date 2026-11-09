package com.maxtracker.api.exception;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a respuestas {@code application/problem+json} (RFC 9457)
 * con los campos {@code type}, {@code title}, {@code status}, {@code detail} y {@code timestamp}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WgerApiException.class)
    public ProblemDetail manejarWger(WgerApiException ex) {
        return problema(HttpStatus.BAD_GATEWAY, "Servicio externo no disponible",
                ex.getMessage() + ". Intenta nuevamente en unos minutos.", "wger-no-disponible");
    }

    @ExceptionHandler(EjercicioNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(EjercicioNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), "no-encontrado");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado.", "error-interno");
    }

    /** Errores de {@code @Min}, {@code @Max}, etc. en parámetros del controlador. */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalle = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(error -> resultado.getMethodParameter().getParameterName() + ": "
                                + error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        ProblemDetail body = problema(HttpStatus.BAD_REQUEST, "Parámetros inválidos", detalle, "validacion");
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** Agrega {@code timestamp} también a los errores que resuelve Spring (400, 404, 405...). */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> respuesta = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (respuesta != null && respuesta.getBody() instanceof ProblemDetail detalle
                && (detalle.getProperties() == null || !detalle.getProperties().containsKey("timestamp"))) {
            detalle.setProperty("timestamp", Instant.now());
        }
        return respuesta;
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle, String codigo) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setTitle(titulo);
        problema.setType(URI.create("/errores/" + codigo));
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }
}
