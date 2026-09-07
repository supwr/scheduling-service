package com.schedulingservice.api.infrastructure.persistence.gateway;

import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.infrastructure.persistence.mapper.AppointmentPersistenceMapper;
import com.schedulingservice.api.infrastructure.persistence.repository.AppointmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class AppointmentPersistenceGateway implements AppointmentGateway {

    private final AppointmentRepository repository;
    private final AppointmentPersistenceMapper mapper;

    public AppointmentPersistenceGateway(
        final AppointmentRepository repository,
        final AppointmentPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Appointment save(final Appointment appointment) {
        return mapper.toDomain(repository.save(mapper.toEntity(appointment)));
    }

    @Override
    public Optional<Appointment> findByUuid(final UUID uuid) {
        return repository.findByUuidAndDeletedAtIsNull(uuid).map(mapper::toDomain);
    }
}
