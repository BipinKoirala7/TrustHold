package com.trusthold.lease.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.repository.LeaseRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaseServiceTests {

  @InjectMocks
  private LeaseService leaseService;

  @Mock
  private PropertyService propertyService;

  @Mock
  private UserService userService;

  @Mock
  private LeaseRepository leaseRepository;

  @Mock
  private LeaseMapper leaseMapper;

  @Mock
  private CreateLeaseDto createLeaseDto;

  private AuthUser sampleAuthUser;
  private UUID landlordId;
  private UUID propertyId;
  private UUID tenantId;

  @BeforeEach
  void setUp() {
    landlordId = UUID.randomUUID();
    propertyId = UUID.randomUUID();
    tenantId = UUID.randomUUID();

    sampleAuthUser = new AuthUser(landlordId, "dummy-token", Role.LANDLORD);
  }

  @Nested
  class CreateLeaseTests {

    @Test
    void shouldThrowAppException_whenAuthUserIsNull() {
      // Act & Assert
      assertThatThrownBy(() -> leaseService.createLease(null, createLeaseDto))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(userService, propertyService, leaseMapper, leaseRepository);
    }

    @Test
    void shouldThrowAppException_whenUserIsNotALandlord() {
      // Arrange
      doThrow(new AppException(ErrorInfo.UNAUTHORIZED))
          .when(userService).isAValidLandlord(sampleAuthUser);

      // Act & Assert
      assertThatThrownBy(() -> leaseService.createLease(sampleAuthUser, createLeaseDto))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(propertyService, leaseMapper, leaseRepository);
    }

    @Test
    void shouldThrowAppException_whenPropertyDoesNotExist() {
      // Arrange
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(propertyService.exists(propertyId)).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> leaseService.createLease(sampleAuthUser, createLeaseDto))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.PROPERTY_NOT_FOUND);

      verify(userService, never()).isUserTenant(any());
      verifyNoInteractions(leaseMapper, leaseRepository);
    }

    @Test
    void shouldThrowAppException_whenProvidedTenantIsNotATenant() {
      // Arrange
      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantId()).thenReturn(tenantId);
      when(propertyService.exists(propertyId)).thenReturn(true);
      when(userService.isUserTenant(tenantId)).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> leaseService.createLease(sampleAuthUser, createLeaseDto))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.PROVIDED_TENANT_IS_NOT_A_TENANT);

      verifyNoInteractions(leaseMapper, leaseRepository);
    }

    @Test
    void shouldPropagateError_whenLeaseSaveFails() {
      // Arrange
      Lease lease = new Lease();

      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantId()).thenReturn(tenantId);
      when(propertyService.exists(propertyId)).thenReturn(true);
      when(userService.isUserTenant(tenantId)).thenReturn(true);
      when(leaseMapper.toLeaseEntity(createLeaseDto)).thenReturn(lease);
      doThrow(new RuntimeException("Database connection failed"))
          .when(leaseRepository).save(any(Lease.class));

      // Act & Assert
      assertThatThrownBy(() -> leaseService.createLease(sampleAuthUser, createLeaseDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Database connection failed");

      verify(leaseRepository).save(lease);
    }

    @Test
    void shouldCreateLeaseSuccessfully() {
      // Arrange
      Lease lease = new Lease();

      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantId()).thenReturn(tenantId);
      when(propertyService.exists(propertyId)).thenReturn(true);
      when(userService.isUserTenant(tenantId)).thenReturn(true);
      when(leaseMapper.toLeaseEntity(createLeaseDto)).thenReturn(lease);

      // Act
      leaseService.createLease(sampleAuthUser, createLeaseDto);

      // Assert
      InOrder inOrder = inOrder(userService, propertyService, leaseMapper, leaseRepository);
      inOrder.verify(userService).isAValidLandlord(sampleAuthUser);
      inOrder.verify(propertyService).exists(propertyId);
      inOrder.verify(leaseMapper).toLeaseEntity(createLeaseDto);
      inOrder.verify(leaseRepository).save(lease);
    }

    @Test
    void shouldAssignAuthenticatedUserAsLandlord_whenLeaseIsCreated() {
      // Arrange
      Lease lease = new Lease();

      when(createLeaseDto.propertyId()).thenReturn(propertyId);
      when(createLeaseDto.tenantId()).thenReturn(tenantId);
      when(propertyService.exists(propertyId)).thenReturn(true);
      when(userService.isUserTenant(tenantId)).thenReturn(true);
      when(leaseMapper.toLeaseEntity(createLeaseDto)).thenReturn(lease);

      // Act
      leaseService.createLease(sampleAuthUser, createLeaseDto);

      // Assert
      ArgumentCaptor<Lease> leaseCaptor = ArgumentCaptor.forClass(Lease.class);
      verify(leaseRepository).save(leaseCaptor.capture());
      assertThat(leaseCaptor.getValue().getLandlordId()).isEqualTo(landlordId);
    }
  }
}