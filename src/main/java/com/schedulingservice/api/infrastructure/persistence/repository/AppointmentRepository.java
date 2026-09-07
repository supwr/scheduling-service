package com.schedulingservice.api.infrastructure.persistence.repository;

import com.schedulingservice.api.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    Optional<AppointmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
