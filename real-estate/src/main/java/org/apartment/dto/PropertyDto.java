package org.apartment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apartment.entity.PropertyImage;
import org.apartment.entity.PropertyStatus;
import org.apartment.entity.PropertyType;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDto {
  @NotBlank(message = "Title is required")
  private String title;

  @Size(max = 2000, message = "Description should not exceed 2000 characters")
  private String description;

  @NotNull(message = "Type is required")
  private PropertyType type;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
  @DecimalMax(value = "1000000000.0", message = "Price must not exceed 1 billion")
  private BigDecimal price;

  @NotBlank(message = "City is required")
  private String city;

  @NotNull(message = "Status is required")
  private PropertyStatus status;

  private Integer postedBy;

  private List<PropertyImage> images;

  private LocalDate createdAt;
  private LocalDate updatedAt;
}