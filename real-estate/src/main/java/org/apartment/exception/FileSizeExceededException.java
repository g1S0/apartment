package org.apartment.exception;

public class FileSizeExceededException extends FileValidationException {
  public FileSizeExceededException(String message) {
    super(message);
  }
}
