package org.apartment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailDetailsDto {
  private final String toEmail;
  private final String subject;
  private final String messageBody;
}
