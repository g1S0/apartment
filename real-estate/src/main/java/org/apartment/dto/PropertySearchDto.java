package org.apartment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apartment.entity.PropertyType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertySearchDto {
  private String keyword;
  private String city;
  private String status;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private LocalDate startDate;
  private LocalDate endDate;
  @Getter
  private PropertyType propertyType;

  public String getKeyword() {
    return keyword != null ? keyword.trim().toLowerCase() : "";
  }

  public String getCity() {
    return city != null ? city.trim().toLowerCase() : "";
  }

  public String getStatus() {
    return status != null ? status.trim().toLowerCase() : "";
  }

  public BigDecimal getMinPrice() {
    return minPrice != null ? minPrice : BigDecimal.ZERO;
  }

  public BigDecimal getMaxPrice() {
    return maxPrice != null ? maxPrice : new BigDecimal("1000000000.0");
  }

  public LocalDate getStartDate() {
    return startDate != null ? startDate : LocalDate.MIN;
  }

  public LocalDate getEndDate() {
    return endDate != null ? endDate : LocalDate.MAX;
  }
}
