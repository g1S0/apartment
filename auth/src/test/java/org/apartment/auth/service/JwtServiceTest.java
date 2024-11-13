package org.apartment.auth.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import io.jsonwebtoken.security.SignatureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(jwtService, "secretKey", "7A5B713377684E693055426D673968734E2B573154424C646742734B6F755274");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 25000);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 1209600000);
    }

    @Test
    void testGenerateToken() {
        when(userDetails.getUsername()).thenReturn("test@gmail.com");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testExtractUsername() {
        String expectedUsername = "test@gmail.com";
        when(userDetails.getUsername()).thenReturn(expectedUsername);
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals(expectedUsername, username);
    }

    @Test
    void testInvalidTokenSignature() {
        when(userDetails.getUsername()).thenReturn("test@gmail.com");
        String token = jwtService.generateToken(userDetails);
        String invalidToken = token + "modified";
        assertThrows(SignatureException.class, () -> jwtService.extractUsername(invalidToken));
    }


    @Test
    void testExtractClaim() {
        when(userDetails.getUsername()).thenReturn("test@gmail.com");
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractClaim(token, Claims::getSubject);
        assertNotNull(username);
        assertEquals("test@gmail.com", username);
    }
}