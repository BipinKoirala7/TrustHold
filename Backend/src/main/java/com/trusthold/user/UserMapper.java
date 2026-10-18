package com.trusthold.user;

import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.user.dtos.PublicUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "role", source = "registerUserDto.registrationRole")
  User createUserDtoToUser(RegisterUserDto registerUserDto);
  PublicUserDto userToPublicUserDto(User user);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateUserFromDto(UpdateUserDto updateUserDto, @MappingTarget User user);
}
