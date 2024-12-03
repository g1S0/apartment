package org.apartment.exception;

public abstract class FileValidationException extends RuntimeException {
  public FileValidationException(String message) {
    super(message);
  }
}

