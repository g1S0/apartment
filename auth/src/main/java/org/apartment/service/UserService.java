package org.apartment.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.ChangePasswordDto;
import org.apartment.entity.User;
import org.apartment.repository.TokenRepository;
import org.apartment.repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final JwtService jwtService;

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Transactional
  public void deleteUserAccount(String authorizationHeader) {
    String userId = jwtService.extractUserIdFromAuthorizationHeader(authorizationHeader);
    Optional<User> userOptional = userRepository.findById(userId);

    userOptional.ifPresentOrElse(user -> {
      userRepository.delete(user);
      tokenRepository.deleteByUserId(userId);
      kafkaTemplate.send("account_data_delete", userId);
    }, () -> {
      throw new EntityNotFoundException("User with id " + userId + " not found");
    });
  }

  public void changePassword(ChangePasswordDto request, Principal connectedUser) {
    var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
    log.debug("Attempting to change password for user: {}", user.getEmail());

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      log.error("Failed to change password: current password does not match for user: {}",
          user.getEmail());
      throw new IllegalStateException("Wrong password");
    }

    if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
      log.error(
          "Failed to change password: new password and confirmation do not match for user: {}",
          user.getEmail());
      throw new IllegalStateException("Password are not the same");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    log.info("Password successfully changed for user: {}", user.getEmail());
  }
}

