package com.schedulingservice.api.application.usecase.appointment.delete;

import com.schedulingservice.api.application.dto.event.AppointmentHistoryEvent;
import com.schedulingservice.api.application.dto.event.AppointmentHistoryEventType;
import com.schedulingservice.api.application.dto.event.AppointmentNotificationBodyFactory;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class DeleteAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final AppointmentEventPublisher eventPublisher;

    public DeleteAppointmentUseCase(
        final AppointmentGateway appointmentGateway,
        final AppointmentEventPublisher eventPublisher
    ) {
        this.appointmentGateway = appointmentGateway;
        this.eventPublisher = eventPublisher;
    }

    public void execute(final UUID uuid) {
        Objects.requireNonNull(uuid);

        final Appointment currentAppointment = appointmentGateway.findByUuid(uuid)
            .orElseThrow(() -> new EntityNotFoundException("Appointment", uuid.toString()));

        final Appointment deletedAppointment = new Appointment(
            currentAppointment.getId(),
            currentAppointment.getUuid(),
            currentAppointment.getPatientId(),
            currentAppointment.getDoctorId(),
            currentAppointment.getAppointmentDateTime(),
            currentAppointment.getFullname(),
            currentAppointment.getEmail(),
            AppointmentStatus.CANCELLED,
            currentAppointment.getCreatedAt(),
            Instant.now(),
            Instant.now()
        );

        final Appointment savedAppointment = appointmentGateway.save(deletedAppointment);
        eventPublisher.publishNotificationEvent(
            savedAppointment.getUuid(),
            new AppointmentScheduledNotificationEvent(
                savedAppointment.getPatientId(),
                savedAppointment.getDoctorId(),
                savedAppointment.getFullname(),
                savedAppointment.getEmail(),
                AppointmentNotificationBodyFactory.deleted(),
                savedAppointment.getAppointmentDateTime()
            )
        );
        eventPublisher.publishHistoryEvent(
            savedAppointment.getUuid(),
            new AppointmentHistoryEvent(
                AppointmentHistoryEventType.DELETED,
                savedAppointment.getPatientId(),
                savedAppointment.getDoctorId(),
                savedAppointment.getFullname(),
                savedAppointment.getEmail(),
                savedAppointment.getAppointmentDateTime()
            )
        );
    }
}
