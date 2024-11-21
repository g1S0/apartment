package org.apartment.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequestDto {
  @NotBlank(message = "Current password must not be empty")
  private String currentPassword;

  @NotBlank(message = "New password must not be empty")
  private String newPassword;

  @NotBlank(message = "Confirmation password must not be empty")
  private String confirmationPassword;
}
