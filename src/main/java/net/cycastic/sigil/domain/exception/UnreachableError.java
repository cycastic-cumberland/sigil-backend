package net.cycastic.sigil.domain.exception;

public class UnreachableError extends Error {
    public UnreachableError(String message, Throwable throwable){
        super(message, throwable);
    }

    public UnreachableError(){
        this("Unreachable statement", null);
    }
}
