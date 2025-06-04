package net.cycastic.portfoliotoolkit.domain.exception;

import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public class RequestException extends RuntimeException {
    private final int responseCode;

    public RequestException(int responseCode, @Nullable String message){
        super(message);
        this.responseCode = responseCode;
    }
}
