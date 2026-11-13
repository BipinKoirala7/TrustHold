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
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import com.trusthold.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaseServiceTests {

  @InjectMocks
  private LeaseService leaseService;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Mock
  private PropertyService propertyService;

  @Mock
  private LeaseRepository leaseRepository;

  @Mock
  private UserService userService;

  @Mock
  private LeaseMapper leaseMapper;

  @Mock
  private CreateLeaseDto createLeaseDto;

  @Mock
  private Property property;

  @Mock
  private Lease lease;

  private AuthUser landlordUser;
  private AuthUser tenantUser;
  private UUID landlordId;
  private UUID tenantId;
  private UUID propertyId;
  private UUID leaseId;

  @BeforeEach
  void setUp() {
    landlordId = UUID.randomUUID();
    tenantId = UUID.randomUUID();
    propertyId = UUID.randomUUID();
    leaseId = UUID.randomUUID();

    landlordUser = new AuthUser(landlordId, "dummy-token", Role.LANDLORD);
    tenantUser = new AuthUser(tenantId, "dummy-token", Role.TENANT);
  }

  @Nested
  class CreateLeaseTests {

    @Test
    void shouldThrowUnauthorized_whenAuthUserIsNull() {
      assertThatThrownBy(() -> leaseService.createLease(null, createLeaseDto))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(propertyService, userService, leaseMapper, leaseRepository,
          applicationEventPublisher);
    }

    @Test
    void shouldPropagateException_whenPropertyLookupFails() {
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(propertyService.getPropertyById(landlordUser, propertyId))
          .thenThrow(new AppException(ErrorInfo.PROPERTY_NOT_FOUND));

      assertThatThrownBy(() -> leaseService.createLease(landlordUser, createLeaseDto))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.PROPERTY_NOT_FOUND);

      verifyNoInteractions(userService, leaseMapper, leaseRepository, applicationEventPublisher);
    }

    @Test
    void shouldThrowAppException_whenProvidedTenantIsNotATenant() {
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantId()).thenReturn(tenantId);
      when(propertyService.getPropertyById(landlordUser, propertyId)).thenReturn(property);
      when(userService.isUserTenant(tenantId)).thenReturn(false);

      assertThatThrownBy(() -> leaseService.createLease(landlordUser, createLeaseDto))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.PROVIDED_TENANT_IS_NOT_A_TENANT);

      verifyNoInteractions(leaseMapper, leaseRepository, applicationEventPublisher);
    }

    @Test
    void shouldPropagateException_andNotPublishEvent_whenSaveFails() {
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantId()).thenReturn(tenantId);
      when(propertyService.getPropertyById(landlordUser, propertyId)).thenReturn(property);
      when(userService.isUserTenant(tenantId)).thenReturn(true);
      when(leaseMapper.toLeaseEntity(createLeaseDto)).thenReturn(lease);
      when(leaseRepository.save(lease)).thenThrow(new RuntimeException("Database connection failed"));

      assertThatThrownBy(() -> leaseService.createLease(landlordUser, createLeaseDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Database connection failed");

      verify(leaseRepository).save(lease);
      verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldCreateLease_whenInputIsValid() {
      stubHappyPath();

      leaseService.createLease(landlordUser, createLeaseDto);

      InOrder inOrder = inOrder(propertyService, userService, leaseMapper, lease,
          leaseRepository, applicationEventPublisher);
      inOrder.verify(propertyService).getPropertyById(landlordUser, propertyId);
      inOrder.verify(userService).isUserTenant(tenantId);
      inOrder.verify(leaseMapper).toLeaseEntity(createLeaseDto);
      inOrder.verify(lease).setLandlordId(landlordId);
      inOrder.verify(leaseRepository).save(lease);
      inOrder.verify(applicationEventPublisher).publishEvent(any(LeaseActivityEvent.class));
    }

    @Test
    void shouldAssignAuthenticatedUserAsLandlord_beforeSaving() {
      stubHappyPath();

      leaseService.createLease(landlordUser, createLeaseDto);

      InOrder inOrder = inOrder(lease, leaseRepository);
      inOrder.verify(lease).setLandlordId(landlordId);
      inOrder.verify(leaseRepository).save(lease);
    }

    @Test
    void shouldPublishLeaseCreatedActivityEvent_withPropertyNameInDescription() {
      stubHappyPath();
      when(property.getName()).thenReturn("Sunrise Apartment");

      leaseService.createLease(landlordUser, createLeaseDto);

      ArgumentCaptor<LeaseActivityEvent> captor = ArgumentCaptor.forClass(LeaseActivityEvent.class);
      verify(applicationEventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue()).isEqualTo(new LeaseActivityEvent(
          leaseId,
          Activity.LEASE_CREATED.getLabel(),
          "Lease created for Sunrise Apartment"));
    }

    private void stubHappyPath() {
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantId()).thenReturn(tenantId);
      when(propertyService.getPropertyById(landlordUser, propertyId)).thenReturn(property);
      when(userService.isUserTenant(tenantId)).thenReturn(true);
      when(leaseMapper.toLeaseEntity(createLeaseDto)).thenReturn(lease);
      when(lease.getId()).thenReturn(leaseId);
    }
  }

  @Nested
  class GetUsersLeasesTests {

    @Test
    void shouldThrowUnauthorized_whenAuthUserIsNull() {
      assertThatThrownBy(() -> leaseService.getUsersLeases(null))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(leaseRepository);
    }

    @Test
    void shouldReturnLeasesByLandlordId_whenUserIsLandlord() {
      List<Lease> leases = List.of(lease);
      when(leaseRepository.findAllByLandlordId(landlordId)).thenReturn(leases);

      List<Lease> result = leaseService.getUsersLeases(landlordUser);

      assertThat(result).isSameAs(leases);
      verify(leaseRepository, never()).findAllByTenantId(any());
    }

    @Test
    void shouldReturnLeasesByTenantId_whenUserIsTenant() {
      List<Lease> leases = List.of(lease);
      when(leaseRepository.findAllByTenantId(tenantId)).thenReturn(leases);

      List<Lease> result = leaseService.getUsersLeases(tenantUser);

      assertThat(result).isSameAs(leases);
      verify(leaseRepository, never()).findAllByLandlordId(any());
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoLeases() {
      when(leaseRepository.findAllByLandlordId(landlordId)).thenReturn(List.of());

      assertThat(leaseService.getUsersLeases(landlordUser)).isEmpty();
    }
  }

  @Nested
  class ExistsTests {

    @Test
    void shouldReturnTrue_whenLeaseExists() {
      when(leaseRepository.existsById(leaseId)).thenReturn(true);

      assertThat(leaseService.exists(leaseId)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenLeaseDoesNotExist() {
      when(leaseRepository.existsById(leaseId)).thenReturn(false);

      assertThat(leaseService.exists(leaseId)).isFalse();
    }
  }
}