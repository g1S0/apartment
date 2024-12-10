package org.apartment.mapper;

import org.apartment.dto.UserRegistrationDto;
import org.apartment.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RegisterMapper {
  RegisterMapper INSTANCE = Mappers.getMapper(RegisterMapper.class);

  User toEntity(UserRegistrationDto registerRequest);
}