package com.schedulingservice.api.application.usecase.appointment.create;

import com.schedulingservice.api.application.dto.event.AppointmentHistoryEvent;
import com.schedulingservice.api.application.dto.event.AppointmentHistoryEventType;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.exception.ConflictException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class CreateAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final AppointmentEventPublisher eventPublisher;

    public CreateAppointmentUseCase(
        final AppointmentGateway appointmentGateway,
        final AppointmentEventPublisher eventPublisher
    ) {
        this.appointmentGateway = appointmentGateway;
        this.eventPublisher = eventPublisher;
    }

    public Appointment execute(final Appointment appointment) {
        Objects.requireNonNull(appointment);
        appointment.validate();

        if (hasConflictWithinFourHours(appointment)) {
            throw new ConflictException(
                "Appointment",
                "A patient can only have one non-cancelled appointment within a 4 hour window"
            );
        }

        final Appointment appointmentToSave = new Appointment(
            null,
            null,
            appointment.getPatientId(),
            appointment.getDoctorId(),
            appointment.getAppointmentDateTime(),
            appointment.getFullname(),
            appointment.getEmail(),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        );

        final Appointment savedAppointment = appointmentGateway.save(appointmentToSave);
        eventPublisher.publishNotificationEvent(
            savedAppointment.getUuid(),
            new AppointmentScheduledNotificationEvent(
                savedAppointment.getPatientId(),
                savedAppointment.getDoctorId(),
                savedAppointment.getFullname(),
                savedAppointment.getEmail(),
                savedAppointment.getAppointmentDateTime()
            )
        );
        eventPublisher.publishHistoryEvent(
            savedAppointment.getUuid(),
            new AppointmentHistoryEvent(
                AppointmentHistoryEventType.SCHEDULED,
                savedAppointment.getPatientId(),
                savedAppointment.getDoctorId(),
                savedAppointment.getFullname(),
                savedAppointment.getEmail(),
                savedAppointment.getAppointmentDateTime()
            )
        );
        return savedAppointment;
    }

    private boolean hasConflictWithinFourHours(final Appointment appointment) {
        final List<Appointment> activeAppointments = appointmentGateway.findActiveAppointmentsByPatientId(appointment.getPatientId());
        final var requestedTime = appointment.getAppointmentDateTime().toInstant().truncatedTo(ChronoUnit.MINUTES);

        return activeAppointments.stream()
            .filter(existing -> existing.getAppointmentDateTime() != null)
            .map(existing -> existing.getAppointmentDateTime().toInstant().truncatedTo(ChronoUnit.MINUTES))
            .map(existingTime -> Duration.between(existingTime, requestedTime).abs())
            .anyMatch(duration -> duration.compareTo(Duration.ofHours(4)) < 0);
    }
}
