package com.innowise.apigateway.exception;

public class DeactivatedUserException extends RuntimeException {

    public DeactivatedUserException() {
      super("User is deactivated");
    }

    public DeactivatedUserException(String message) {
        super(message);
    }

}
