package net.cycastic.sigil.domain.exception;

public class ForbiddenException extends RequestException {
    public ForbiddenException(String message){
        super(403, message);
    }
    public ForbiddenException() {
        this("Not enough permission to perform this action");
    }
}
