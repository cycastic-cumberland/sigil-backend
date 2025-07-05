package net.cycastic.sigil.controller;

import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.ExceptionHandlerConfigurations;
import net.cycastic.sigil.domain.exception.ExceptionResponse;
import net.cycastic.sigil.domain.exception.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final Pattern DATA_TOO_LONG_PATTERN = Pattern.compile("data too long", Pattern.CASE_INSENSITIVE);
    private final ExceptionHandlerConfigurations configurations;

    private static String buildStackTrace(StackTraceElement[] elements){
        var sb = new StringBuilder();

        for (var element : elements){
            sb.append(element.getClassName());
            var fileName = element.getFileName();
            if (fileName == null || fileName.isEmpty()){
                continue;
            }
            sb.append('(').append(fileName);
            if (element.getLineNumber() > 0){
                sb.append(':').append(element.getLineNumber());
            }
            sb.append(")\n");
        }

        return sb.toString();
    }

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(RequestException ex, HttpServletRequest request) {
        logger.error("Exception occurred while resolving HTTP request", ex);
        var currentDateTime = OffsetDateTime.now(ZoneId.of("UTC"));
        var includeStackTrace = configurations.isShowStackTrace();
        var errorBuilder = ExceptionResponse.builder()
                .timestamp(currentDateTime)
                .status(ex.getResponseCode())
                .message(ex.getMessage())
                .path(request.getRequestURI());
        if (includeStackTrace){
            errorBuilder.stackTrace(buildStackTrace(ex.getStackTrace()));
        }
        var error = errorBuilder.build();
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getResponseCode()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleJpaViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        var root = ex.getRootCause();
        if (root != null && root.getMessage() != null && DATA_TOO_LONG_PATTERN.matcher(root.getMessage()).find()) {
            return handleGeneric(new RequestException(400, "Data is too long"), request);
        } else {
            return handleGeneric(new RequestException(409, "Data conflict detected"), request);
        }
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(404, ex, "Resource not found"), request);
    }

    @ExceptionHandler(UnavailableException.class)
    public ResponseEntity<ExceptionResponse> handleUnavailableException(UnavailableException ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(503, ex, "Service unavailable"), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(500, ex, "Internal server error"), request);
    }
}
