package org.apartment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
  @InjectMocks
  private JwtService jwtService;

  @Mock
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    ReflectionTestUtils.setField(jwtService, "secretKey",
        "7A5B713377684E693055426D673968734E2B573154424C646742734B6F755274");
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 25000);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", 1209600000);
  }

  @Test
  void testGenerateToken() {
    when(userDetails.getUsername()).thenReturn("test@gmail.com");

    String token = jwtService.generateToken(userDetails, 1L);

    assertNotNull(token);
    assertTrue(jwtService.isTokenValid(token, userDetails));
  }

  @Test
  void testExtractUsername() {
    String expectedUsername = "test@gmail.com";
    when(userDetails.getUsername()).thenReturn(expectedUsername);
    String token = jwtService.generateToken(userDetails, 1L);
    String username = jwtService.extractUsername(token);
    assertEquals(expectedUsername, username);
  }

  @Test
  void testInvalidTokenSignature() {
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String token = jwtService.generateToken(userDetails, 1L);
    String invalidToken = token + "modified";
    assertThrows(SignatureException.class, () -> jwtService.extractUsername(invalidToken));
  }

  @Test
  void testExtractClaim() {
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String token = jwtService.generateToken(userDetails, 1L);

    String username = jwtService.extractClaim(token, Claims::getSubject);
    assertNotNull(username);
    assertEquals("test@gmail.com", username);
  }

  @Test
  void testExtractUserId() {
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String token = jwtService.generateToken(userDetails, 123L);

    Long userId = jwtService.extractUserId(token);
    assertNotNull(userId);
    assertEquals(123, userId);
  }

  @Test
  public void testUserIdWithIntegerValue() {
    long userId = Integer.MAX_VALUE;
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String token = jwtService.generateToken(userDetails, userId);

    Long extractedUserId = jwtService.extractUserId(token);
    assertEquals(userId, extractedUserId);
  }

  @Test
  public void testUserIdWithLongValue() {
    long userId = Long.MAX_VALUE;
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String token = jwtService.generateToken(userDetails, userId);

    Long extractedUserId = jwtService.extractUserId(token);
    assertEquals(userId, extractedUserId);
  }

  @Test
  void testExtractUserIdFromAuthorizationHeader_MissingHeader() {
    assertThrows(ResponseStatusException.class,
        () -> jwtService.extractUserIdFromAuthorizationHeader(null));
  }

  @Test
  void testExtractUserIdFromAuthorizationHeader_InvalidHeader() {
    assertThrows(ResponseStatusException.class,
        () -> jwtService.extractUserIdFromAuthorizationHeader("InvalidToken"));
  }

  @Test
  void testExtractUserIdFromAuthorizationHeader_EmptyToken() {
    assertThrows(ResponseStatusException.class,
        () -> jwtService.extractUserIdFromAuthorizationHeader("Bearer "));
  }

  @Test
  void testGenerateTokenAndExtractUserIdFromAuthorizationHeader() {
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("testUser");

    long expectedUserId = 123L;

    String token = jwtService.generateToken(userDetails, expectedUserId);

    Long actualUserId = jwtService.extractUserIdFromAuthorizationHeader("Bearer " + token);

    assertEquals(expectedUserId, actualUserId);
  }
}