package net.cycastic.sigil.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String message;
    private String path;
    private String stackTrace;
}
