package com.innowise.authenticationservice.exception;

public class InvalidTokenTypeException extends RuntimeException {
    public InvalidTokenTypeException() {
        super("Invalid token type");
    }

    public InvalidTokenTypeException(String message) {
        super(message);
    }

    public InvalidTokenTypeException(String message, Throwable cause) {
    super(message, cause);
  }
}
