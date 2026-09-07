package com.schedulingservice.api.infrastructure.persistence.mapper;

import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.infrastructure.persistence.entity.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentPersistenceMapper {

    @Mapping(target = "uuid", expression = "java(appointment.getUuid() == null ? UUID.randomUUID() : appointment.getUuid())")
    AppointmentEntity toEntity(Appointment appointment);

    Appointment toDomain(AppointmentEntity entity);
}
