package net.cycastic.portfoliotoolkit.domain.exception;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public class RequestException extends RuntimeException {
    private final int responseCode;

    public RequestException(int responseCode, @Nullable String message){
        super(message);
        this.responseCode = responseCode;
    }

    public RequestException(int responseCode, @NotNull String template, Object... args){
        this(responseCode, String.format(template, args));
    }
}
