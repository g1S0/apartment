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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
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
}