package net.cycastic.sigil.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ExceptionResponse;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(RequestException ex, HttpServletRequest request) {
        logger.error("Exception occurred while resolving HTTP request", ex);
        var currentDateTime = OffsetDateTime.now(ZoneId.of("UTC"));
        var errorBuilder = ExceptionResponse.builder()
                .timestamp(currentDateTime)
                .exceptionCode(ex.getExceptionCode())
                .status(ex.getResponseCode())
                .message(ex.getMessage())
                .path(request.getRequestURI());
        if (ex instanceof ValidationException validationException &&
                !validationException.getValidationMessages().isEmpty()){
            errorBuilder.validationMessages(validationException.getValidationMessages());
        }

        var error = errorBuilder.build();
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getResponseCode()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(404, ex, "Handler not found"), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(500, ex, "Internal server error"), request);
    }
}
