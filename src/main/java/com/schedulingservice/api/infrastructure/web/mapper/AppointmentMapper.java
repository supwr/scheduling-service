package com.schedulingservice.api.infrastructure.web.mapper;

import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;
import com.schedulingservice.api.model.AppointmentRequest;
import com.schedulingservice.api.model.AppointmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "status", expression = "java(AppointmentStatus.SCHEDULED)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Appointment toDomain(AppointmentRequest request);

    AppointmentResponse toResponse(Appointment appointment);

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default Instant map(final OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
