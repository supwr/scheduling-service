package com.schedulingservice.api.infrastructure.persistence.repository;

import com.schedulingservice.api.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    Optional<AppointmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    @Query("""
        select a
        from AppointmentEntity a
        where a.patientId = :patientId
          and a.deletedAt is null
        """)
    List<AppointmentEntity> findActiveAppointmentsByPatientId(
        @Param("patientId") UUID patientId
    );
}
