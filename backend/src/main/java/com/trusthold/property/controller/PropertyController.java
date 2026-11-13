package com.trusthold.property.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.dtos.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/property")
@RequiredArgsConstructor
@Validated
public class PropertyController {

  private final PropertyService propertyService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> createProperty(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid CreatePropertyDto createPropertyDto) {
    propertyService.createProperty(authUser, createPropertyDto);
    return ApiResponse.success();
  }

  @GetMapping
  public ApiResponse<List<Property>> getAllProperties(@AuthenticationPrincipal AuthUser authUser) {
    return ApiResponse.success(propertyService.getAllProperties(authUser));
  }

  @GetMapping("/{id}")
  public ApiResponse<Property> getPropertyById(@AuthenticationPrincipal AuthUser authUser, @PathVariable @NotBlank String id) {
    return ApiResponse.success(propertyService.getPropertyById(authUser, UUID.fromString(id)));
  }
}
