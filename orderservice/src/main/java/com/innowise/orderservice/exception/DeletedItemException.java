package com.innowise.orderservice.exception;

public class DeletedItemException extends RuntimeException {
    public DeletedItemException(Long id) {
        super("Item id " + id + " is not available anymore.");
    }

    public DeletedItemException(String message) {
        super(message);
    }

    public DeletedItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
