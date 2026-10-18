package com.trusthold.user;

import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.user.dtos.PublicUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

  PublicUserDto userToPublicUserDto(User user);
  User createUserDtoToUser(RegisterUserDto registerUserDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateUserFromDto(UpdateUserDto updateUserDto, @MappingTarget User user);
}
