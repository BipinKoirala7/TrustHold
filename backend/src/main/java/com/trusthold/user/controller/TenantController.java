package com.trusthold.user.controller;

import com.trusthold.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
@Validated
public class TenantController {

  private final UserService userService;
}
