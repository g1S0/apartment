package org.apartment.auth.mapper;

import org.apartment.auth.dto.RegisterRequestDto;
import org.apartment.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RegisterMapper
{
  RegisterMapper INSTANCE = Mappers.getMapper(RegisterMapper.class);

  User toEntity(RegisterRequestDto registerRequest);
}