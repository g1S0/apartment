package org.apartment.exception;

public class InvalidFileExtensionException extends FileValidationException {
  public InvalidFileExtensionException(String message) {
    super(message);
  }
}
