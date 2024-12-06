package org.apartment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.security.Principal;
import org.apartment.dto.ChangePasswordRequestDto;
import org.apartment.entity.User;
import org.apartment.repository.UserRepository;
import org.apartment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {
  @InjectMocks
  private UserService userService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserRepository userRepository;

  private User testUser =
      User.builder().id(1).firstName("John").secondName("Doe").email("test@example.com")
          .password("encodedPassword").build();

  private final String currentPassword = "currentPassword";
  private final String newPassword = "newPassword";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testChangePasswordSuccessfully() {
    Principal mockPrincipal = new UsernamePasswordAuthenticationToken(testUser, "encodedPassword");

    when(passwordEncoder.matches(eq(currentPassword), anyString())).thenReturn(true);
    when(passwordEncoder.encode(eq(newPassword))).thenReturn("encodedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    ChangePasswordRequestDto changePasswordRequest =
        new ChangePasswordRequestDto(currentPassword, newPassword, newPassword);

    userService.changePassword(changePasswordRequest, mockPrincipal);

    assertEquals("encodedNewPassword", testUser.getPassword());
  }

  @Test
  void testChangePasswordWithWrongCurrentPassword() {
    Principal mockPrincipal = new UsernamePasswordAuthenticationToken(testUser, "encodedPassword");

    String wrongPassword = "wrongPassword";
    when(passwordEncoder.matches(eq(wrongPassword), anyString())).thenReturn(false);

    ChangePasswordRequestDto changePasswordRequest =
        new ChangePasswordRequestDto(wrongPassword, newPassword, newPassword);

    assertThrows(IllegalStateException.class,
        () -> userService.changePassword(changePasswordRequest, mockPrincipal));
  }

  @Test
  void testChangePasswordWithMismatchedNewPasswordAndConfirmation() {
    Principal mockPrincipal = new UsernamePasswordAuthenticationToken(testUser, "encodedPassword");

    when(passwordEncoder.matches(eq(currentPassword), anyString())).thenReturn(true);

    String mismatchedPassword = "mismatchedPassword";
    ChangePasswordRequestDto changePasswordRequest =
        new ChangePasswordRequestDto(currentPassword, newPassword, mismatchedPassword);

    assertThrows(IllegalStateException.class,
        () -> userService.changePassword(changePasswordRequest, mockPrincipal));
  }
}