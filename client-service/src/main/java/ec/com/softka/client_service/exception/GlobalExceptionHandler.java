package ec.com.softka.client_service.exception;


import ec.com.softka.client_service.exception.dto.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ========= Helpers ========= */

    private ResponseEntity<ErrorMessage> build(HttpStatus status, String msg, String path,
                                               Map<String, String> details) {
        ErrorMessage body = ErrorMessage.builder()
                .message(msg)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .path(path)
                .details(details == null || details.isEmpty() ? null : details)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, String> fieldErrorsOf(BindException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        return details;
    }

    /* ========= Validación / Binding ========= */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                     HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", req.getRequestURI(), fieldErrorsOf(ex));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorMessage> handleBind(BindException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Error de enlace de parámetros", req.getRequestURI(), fieldErrorsOf(ex));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolation(ConstraintViolationException ex,
                                                                  HttpServletRequest req) {
        Map<String, String> details = new HashMap<>();
        Set<ConstraintViolation<?>> v = ex.getConstraintViolations();
        for (ConstraintViolation<?> c : v) {
            String field = c.getPropertyPath() != null ? c.getPropertyPath().toString() : "value";
            details.put(field, c.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", req.getRequestURI(), details);
    }

    /* ========= JSON / Conversión / Tipos ========= */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.debug("JSON parse error", ex);
        return build(HttpStatus.BAD_REQUEST, "Cuerpo de la petición inválido o malformado", req.getRequestURI(), null);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConversionFailedException.class,
            HttpMessageConversionException.class})
    public ResponseEntity<ErrorMessage> handleTypeMismatch(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Tipo de dato inválido en parámetros", req.getRequestURI(), null);
    }

    /* ========= Parámetros / Rutas / Métodos ========= */

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorMessage> handleMissingParam(MissingServletRequestParameterException ex,
                                                           HttpServletRequest req) {
        Map<String, String> d = Map.of(ex.getParameterName(), "Parámetro requerido");
        return build(HttpStatus.BAD_REQUEST, "Faltan parámetros requeridos", req.getRequestURI(), d);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorMessage> handleMissingPath(MissingPathVariableException ex, HttpServletRequest req) {
        Map<String, String> d = Map.of(ex.getVariableName(), "Path variable requerida");
        return build(HttpStatus.BAD_REQUEST, "Variable de ruta faltante", req.getRequestURI(), d);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorMessage> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest req) {
        String method = ex.getMethod();
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método no soportado: " + method, req.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorMessage> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest req) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type no soportado", req.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorMessage> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.NOT_ACCEPTABLE, "Formato de respuesta no aceptable", req.getRequestURI(), null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorMessage> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Ruta no encontrada", req.getRequestURI(), null);
    }

    /* ========= Dominio / Persistencia ========= */

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleClientNotFoundException(ClientNotFoundException ex,
                                                                      HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> handleDataIntegrity(DataIntegrityViolationException ex,
                                                            HttpServletRequest req) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "Violación de integridad de datos (unicidad, FK, etc.)", req.getRequestURI(),
                null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI(), null);
    }

    /* ========= Seguridad ========= */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "No tienes permisos para esta operación", req.getRequestURI(), null);
    }

    /* ========= ResponseStatusException (útil si la lanzas en servicios/controladores) ========= */

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorMessage> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatusCode() instanceof HttpStatus http ? http : HttpStatus.BAD_REQUEST;
        return build(status, ex.getReason() != null ? ex.getReason() : "Error", req.getRequestURI(), null);
    }

    /* ========= Fallback general ========= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado", req.getRequestURI(), null);
    }
}