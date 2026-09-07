package com.schedulingservice.api.application.usecase.appointment.update;

import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.exception.ConflictException;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UpdateAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;

    public UpdateAppointmentUseCase(final AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public Appointment execute(final UUID uuid, final Appointment appointment) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(appointment);
        appointment.validate();

        final Appointment currentAppointment = appointmentGateway.findByUuid(uuid)
            .orElseThrow(() -> new EntityNotFoundException("Appointment", uuid.toString()));

        if (hasConflictWithinFourHours(appointment, currentAppointment.getUuid())) {
            throw new ConflictException(
                "Appointment",
                "A patient can only have one non-cancelled appointment within a 4 hour window"
            );
        }

        final Appointment updatedAppointment = new Appointment(
            currentAppointment.getId(),
            currentAppointment.getUuid(),
            appointment.getPatientId(),
            appointment.getDoctorId(),
            appointment.getAppointmentDateTime(),
            AppointmentStatus.SCHEDULED,
            currentAppointment.getCreatedAt(),
            Instant.now(),
            null
        );

        return appointmentGateway.save(updatedAppointment);
    }

    private boolean hasConflictWithinFourHours(final Appointment appointment, final UUID excludedUuid) {
        final List<Appointment> activeAppointments = appointmentGateway.findActiveAppointmentsByPatientId(appointment.getPatientId());
        final var requestedTime = appointment.getAppointmentDateTime().toInstant().truncatedTo(ChronoUnit.MINUTES);

        return activeAppointments.stream()
            .filter(existing -> existing.getUuid() == null || !existing.getUuid().equals(excludedUuid))
            .filter(existing -> existing.getAppointmentDateTime() != null)
            .map(existing -> existing.getAppointmentDateTime().toInstant().truncatedTo(ChronoUnit.MINUTES))
            .map(existingTime -> Duration.between(existingTime, requestedTime).abs())
            .anyMatch(duration -> duration.compareTo(Duration.ofHours(4)) < 0);
    }
}
