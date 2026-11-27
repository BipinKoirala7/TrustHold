package com.trusthold.lease.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.dtos.LeaseActivityEvent;
import com.trusthold.lease.dtos.LeaseSummaryDto;
import com.trusthold.lease.model.Activity;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseStatus;
import com.trusthold.lease.repository.LeaseRepository;
import com.trusthold.property.dtos.PropertySummaryDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.service.PropertyMapper;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.exception.AuthException;
import com.trusthold.shared.exception.LeaseException;
import com.trusthold.shared.exception.PropertyException;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import com.trusthold.user.model.User;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
  private PropertyMapper propertyMapper;

  @Mock
  private CreateLeaseDto createLeaseDto;

  @Mock
  private User tenant;

  @Mock
  private Property property;

  @Mock
  private Lease lease;

  private AuthUser landlordUser;
  private AuthUser tenantUser;
  private UUID landlordId;
  private UUID tenantId;
  private String tenantEmail;
  private UUID propertyId;
  private UUID leaseId;

  @BeforeEach
  void setUp() {
    landlordId = UUID.randomUUID();
    tenantEmail = "john.doe@example.com";
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
          .hasFieldOrPropertyWithValue("code", AuthException.UNAUTHORIZED);

      verifyNoInteractions(propertyService, userService, leaseMapper, leaseRepository,
          applicationEventPublisher);
    }

    @Test
    void shouldPropagateException_whenPropertyLookupFails() {
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(propertyService.getProperty(landlordUser, propertyId))
          .thenThrow(new AppException(PropertyException.PROPERTY_NOT_FOUND));

      assertThatThrownBy(() -> leaseService.createLease(landlordUser, createLeaseDto))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("code", PropertyException.PROPERTY_NOT_FOUND);

      verifyNoInteractions(userService, leaseMapper, leaseRepository, applicationEventPublisher);
    }

    @Test
    void shouldThrowAppException_whenProvidedTenantIsNotATenant() {
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantEmail()).thenReturn(tenantEmail);
      when(propertyService.getProperty(landlordUser, propertyId)).thenReturn(property);
      when(userService.isUserTenant(tenantEmail)).thenReturn(false);

      assertThatThrownBy(() -> leaseService.createLease(landlordUser, createLeaseDto))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("code", LeaseException.PROVIDED_TENANT_IS_NOT_A_TENANT);

      verifyNoInteractions(leaseMapper, leaseRepository, applicationEventPublisher);
    }

    @Test
    void shouldPropagateException_andNotPublishEvent_whenSaveFails() {
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantEmail()).thenReturn(tenantEmail);
      when(propertyService.getProperty(landlordUser, propertyId)).thenReturn(property);
      when(userService.isUserTenant(tenantEmail)).thenReturn(true);
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
      inOrder.verify(propertyService).getProperty(landlordUser, propertyId);
      inOrder.verify(userService).isUserTenant(tenantEmail);
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
      when(createLeaseDto.tenantEmail()).thenReturn(tenantEmail);
      when(propertyService.getProperty(landlordUser, propertyId)).thenReturn(property);
      when(userService.isUserTenant(tenantEmail)).thenReturn(true);
      when(leaseMapper.toLeaseEntity(createLeaseDto)).thenReturn(lease);
      when(lease.getId()).thenReturn(leaseId);
    }
  }

  @Nested
  class GetUsersLeasesTests {

    private final PropertySummaryDto propertySummary =
        new PropertySummaryDto("Sunrise Apartment", null, "Pokhara");

    @Test
    void shouldThrowUnauthorized_whenAuthUserIsNull() {
      assertThatThrownBy(() -> leaseService.getUsersLeases(null))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("code", AuthException.UNAUTHORIZED);

      verifyNoInteractions(leaseRepository, propertyService, propertyMapper, userService);
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoLeases() {
      when(leaseRepository.findAllByLandlordId(landlordId)).thenReturn(List.of());

      assertThat(leaseService.getUsersLeases(landlordUser)).isEmpty();

      verifyNoInteractions(propertyService, propertyMapper, userService);
    }

    @Test
    void shouldReturnLeasesByLandlordId_andMapToSummary_whenUserIsLandlord() {
      Instant startDate = Instant.now();
      stubLease(lease, leaseId, propertyId, tenantId, startDate);
      when(leaseRepository.findAllByLandlordId(landlordId)).thenReturn(List.of(lease));
      when(propertyService.getPropertyById(propertyId)).thenReturn(property);
      when(propertyMapper.toPropertySummary(property)).thenReturn(propertySummary);
      when(userService.getUserByIdOrNull(tenantId)).thenReturn(Optional.of(tenant));
      when(tenant.getFirstName()).thenReturn("John");
      when(tenant.getLastName()).thenReturn("Doe");

      List<LeaseSummaryDto> result = leaseService.getUsersLeases(landlordUser);

      assertThat(result).hasSize(1);
      LeaseSummaryDto dto = result.getFirst();
      assertThat(dto.id()).isEqualTo(leaseId);
      assertThat(dto.propertySummary()).isSameAs(propertySummary);
      assertThat(dto.tenantName()).isEqualTo("John Doe");
      assertThat(dto.depositAmount()).isEqualByComparingTo("1000");
      assertThat(dto.status()).isEqualTo(LeaseStatus.ACTIVE);
      assertThat(dto.startDate()).isEqualTo(startDate);

      verify(leaseRepository, never()).findAllByTenantId(any());
    }

    @Test
    void shouldReturnLeasesByTenantId_whenUserIsTenant() {
      stubLease(lease, leaseId, propertyId, tenantId, Instant.now());
      when(leaseRepository.findAllByTenantId(tenantId)).thenReturn(List.of(lease));
      when(propertyService.getPropertyById(propertyId)).thenReturn(property);
      when(propertyMapper.toPropertySummary(property)).thenReturn(propertySummary);
      when(userService.getUserByIdOrNull(tenantId)).thenReturn(Optional.of(tenant));
      when(tenant.getFirstName()).thenReturn("John");
      when(tenant.getLastName()).thenReturn("Doe");

      List<LeaseSummaryDto> result = leaseService.getUsersLeases(tenantUser);

      assertThat(result).hasSize(1);
      verify(leaseRepository, never()).findAllByLandlordId(any());
    }

    @Test
    void shouldSetTenantNameToNull_whenTenantIsNotFound() {
      stubLease(lease, leaseId, propertyId, tenantId, Instant.now());
      when(leaseRepository.findAllByLandlordId(landlordId)).thenReturn(List.of(lease));
      when(propertyService.getPropertyById(propertyId)).thenReturn(property);
      when(propertyMapper.toPropertySummary(property)).thenReturn(propertySummary);
      when(userService.getUserByIdOrNull(tenantId)).thenReturn(Optional.empty());

      List<LeaseSummaryDto> result = leaseService.getUsersLeases(landlordUser);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().tenantName()).isNull();
    }

    @Test
    void shouldMapEveryLease_whenUserHasMultipleLeases() {
      Lease secondLease = mock(Lease.class);
      Property secondProperty = mock(Property.class);
      UUID secondLeaseId = UUID.randomUUID();
      UUID secondPropertyId = UUID.randomUUID();
      UUID secondTenantId = UUID.randomUUID();
      User secondTenant = mock(User.class);
      PropertySummaryDto secondInfo = new PropertySummaryDto("Lakeside House", null, "Kathmandu");

      stubLease(lease, leaseId, propertyId, tenantId, Instant.now());
      stubLease(secondLease, secondLeaseId, secondPropertyId, secondTenantId, Instant.now());
      when(leaseRepository.findAllByLandlordId(landlordId)).thenReturn(List.of(lease, secondLease));

      when(propertyService.getPropertyById(propertyId)).thenReturn(property);
      when(propertyService.getPropertyById(secondPropertyId)).thenReturn(secondProperty);
      when(propertyMapper.toPropertySummary(property)).thenReturn(propertySummary);
      when(propertyMapper.toPropertySummary(secondProperty)).thenReturn(secondInfo);

      when(userService.getUserByIdOrNull(tenantId)).thenReturn(Optional.of(tenant));
      when(userService.getUserByIdOrNull(secondTenantId)).thenReturn(Optional.of(secondTenant));
      when(tenant.getFirstName()).thenReturn("John");
      when(tenant.getLastName()).thenReturn("Doe");
      when(secondTenant.getFirstName()).thenReturn("Jane");
      when(secondTenant.getLastName()).thenReturn("Smith");

      List<LeaseSummaryDto> result = leaseService.getUsersLeases(landlordUser);

      assertThat(result).extracting(LeaseSummaryDto::id)
          .containsExactly(leaseId, secondLeaseId);
      assertThat(result).extracting(LeaseSummaryDto::tenantName)
          .containsExactly("John Doe", "Jane Smith");
      assertThat(result).extracting(LeaseSummaryDto::propertySummary)
          .containsExactly(propertySummary, secondInfo);
    }

    @Test
    void shouldPropagateException_whenPropertyLookupFails() {
      when(lease.getPropertyId()).thenReturn(propertyId);
      when(leaseRepository.findAllByLandlordId(landlordId)).thenReturn(List.of(lease));
      when(propertyService.getPropertyById(propertyId))
          .thenThrow(new AppException(PropertyException.PROPERTY_NOT_FOUND));

      assertThatThrownBy(() -> leaseService.getUsersLeases(landlordUser))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("code", PropertyException.PROPERTY_NOT_FOUND);

      verifyNoInteractions(propertyMapper, userService);
    }

    private void stubLease(Lease target, UUID id, UUID propId, UUID tenId, Instant startDate) {
      when(target.getId()).thenReturn(id);
      when(target.getPropertyId()).thenReturn(propId);
      when(target.getTenantId()).thenReturn(tenId);
      when(target.getDepositAmount()).thenReturn(new BigDecimal("1000"));
      when(target.getStatus()).thenReturn(LeaseStatus.ACTIVE);
      when(target.getStartDate()).thenReturn(startDate);
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