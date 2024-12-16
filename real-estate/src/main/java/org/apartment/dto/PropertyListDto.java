package org.apartment.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListDto {
  private List<PropertyDto> content;
  private int totalElements;
  private int totalPages;
  private boolean last;
  private boolean first;
  private int numberOfElements;
}
