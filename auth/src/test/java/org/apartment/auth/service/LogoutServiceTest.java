package org.apartment.auth.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.apartment.auth.entity.Token;
import org.apartment.auth.exception.InvalidAuthorizationHeaderException;
import org.apartment.auth.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

class LogoutServiceTest {
  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private Authentication authentication;

  private LogoutService logoutService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    logoutService = new LogoutService(tokenRepository);
  }

  @Test
  void testLogout_Success() {
    String token = "valid-jwt-token";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);

    Token storedToken = new Token();
    storedToken.setToken(token);
    when(tokenRepository.findTokenByValue(token)).thenReturn(Optional.of(storedToken));

    logoutService.logout(request, response, authentication);

    verify(tokenRepository).save(storedToken);
  }

  @Test
  void testLogout_MissingAuthorizationHeader() {
    when(request.getHeader("Authorization")).thenReturn(null);

    assertThrows(InvalidAuthorizationHeaderException.class,
        () -> logoutService.logout(request, response, authentication));
  }

  @Test
  void testLogout_InvalidAuthorizationHeader() {
    when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

    assertThrows(InvalidAuthorizationHeaderException.class,
        () -> logoutService.logout(request, response, authentication));
  }

  @Test
  void testLogout_TokenNotFound() {
    String token = "valid-jwt-token";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);

    when(tokenRepository.findTokenByValue(token)).thenReturn(Optional.empty());

    logoutService.logout(request, response, authentication);

    verify(tokenRepository, never()).save(any());
  }
}