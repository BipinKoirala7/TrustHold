package com.trusthold;

import com.trusthold.user.dtos.CreateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public void createUser(CreateUserDto createUserDto) {
    // Implement the logic to create a user
    // For example, you can map the CreateUserDto to a User entity and save it using the userRepository
  }
}
