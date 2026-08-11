package com.innowise.orderservice.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super("Item is not found.");
    }

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
