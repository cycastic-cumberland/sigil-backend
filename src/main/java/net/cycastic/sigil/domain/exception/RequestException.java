package net.cycastic.sigil.domain.exception;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public class RequestException extends RuntimeException {
    private final int responseCode;

    public RequestException(int responseCode, Throwable exception, @NotNull String message){
        super(message, exception);
        this.responseCode = responseCode;
    }

    public RequestException(int responseCode, @Nullable String message){
        this(responseCode, null, message);
    }

    public RequestException(int responseCode, @NotNull String template, Object... args){
        this(responseCode, String.format(template, args));
    }

    public RequestException(int responseCode, Throwable exception, @NotNull String template, Object... args){
        this(responseCode, exception, String.format(template, args));
    }
}
