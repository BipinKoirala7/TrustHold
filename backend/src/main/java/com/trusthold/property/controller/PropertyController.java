package com.trusthold.property.controller;

import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.dtos.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lease")
@RequiredArgsConstructor
@Validated
public class PropertyController {

  private final PropertyService propertyService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> createProperty(@RequestBody @Valid CreatePropertyDto createPropertyDto) {
    propertyService.createProperty(createPropertyDto);
    return ApiResponse.success();
  }
}
