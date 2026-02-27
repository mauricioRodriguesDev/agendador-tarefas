package com.javanauta.agendadortarefas.infrastructure.exceptions;

public class ResourceNotFundException extends RuntimeException {

    public ResourceNotFundException(String message) {

        super(message);
    }

    public ResourceNotFundException(String message, Throwable throwable) {

        super(message, throwable);
    }

}
