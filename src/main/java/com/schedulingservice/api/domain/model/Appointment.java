package com.schedulingservice.api.domain.model;

import com.schedulingservice.api.domain.exception.ValidationException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Appointment {

    private final Long id;
    private final UUID uuid;
    private final UUID patientId;
    private final UUID doctorId;
    private final OffsetDateTime appointmentDateTime;
    private final AppointmentStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant deletedAt;

    public Appointment(
        final Long id,
        final UUID uuid,
        final UUID patientId,
        final UUID doctorId,
        final OffsetDateTime appointmentDateTime,
        final AppointmentStatus status,
        final Instant createdAt,
        final Instant updatedAt,
        final Instant deletedAt
    ) {
        this.id = id;
        this.uuid = uuid;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public OffsetDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void validate() {
        if (patientId == null) {
            throw new ValidationException("patientId", null, "Patient id is required");
        }
        if (doctorId == null) {
            throw new ValidationException("doctorId", null, "Doctor id is required");
        }
        if (appointmentDateTime == null) {
            throw new ValidationException("appointmentDateTime", null, "Appointment date and time is required");
        }
        if (status == null) {
            throw new ValidationException("status", null, "Appointment status is required");
        }
    }
}
