package org.apartment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import java.util.UUID;
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

    String token = jwtService.generateToken(userDetails, UUID.randomUUID().toString());

    assertNotNull(token);
    assertTrue(jwtService.isTokenValid(token, userDetails));
  }

  @Test
  void testExtractUsername() {
    String expectedUsername = "test@gmail.com";
    when(userDetails.getUsername()).thenReturn(expectedUsername);
    String token = jwtService.generateToken(userDetails, UUID.randomUUID().toString());
    String username = jwtService.extractUsername(token);
    assertEquals(expectedUsername, username);
  }

  @Test
  void testInvalidTokenSignature() {
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String token = jwtService.generateToken(userDetails, UUID.randomUUID().toString());
    String invalidToken = token + "modified";
    assertThrows(SignatureException.class, () -> jwtService.extractUsername(invalidToken));
  }

  @Test
  void testExtractClaim() {
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String token = jwtService.generateToken(userDetails, UUID.randomUUID().toString());

    String username = jwtService.extractClaim(token, Claims::getSubject);
    assertNotNull(username);
    assertEquals("test@gmail.com", username);
  }

  @Test
  void testExtractUserId() {
    when(userDetails.getUsername()).thenReturn("test@gmail.com");
    String userId = UUID.randomUUID().toString();
    String token = jwtService.generateToken(userDetails, userId);

    String extractedUserId = jwtService.extractUserId(token);
    assertNotNull(userId);
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

    String expectedUserId = UUID.randomUUID().toString();

    String token = jwtService.generateToken(userDetails, expectedUserId);

    String actualUserId = jwtService.extractUserIdFromAuthorizationHeader("Bearer " + token);

    assertEquals(expectedUserId, actualUserId);
  }
}