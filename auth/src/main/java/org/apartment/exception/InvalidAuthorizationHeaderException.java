package org.apartment.exception;

public class InvalidAuthorizationHeaderException extends RuntimeException {
  public InvalidAuthorizationHeaderException(String message) {
    super(message);
  }
}
