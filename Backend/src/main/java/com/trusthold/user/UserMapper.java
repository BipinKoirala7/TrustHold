package com.trusthold.user;

import com.trusthold.user.dtos.CreateUserDto;
import com.trusthold.user.dtos.PublicUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  PublicUserDto userToPublicUserDto(User user);
  User createUserDtoToUser(CreateUserDto createUserDto);

}
