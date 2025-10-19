package net.cycastic.sigil.domain.exception;

import lombok.Getter;

@Getter
public class ApiRequestException extends Exception{
    private final ExceptionResponse response;

    public ApiRequestException(ExceptionResponse response){
        this.response = response;
    }

    @Override
    public String getMessage(){
        return response.toString();
    }
}
