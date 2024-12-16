package org.apartment.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.ResponseDto;
import org.apartment.exception.FileValidationException;
import org.apartment.exception.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class RealEstateExceptionHandler extends GlobalExceptionHandler {
  @ExceptionHandler(FileValidationException.class)
  public ResponseEntity<ResponseDto<String>> handleFileValidationException(
      FileValidationException e) {
    ResponseDto<String> response = new ResponseDto<>("File validation failed: " + e.getMessage());
    log.info("File validation failed: {}", e.getMessage(), e);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
