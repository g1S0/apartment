package org.apartment.auth.controller;

import java.security.Principal;
import org.apartment.auth.dto.ChangePasswordRequestDto;
import org.apartment.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @PutMapping
  public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDto request,
                                             Principal connectedUser) {
    service.changePassword(request, connectedUser);
    return ResponseEntity.ok().build();
  }
}
