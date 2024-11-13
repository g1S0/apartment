package org.apartment.auth.service;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apartment.auth.dto.ChangePasswordRequestDto;
import org.apartment.auth.entity.User;
import org.apartment.auth.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService
{

  private final PasswordEncoder passwordEncoder;
  private final UserRepository repository;

  public void changePassword(ChangePasswordRequestDto request, Principal connectedUser)
  {
    var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
    log.debug("Attempting to change password for user: {}", user.getEmail());

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
    {
      log.error("Failed to change password: current password does not match for user: {}",
          user.getEmail());
      throw new IllegalStateException("Wrong password");
    }

    if (!request.getNewPassword().equals(request.getConfirmationPassword()))
    {
      log.error(
          "Failed to change password: new password and confirmation do not match for user: {}",
          user.getEmail());
      throw new IllegalStateException("Password are not the same");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    repository.save(user);
    log.info("Password successfully changed for user: {}", user.getEmail());
  }
}

