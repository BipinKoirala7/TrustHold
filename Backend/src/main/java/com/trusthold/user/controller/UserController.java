package com.trusthold.user.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.UserService;
import com.trusthold.user.dtos.PublicUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
@Validated
public class UserController {
  private final UserService userService;

  @GetMapping("/me")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<PublicUserDto> getAuthenticatedUser(@Valid @AuthenticationPrincipal AuthUser authUser){
    return ApiResponse.success(userService.getAuthenticatedUser(authUser), "Successfully Fetched User");
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<PublicUserDto> updateUser(@Valid @AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid UpdateUserDto updateUserDto){
    userService.updateUser(authUser, updateUserDto);
    return ApiResponse.success("Successfully Updated User");
  }
}
