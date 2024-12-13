package org.apartment.controller.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.SignatureException;
import org.apartment.dto.ResponseDto;
import org.apartment.exception.GlobalExceptionHandler;
import org.apartment.exception.InvalidAuthorizationHeaderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandler extends GlobalExceptionHandler {
  @ExceptionHandler(InvalidAuthorizationHeaderException.class)
  public ResponseEntity<ResponseDto<String>> handleInvalidAuthorizationHeaderException(
      InvalidAuthorizationHeaderException ex) {
    ResponseDto<String> response = new ResponseDto<>(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ResponseDto<String>> handleExpiredJwtException(ExpiredJwtException e) {
    ResponseDto<String> response = new ResponseDto<>("Token has expired");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(SignatureException.class)
  public ResponseEntity<ResponseDto<String>> handleSignatureException(SignatureException e) {
    ResponseDto<String> response = new ResponseDto<>("Invalid token signature");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MalformedJwtException.class)
  public ResponseEntity<ResponseDto<String>> handleMalformedJwtException(MalformedJwtException e) {
    ResponseDto<String> response = new ResponseDto<>("Malformed token");
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnsupportedJwtException.class)
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
