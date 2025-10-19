package net.cycastic.sigil.domain.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ValidationException extends RequestException {
    private final Map<String, List<String>> validationMessages;

    public ValidationException(Throwable throwable, Map<String, List<String>> validationMessages){
        super(400, "C400T014", throwable, "Validation failed");
        this.validationMessages = validationMessages;
    }

    public ValidationException(Map<String, List<String>> validationMessages){
        this(null, validationMessages);
    }
}
