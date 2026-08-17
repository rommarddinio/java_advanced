package com.innowise.orderservice.exception;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException() {
      super("Invalid status. Use NEW, PAID or CANCELLED.");
    }

    public InvalidOrderStatusException(String message) {
    super(message);
  }

    public InvalidOrderStatusException(String message, Throwable cause) {
    super(message, cause);
  }
}
