package com.innowise.authenticationservice.exception;

public class EmptyTokenException extends RuntimeException {

    public EmptyTokenException() {
        super("Token is empty");
    }

    public EmptyTokenException(String message) {
        super(message);
    }

    public EmptyTokenException(String message, Throwable cause) {
        super(message, cause);
    }

}
