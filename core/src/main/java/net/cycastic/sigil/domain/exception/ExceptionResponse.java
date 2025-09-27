package net.cycastic.sigil.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String exceptionCode;
    private String message;
    private String path;
    private String stackTrace;
    private Map<String, List<String>> validationMessages;
}
