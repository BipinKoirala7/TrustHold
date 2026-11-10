package com.trusthold.lease.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.dtos.LeaseActivityEvent;
import com.trusthold.lease.model.Activity;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.repository.LeaseActivityRepository;
import com.trusthold.lease.repository.LeaseRepository;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.Guard;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class LeaseService {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final LeaseActivityRepository leaseActivityRepository;
  private final LeaseActivityMapper leaseActivityMapper;
  private final PropertyService propertyService;
  private final LeaseRepository leaseRepository;
  private final UserService userService;
  private final LeaseMapper leaseMapper;

  /**
   * Creates a new lease for the authenticated user.
   *
   * @param authUser authenticated user
   * @param createLeaseDto the lease details
   * @throws AppException if the user is not a landlord
   * @throws AppException if the property does not exist
   * @throws AppException if the tenant is not a tenant
   */
  public void createLease(AuthUser authUser, @NotNull CreateLeaseDto createLeaseDto) {
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.UNAUTHORIZED));

    log.info("Creating lease with data: {} for user: {}", createLeaseDto, authUser);
    userService.isAValidLandlord(authUser);

    if(!propertyService.exists(createLeaseDto.propertyId())){
      log.warn("Property with id {} does not exist", createLeaseDto.propertyId());
      throw new AppException(ErrorInfo.PROPERTY_NOT_FOUND);
    }

    if(!userService.isUserTenant(createLeaseDto.tenantId())){
      log.warn("User with id {} is not a tenant", createLeaseDto.tenantId());
      throw new AppException(ErrorInfo.PROVIDED_TENANT_IS_NOT_A_TENANT);
    }

    Lease lease = leaseMapper.toLeaseEntity(createLeaseDto);
    lease.setLandlordId(authUser.id());
    leaseRepository.save(lease);

    applicationEventPublisher.publishEvent(new LeaseActivityEvent(lease.getId(), Activity.LEASE_CREATED.getLabel(), "Lease created for property with id " + lease.getId()));

    log.info("Lease created with id {}", lease.getId());
  }

  public void createLeaseActivity(@NotNull LeaseActivityEvent leaseActivityEvent){
    log.debug("Creating lease activity");
    leaseActivityRepository.save(leaseActivityMapper.toEntity(leaseActivityEvent));
  }
}
