package org.apartment.dto;


import lombok.*;
import org.apartment.entity.PropertyStatus;
import org.apartment.entity.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDto {
    private String title;
    private String description;
    private PropertyType type;
    private BigDecimal price;
    private String city;
    private PropertyStatus status;
    private Long postedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
