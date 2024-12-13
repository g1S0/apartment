package org.apartment.controller.advice;

import org.apartment.exception.FileValidationException;
import org.apartment.exception.GlobalExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RealEstateExceptionHandler extends GlobalExceptionHandler {
  @ExceptionHandler(FileValidationException.class)
  public ResponseEntity<String> handleFileValidationException(FileValidationException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }
}
