package org.apartment.controller.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.ResponseDto;
import org.apartment.exception.GlobalExceptionHandler;
import org.apartment.exception.InvalidAuthorizationHeaderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@Slf4j
public class AuthExceptionHandler extends GlobalExceptionHandler {
  @ExceptionHandler(InvalidAuthorizationHeaderException.class)
  public ResponseEntity<ResponseDto<String>> handleInvalidAuthorizationHeaderException(
      InvalidAuthorizationHeaderException e) {
    log.info("Handling InvalidAuthorizationHeaderException: {}", e.getMessage());
    ResponseDto<String> response = new ResponseDto<>(e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ResponseDto<String>> handleExpiredJwtException() {
    log.info("Handling ExpiredJwtException: token has expired");
    ResponseDto<String> response = new ResponseDto<>("Token has expired");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(SignatureException.class)
  public ResponseEntity<ResponseDto<String>> handleSignatureException() {
    log.info("Handling SignatureException: invalid token signature");
    ResponseDto<String> response = new ResponseDto<>("Invalid token signature");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MalformedJwtException.class)
  public ResponseEntity<ResponseDto<String>> handleMalformedJwtException() {
    log.info("Handling MalformedJwtException: Malformed token");
    ResponseDto<String> response = new ResponseDto<>("Malformed token");
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnsupportedJwtException.class)
  public ResponseEntity<ResponseDto<String>> handleUnsupportedJwtException() {
    log.info("Handling UnsupportedJwtException: unsupported token");
    ResponseDto<String> response = new ResponseDto<>("Unsupported token");
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<String> handleBadCredentialsException() {
    log.info("Handling BadCredentialsException: invalid user data");
    return new ResponseEntity<>("Invalid user data", HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ResponseDto<String>> handleResponseStatusException(
      ResponseStatusException e) {
    log.info("Handling ResponseStatusException: {}", e.getReason());
    ResponseDto<String> response = new ResponseDto<>(e.getReason());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }
}
