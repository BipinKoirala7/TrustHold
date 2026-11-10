package com.trusthold.lease.service;

import com.trusthold.lease.dtos.LeaseActivityEvent;
import com.trusthold.lease.model.LeaseActivity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LeaseActivityMapper {

  LeaseActivity toEntity(LeaseActivityEvent event);
}
