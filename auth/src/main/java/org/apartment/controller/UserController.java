package org.apartment.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import org.apartment.dto.ChangePasswordDto;
import org.apartment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService service) {
    this.userService = service;
  }

  @PutMapping
  public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordDto request,
                                             Principal connectedUser) {
    userService.changePassword(request, connectedUser);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteUserAccount(
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    userService.deleteUserAccount(authorizationHeader);
    return ResponseEntity.ok().build();
  }
}
