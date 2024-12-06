package org.apartment.controller.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.validation.ConstraintViolationException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import org.apartment.dto.ResponseDto;
import org.apartment.exception.InvalidAuthorizationHeaderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    ResponseDto<Map<String, String>> response = new ResponseDto<>(errors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseDto<String>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    ResponseDto<String> response = new ResponseDto<>("Constraint violation: " + ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseDto<String>> handleIllegalStateException(IllegalStateException ex) {
    ResponseDto<String> response = new ResponseDto<>(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidAuthorizationHeaderException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseDto<String>> handleInvalidAuthorizationHeaderException(
      InvalidAuthorizationHeaderException ex) {
    ResponseDto<String> response = new ResponseDto<>(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ResponseDto<String>> handleGenericException(Exception e) {
    ResponseDto<String> response =
        new ResponseDto<>("An unexpected error occurred: " + e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ResponseDto<String>> handleExpiredJwtException(ExpiredJwtException e) {
    ResponseDto<String> response = new ResponseDto<>("Token has expired");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(SignatureException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ResponseDto<String>> handleSignatureException(SignatureException e) {
    ResponseDto<String> response = new ResponseDto<>("Invalid token signature");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MalformedJwtException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseDto<String>> handleMalformedJwtException(MalformedJwtException e) {
    ResponseDto<String> response = new ResponseDto<>("Malformed token");
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnsupportedJwtException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseDto<String>> handleUnsupportedJwtException(
      UnsupportedJwtException e) {
    ResponseDto<String> response = new ResponseDto<>("Unsupported token");
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
    return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
  }
}