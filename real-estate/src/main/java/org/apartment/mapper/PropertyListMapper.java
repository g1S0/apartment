package org.apartment.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.apartment.dto.PropertyDto;
import org.apartment.dto.PropertyListDto;
import org.apartment.entity.Property;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface PropertyListMapper {

  PropertyListMapper INSTANCE = Mappers.getMapper(PropertyListMapper.class);

  default PropertyListDto toPropertyListDto(Page<Property> propertyPage) {
    List<PropertyDto> propertyListDto =
        propertyPage.getContent().stream().map(PropertyMapper.INSTANCE::toDto)
            .collect(Collectors.toList());

    return PropertyListDto.builder().content(propertyListDto)
        .totalElements((int) propertyPage.getTotalElements())
        .totalPages(propertyPage.getTotalPages()).last(propertyPage.isLast())
        .first(propertyPage.isFirst()).numberOfElements(propertyPage.getNumberOfElements()).build();
  }
}