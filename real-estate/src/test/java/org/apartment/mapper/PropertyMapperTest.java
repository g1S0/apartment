package org.apartment.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apartment.dto.PropertyDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyStatus;
import org.apartment.entity.PropertyType;
import org.junit.jupiter.api.Test;

public class PropertyMapperTest {
  private final PropertyMapper propertyMapper = PropertyMapper.INSTANCE;

  @Test
  public void testToDto() {
    Property property = Property.builder().id(1L).title("Apartment").description("Test text")
        .type(PropertyType.APARTMENT).price(BigDecimal.valueOf(1000000)).city("Msk")
        .status(PropertyStatus.AVAILABLE).postedBy(123L).createdAt(LocalDate.now())
        .updatedAt(LocalDate.now()).build();


    PropertyDto propertyDto = propertyMapper.toDto(property);

    assertThat(propertyDto).isNotNull();
    assertThat(propertyDto.getTitle()).isEqualTo(property.getTitle());
    assertThat(propertyDto.getDescription()).isEqualTo(property.getDescription());
    assertThat(propertyDto.getType()).isEqualTo(property.getType());
    assertThat(propertyDto.getPrice()).isEqualTo(property.getPrice());
    assertThat(propertyDto.getCity()).isEqualTo(property.getCity());
    assertThat(propertyDto.getStatus()).isEqualTo(property.getStatus());
    assertThat(propertyDto.getPostedBy()).isEqualTo(property.getPostedBy());

    assertThat(propertyDto.getCreatedAt()).isEqualTo(property.getCreatedAt());
    assertThat(propertyDto.getUpdatedAt()).isEqualTo(property.getUpdatedAt());
  }

  @Test
  public void testToEntity() {
    PropertyDto propertyDto = PropertyDto.builder().title("Apartment").description("Test text")
        .type(PropertyType.APARTMENT).price(BigDecimal.valueOf(1000000)).city("Msk")
        .status(PropertyStatus.AVAILABLE).postedBy(123L).createdAt(LocalDate.now())
        .updatedAt(LocalDate.now()).build();

    Property property = propertyMapper.toEntity(propertyDto);

    assertThat(property).isNotNull();
    assertThat(property.getTitle()).isEqualTo(propertyDto.getTitle());
    assertThat(property.getDescription()).isEqualTo(propertyDto.getDescription());
    assertThat(property.getType()).isEqualTo(propertyDto.getType());
    assertThat(property.getPrice()).isEqualTo(propertyDto.getPrice());
    assertThat(property.getCity()).isEqualTo(propertyDto.getCity());
    assertThat(property.getStatus()).isEqualTo(propertyDto.getStatus());
    assertThat(property.getPostedBy()).isEqualTo(propertyDto.getPostedBy());

    assertThat(property.getCreatedAt()).isNull();
    assertThat(property.getUpdatedAt()).isNull();
  }
}