package org.apartment.mapper;

import org.apartment.dto.PropertyDto;
import org.apartment.entity.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PropertyMapper
{
  PropertyMapper INSTANCE = Mappers.getMapper(PropertyMapper.class);

  @Mapping(target = "postedBy", source = "postedBy")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  PropertyDto toDto(Property property);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Property toEntity(PropertyDto propertyDto);
}
