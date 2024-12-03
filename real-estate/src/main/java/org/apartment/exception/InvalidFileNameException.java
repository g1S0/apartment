package org.apartment.exception;

public class InvalidFileNameException extends FileValidationException {
  public InvalidFileNameException(String message) {
    super(message);
  }
}
