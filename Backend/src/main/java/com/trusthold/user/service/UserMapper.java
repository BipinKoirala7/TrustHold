package com.trusthold.user.service;

import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.user.dtos.PublicUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "role", source = "registerUserDto.registrationRole")
  User createUserDtoToUser(RegisterUserDto registerUserDto);
  PublicUserDto userToPublicUserDto(User user);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateUserFromDto(UpdateUserDto updateUserDto, @MappingTarget User user);
}
