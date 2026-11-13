package com.trusthold.lease.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.dtos.LeaseActivityEvent;
import com.trusthold.lease.model.Activity;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.repository.LeaseRepository;
import com.trusthold.property.model.Property;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.Guard;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseService {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final PropertyService propertyService;
  private final LeaseRepository leaseRepository;
  private final UserService userService;
  private final LeaseMapper leaseMapper;

  /**
   * Creates a new lease for the authenticated user.
   *
   * @param authUser authenticated user
   * @param createLeaseDto the lease details
   * @throws AppException if the tenant is not a tenant
   */
  @PreAuthorize("hasRole('LANDLORD')")
  @Transactional
  public void createLease(AuthUser authUser, CreateLeaseDto createLeaseDto) {
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.UNAUTHORIZED));
    log.info("Creating lease with data: {} for user: {}", createLeaseDto, authUser);

    Property property = propertyService.getPropertyById(authUser, createLeaseDto.propertyId());

    if(!userService.isUserTenant(createLeaseDto.tenantId())){
      log.warn("User with id {} is not a tenant", createLeaseDto.tenantId());
      throw new AppException(ErrorInfo.PROVIDED_TENANT_IS_NOT_A_TENANT);
    }

    Lease lease = leaseMapper.toLeaseEntity(createLeaseDto);
    lease.setLandlordId(authUser.id());
    leaseRepository.save(lease);

    String description = "Lease created for " + property.getName();
    applicationEventPublisher.publishEvent(new LeaseActivityEvent(lease.getId(), Activity.LEASE_CREATED.getLabel(), description));

    log.info("Lease created with id {}", lease.getId());
  }

  @PreAuthorize("isAuthenticated()")
  public List<Lease> getUsersLeases(AuthUser authUser){
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.UNAUTHORIZED));
    log.info("Getting leases for user={}", authUser.id());

    if(authUser.isLandlord()){
      return leaseRepository.findAllByLandlordId(authUser.id());
    } else {
      return leaseRepository.findAllByTenantId(authUser.id());
    }
  }

  public boolean exists(@NotNull UUID leaseId){
    return leaseRepository.existsById(leaseId);
  }
}
