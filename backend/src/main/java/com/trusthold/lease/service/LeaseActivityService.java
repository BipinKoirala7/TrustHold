package com.trusthold.lease.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.LeaseActivityEvent;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseActivity;
import com.trusthold.lease.repository.LeaseActivityRepository;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.Guard;
import com.trusthold.shared.exception.AppException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@EnableAsync
@Validated
@RequiredArgsConstructor
@Slf4j
public class LeaseActivityService {

  private final LeaseActivityRepository leaseActivityRepository;
  private final LeaseActivityMapper leaseActivityMapper;
  private final LeaseService leaseService;

  /**
   * Creates a lease activity and saves it to the DB.
   * This method is called from an event listener.
   *
   * @param leaseActivityEvent the lease activity event
   */
  @Async
  public void createLeaseActivity(@NotNull @Valid LeaseActivityEvent leaseActivityEvent){
    log.debug("Creating lease activity");
    leaseActivityRepository.save(leaseActivityMapper.toEntity(leaseActivityEvent));
  }

  public List<LeaseActivity> getLeaseActivity(@NotNull UUID leaseId){
    log.debug("Getting lease activity for lease={}", leaseId);

    if(!leaseService.exists(leaseId)){
      log.warn("Lease with id={} does not exist", leaseId);
      throw new AppException(ErrorInfo.LEASE_NOT_FOUND);
    }

    return leaseActivityRepository.findByLeaseIdOrderByCreatedAtDesc(leaseId);
  }

  public List<LeaseActivity> getRecentActivity(AuthUser authUser){
    log.debug("Getting recent activity for user={}", authUser.id());
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.UNAUTHORIZED));

    List<Lease> leases = leaseService.getUsersLeases(authUser);

    return leaseActivityRepository.findTop5ByLeaseIdInOrderByCreatedAtDesc(leases.stream().map(Lease::getId).collect(Collectors.toList()));
  }
}
