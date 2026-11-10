package com.maxtracker.api.exception;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
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

    /** Incluye {@link EjercicioNoEncontradoException}. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), "no-encontrado");
    }

    @ExceptionHandler(SesionFinalizadaException.class)
    public ProblemDetail manejarSesionFinalizada(SesionFinalizadaException ex) {
        return problema(HttpStatus.CONFLICT, "Sesión finalizada", ex.getMessage(), "sesion-finalizada");
    }

    /**
     * Una restricción de la BD rechazó el cambio (p. ej. dos series registradas a la vez con el mismo número).
     * No se expone el detalle SQL.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarConflictoDeDatos(DataIntegrityViolationException ex) {
        log.warn("Restricción de integridad violada: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflicto con los datos existentes",
                "La operación entra en conflicto con otro cambio. Recarga los datos e intenta nuevamente.",
                "conflicto");
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

    /**
     * Validación de método: {@code @Min}, {@code @Positive}, etc. en parámetros y, cuando el controlador tiene
     * restricciones en parámetros, también el {@code @Valid @RequestBody} (sus errores vienen como
     * {@link ParameterErrors} y se informan por campo del cuerpo, no por nombre del parámetro).
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        boolean errorEnCuerpo = ex.getParameterValidationResults().stream()
                .anyMatch(ParameterErrors.class::isInstance);
        String detalle = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado instanceof ParameterErrors errores
                        ? errores.getFieldErrors().stream().map(GlobalExceptionHandler::describir)
                        : resultado.getResolvableErrors().stream()
                                .map(error -> resultado.getMethodParameter().getParameterName() + ": "
                                        + error.getDefaultMessage()))
                .sorted()
                .collect(Collectors.joining("; "));
        ProblemDetail body = problema(HttpStatus.BAD_REQUEST,
                errorEnCuerpo ? "Datos inválidos" : "Parámetros inválidos", detalle, "validacion");
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** Errores de Bean Validation en un {@code @Valid @RequestBody}: "campo: mensaje", ordenados por campo. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::describir)
                .sorted()
                .collect(Collectors.joining("; "));
        ProblemDetail body = problema(HttpStatus.BAD_REQUEST, "Datos inválidos", detalle, "validacion");
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static String describir(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
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
