package com.trusthold.lease.service;

import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.dtos.CreateLeasePhotoDto;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeasePhoto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LeaseMapper {

  Lease toLeaseEntity(CreateLeaseDto createLeaseDto);
  LeasePhoto toLeasePhotoEntity(CreateLeasePhotoDto createLeasePhotoDto);

}
