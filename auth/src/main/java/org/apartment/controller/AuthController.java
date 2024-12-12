package org.apartment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.AccessRefreshTokensDto;
import org.apartment.dto.LoginDto;
import org.apartment.dto.UserRegistrationDto;
import org.apartment.entity.User;
import org.apartment.mapper.RegisterMapper;
import org.apartment.service.AuthService;
import org.apartment.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  public ResponseEntity<AccessRefreshTokensDto> addNewUser(
      @RequestBody @Valid UserRegistrationDto registerRequest) {
    User user = RegisterMapper.INSTANCE.toEntity(registerRequest);

    return ResponseEntity.ok(service.register(user));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AccessRefreshTokensDto> authenticate(@RequestBody @Valid LoginDto request) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AccessRefreshTokensDto> refreshToken(HttpServletRequest request) {
    AccessRefreshTokensDto authResponse = service.refreshToken(request);

    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/validate-token")
  public ResponseEntity<String> validateToken(
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    String token = jwtService.extractUserIdFromAuthorizationHeader(authorizationHeader);

    return ResponseEntity.ok(token);
  }
}