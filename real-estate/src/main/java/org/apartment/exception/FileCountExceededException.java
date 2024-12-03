package org.apartment.exception;

public class FileCountExceededException extends FileValidationException {
  public FileCountExceededException(String message) {
    super(message);
  }
}
