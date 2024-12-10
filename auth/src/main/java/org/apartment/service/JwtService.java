package org.apartment.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtService {
  @Value("${application.security.jwt.secret-key}")
  private String secretKey;
  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration;
  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshExpiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Long extractUserId(String token) {
    return extractClaim(token, claims -> {
      Object userId = claims.get("user_id");

      if (userId instanceof Integer) {
        return ((Integer) userId).longValue();
      } else if (userId instanceof Long) {
        return (Long) userId;
      } else {
        throw new IllegalArgumentException(
            "Invalid type for user_id: " + userId.getClass().getName());
      }
    });
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails, long userId) {
    return buildToken(new HashMap<>(), userDetails, jwtExpiration, userId);
  }

  public Long extractUserIdFromAuthorizationHeader(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid or missing Authorization header");
    }

    String token = authorizationHeader.substring(7);

    if (token.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is empty");
    }

    return extractUserId(token);
  }

  public String generateRefreshToken(UserDetails userDetails, long userId) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration, userId);
  }

  private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails,
                            long expiration, long userId) {
    extraClaims.put("user_id", userId);

    String randomValue = generateShortUuid();
    extraClaims.put("random_value", randomValue);

    return Jwts.builder().claims(extraClaims).subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration)).signWith(getSignInKey())
        .compact();
  }

  private String generateShortUuid() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}