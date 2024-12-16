package org.apartment.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public abstract class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationExceptions(
      MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    ResponseDto<Map<String, String>> response = new ResponseDto<>(errors);
    log.info("Validation: {}", e.getMessage(), e);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ResponseDto<String>> handleConstraintViolationException(
      ConstraintViolationException e) {
    ResponseDto<String> response = new ResponseDto<>("Constraint violation: " + e.getMessage());
    log.info("Constraint violation: {}", e.getMessage(), e);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseDto<String>> handleGenericException(Exception e) {
    ResponseDto<String> response =
        new ResponseDto<>("An unexpected error occurred: " + e.getMessage());
    log.error("Exception: {}", e.getMessage(), e);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ResponseDto<String>> handleIllegalStateException(IllegalStateException e) {
    ResponseDto<String> response = new ResponseDto<>("An error occurred: " + e.getMessage());
    log.error("File validation failed: {}", e.getMessage(), e);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
