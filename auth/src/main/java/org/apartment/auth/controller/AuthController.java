package org.apartment.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apartment.auth.dto.AuthenticationRequestDto;
import org.apartment.auth.dto.AuthenticationResponseDto;
import org.apartment.auth.dto.RegisterRequestDto;
import org.apartment.auth.entity.User;
import org.apartment.auth.mapper.RegisterMapper;
import org.apartment.auth.service.AuthService;
import org.apartment.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

  private final AuthService service;
  private final JwtService jwtService;

  public AuthController(AuthService service, JwtService jwtService) {
    this.service = service;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponseDto> addNewUser(
      @RequestBody @Valid RegisterRequestDto registerRequest) {
    User user = RegisterMapper.INSTANCE.toEntity(registerRequest);

    return ResponseEntity.ok(service.register(user));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponseDto> authenticate(
      @RequestBody @Valid AuthenticationRequestDto request) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AuthenticationResponseDto> refreshToken(HttpServletRequest request) {
    AuthenticationResponseDto authResponse = service.refreshToken(request);

    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/validate-token")
  public ResponseEntity<Integer> validateToken(
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid or missing Authorization header");
    }

    String token = authorizationHeader.substring(7);

    if (token.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is empty");
    }

    try {
      Integer userId = jwtService.extractUserId(token);
      return ResponseEntity.ok(userId);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", e);
    }
  }
}