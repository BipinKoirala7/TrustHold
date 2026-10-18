package com.trusthold.user;

import com.trusthold.user.dtos.RegisterUserDto;
import com.trusthold.user.exception.EmailAlreadyExists;
import com.trusthold.user.exception.UsernameAlreadyExists;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
}
