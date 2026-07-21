package com.innowise.userservice.exception;

public class CardLimitException extends RuntimeException {

  public CardLimitException() {
    super("User reached the limit of cards.");
  }

  public CardLimitException(String message) {
    super(message);
  }

  public CardLimitException(String message, Throwable cause) {
    super(message, cause);
  }

}
