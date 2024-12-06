package org.apartment.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.AuthenticationRequestDto;
import org.apartment.dto.AuthenticationResponseDto;
import org.apartment.entity.Token;
import org.apartment.entity.User;
import org.apartment.repository.TokenRepository;
import org.apartment.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Transactional
  public AuthenticationResponseDto register(User user) {
    log.debug("Registering user with email: {}", user.getEmail());
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    var savedUser = userRepository.save(user);
    var jwtToken = jwtService.generateToken(user, user.getId());
    saveUserToken(savedUser, jwtToken);

    kafkaTemplate.send("email_topic", savedUser.getEmail());

    log.info("User registered successfully: {}", user.getEmail());
    return AuthenticationResponseDto.builder().accessToken(jwtToken)
        .refreshToken(jwtService.generateRefreshToken(user, user.getId())).build();
  }

  @Transactional
  public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
    log.debug("Authenticating user with email: {}", request.getEmail());
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
    var jwtToken = jwtService.generateToken(user, user.getId());
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);

    log.info("User authenticated successfully: {}", request.getEmail());
    return AuthenticationResponseDto.builder().accessToken(jwtToken)
        .refreshToken(jwtService.generateRefreshToken(user, user.getId())).build();
  }

  private void saveUserToken(User user, String jwtToken) {
    log.debug("Saving token for user: {}", user.getEmail());
    var token = Token.builder().user(user).token(jwtToken).revoked(false).build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    log.debug("Revoking all tokens for user: {}", user.getEmail());
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty()) {
      return;
    }
    validUserTokens.forEach(token -> token.setRevoked(true));
    tokenRepository.saveAll(validUserTokens);
  }

  public AuthenticationResponseDto refreshToken(HttpServletRequest request) {
    log.debug("Refreshing token");
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.error("Missing or invalid Authorization header");
      throw new RuntimeException("Missing or invalid Authorization header");
    }

    String refreshToken = authHeader.substring(7);
    String userEmail = jwtService.extractUsername(refreshToken);

    if (userEmail != null) {
      var user = this.userRepository.findByEmail(userEmail)
          .orElseThrow(() -> new RuntimeException("User not found"));

      if (jwtService.isTokenValid(refreshToken, user)) {
        String accessToken = jwtService.generateToken(user, user.getId());
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        log.info("Token refreshed successfully for user: {}", userEmail);
        return AuthenticationResponseDto.builder().accessToken(accessToken)
            .refreshToken(refreshToken).build();
      } else {
        log.error("Invalid refresh token for user: {}", userEmail);
        throw new RuntimeException("Invalid refresh token");
      }
    } else {
      log.error("Invalid refresh token");
      throw new RuntimeException("Invalid refresh token");
    }
  }
}