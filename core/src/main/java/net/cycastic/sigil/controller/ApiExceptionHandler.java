package net.cycastic.sigil.controller;

import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.validation.JakartaValidationHelper;
import net.cycastic.sigil.configuration.application.ExceptionHandlerConfigurations;
import net.cycastic.sigil.domain.exception.ApiRequestException;
import net.cycastic.sigil.domain.exception.ExceptionResponse;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.exception.ValidationException;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.SignatureException;
import java.sql.DataTruncation;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
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

    public ExceptionResponse toExceptionResponse(RequestException ex, HttpServletRequest request) {
        var currentDateTime = OffsetDateTime.now(ZoneId.of("UTC"));
        var includeStackTrace = configurations.isShowStackTrace();
        var errorBuilder = ExceptionResponse.builder()
                .timestamp(currentDateTime)
                .exceptionCode(ex.getExceptionCode())
                .status(ex.getResponseCode())
                .message(ex.getMessage())
                .path(request.getRequestURI());
        if (includeStackTrace){
            errorBuilder.stackTrace(buildStackTrace(ex.getStackTrace()));
        }
        if (ex instanceof ValidationException validationException &&
                !validationException.getValidationMessages().isEmpty()){
            errorBuilder.validationMessages(validationException.getValidationMessages());
        }

        return errorBuilder.build();
    }

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(RequestException ex, HttpServletRequest request) {
        logger.error("Exception occurred while resolving HTTP request", ex);

        return new ResponseEntity<>(toExceptionResponse(ex, request), HttpStatus.valueOf(ex.getResponseCode()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(400, ex, ex.getMessage()), request);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(org.springframework.web.method.annotation.HandlerMethodValidationException ex, HttpServletRequest request) {
        var map = JakartaValidationHelper.toValidationMap(ex);
        return handleGeneric(new ValidationException(ex, map), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleJpaViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        var root = ex.getRootCause();
        if (root instanceof DataTruncation) {
            return handleGeneric(RequestException.withExceptionCode("C400T003", ex), request);
        }
        if (root instanceof SQLIntegrityConstraintViolationException){
            return handleGeneric(RequestException.withExceptionCode("C409T001", ex), request);
        }
        return handleGeneric(RequestException.withExceptionCode("C409T000", ex), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(404, ex, "Resource not found"), request);
    }

    @ExceptionHandler(UnavailableException.class)
    public ResponseEntity<ExceptionResponse> handleUnavailableException(UnavailableException ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(503, ex, "Service unavailable"), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var map = JakartaValidationHelper.toValidationMap(ex);
        return handleGeneric(new ValidationException(ex, map), request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ExceptionResponse> handleStaleObjectState(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        var root = ex.getRootCause();
        if (root instanceof StaleObjectStateException){
            return handleGeneric(new RequestException(429, ex, "Too many requests"), request);
        }

        return handleOtherExceptions(ex, request);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ExceptionResponse> handleSignatureException(SignatureException ex, HttpServletRequest request){
        return handleGeneric(RequestException.withExceptionCode("C403T002", ex), request);
    }

    @ExceptionHandler(ApiRequestException.class)
    public static ResponseEntity<ExceptionResponse> handleApiRequestException(ApiRequestException ex, HttpServletRequest request){
        logger.error("API exception encountered", ex);
        var error = ex.getResponse();
        return new ResponseEntity<>(error.toBuilder()
                .message(error.getMessage())
                .stackTrace(null)
                .validationMessages(null)
                .path(request.getRequestURI())
                .build(),
                HttpStatus.valueOf(error.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        return handleGeneric(new RequestException(500, ex, "Internal server error"), request);
    }
}
