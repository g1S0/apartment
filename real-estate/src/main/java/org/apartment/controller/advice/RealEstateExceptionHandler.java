package org.apartment.controller.advice;

import org.apartment.dto.ResponseDto;
import org.apartment.exception.FileValidationException;
import org.apartment.exception.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RealEstateExceptionHandler extends GlobalExceptionHandler {
  @ExceptionHandler(FileValidationException.class)
  public ResponseEntity<ResponseDto<String>> handleFileValidationException(FileValidationException ex) {
    ResponseDto<String> response = new ResponseDto<>("File validation failed: " + ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
