package com.trusthold.lease.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.repository.LeaseRepository;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.Guard;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import com.trusthold.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class LeaseService {

  private final PropertyService propertyService;
  private final UserService userService;
  private final LeaseRepository leaseRepository;
  private final LeaseMapper leaseMapper;

  public void createLease(AuthUser authUser, @NotNull CreateLeaseDto createLeaseDto) {
    log.info("Creating lease with data: {}", createLeaseDto);
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.UNAUTHORIZED));

    if(authUser.role() != Role.LANDLORD){
      log.warn("User with id {} is not a landlord", authUser.id());
      throw new AppException(ErrorInfo.TENANT_CANNOT_CREATE_LEASE);
    }

    if(!propertyService.exists(createLeaseDto.propertyId())){
      log.warn("Property with id {} does not exist", createLeaseDto.propertyId());
      throw new AppException(ErrorInfo.PROPERTY_NOT_FOUND);
    }

    if(!userService.isUserTenant(createLeaseDto.tenantId())){
      log.warn("User with id {} is not a tenant", createLeaseDto.tenantId());
      throw new AppException(ErrorInfo.PROVIDED_TENANT_IS_NOT_A_TENANT);
    }

    Lease lease = leaseMapper.toLeaseEntity(createLeaseDto);

    leaseRepository.save(lease);
    log.info("Lease created successfully");
  }
}
