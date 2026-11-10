package com.maxtracker.api.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ProblemDetail manejarEmailYaRegistrado(EmailYaRegistradoException ex) {
        return problema(HttpStatus.CONFLICT, "Email ya registrado", ex.getMessage(), "email-ya-registrado");
    }

    @ExceptionHandler(RegistroDeshabilitadoException.class)
    public ProblemDetail manejarRegistroDeshabilitado(RegistroDeshabilitadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Registro deshabilitado", ex.getMessage(), "registro-deshabilitado");
    }

    /**
     * Credenciales incorrectas en el login, o petición sin un JWT válido (vía {@code SecurityProblemHandler}).
     * No distingue "usuario inexistente" de "contraseña incorrecta" para no revelar qué emails existen.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> manejarNoAutenticado(AuthenticationException ex) {
        ProblemDetail body = ex instanceof BadCredentialsException
                ? problema(HttpStatus.UNAUTHORIZED, "Credenciales inválidas",
                        "Email o contraseña incorrectos.", "credenciales-invalidas")
                : problema(HttpStatus.UNAUTHORIZED, "No autenticado",
                        "Se requiere un token JWT válido en la cabecera Authorization.", "no-autenticado");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail manejarAccesoDenegado(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permiso para acceder a este recurso.", "acceso-denegado");
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

    /** Errores de Bean Validation en un {@code @Valid @RequestBody}: "campo: mensaje", ordenados por campo. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparing(error -> String.valueOf(error.getDefaultMessage())))
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail body = problema(HttpStatus.BAD_REQUEST, "Datos inválidos", detalle, "validacion");
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
