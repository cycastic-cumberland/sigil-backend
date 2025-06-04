package net.cycastic.portfoliotoolkit.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ExceptionResponse;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private final Environment environment;

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
    public ResponseEntity<ExceptionResponse> handleNotFound(RequestException ex, HttpServletRequest request) {
        logger.error("Exception occurred while resolving HTTP request", ex);
        var currentDateTime = OffsetDateTime.now(ZoneId.of("UTC"));
        var includeStackTrace = Arrays.asList(environment.getActiveProfiles()).contains("dev");
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
}
