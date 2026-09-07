package com.schedulingservice.api.application.usecase.appointment.create;

import com.schedulingservice.api.application.dto.event.AppointmentScheduledEvent;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;

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

        final Appointment appointmentToSave = new Appointment(
            null,
            null,
            appointment.getPatientId(),
            appointment.getDoctorId(),
            appointment.getAppointmentDateTime(),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        );

        final Appointment savedAppointment = appointmentGateway.save(appointmentToSave);
        eventPublisher.publishNotificationEvent(
            new AppointmentScheduledNotificationEvent(
                savedAppointment.getPatientId(),
                savedAppointment.getDoctorId(),
                savedAppointment.getAppointmentDateTime()
            )
        );
        eventPublisher.publishHistoryEvent(
            new AppointmentScheduledEvent(
                savedAppointment.getPatientId(),
                savedAppointment.getDoctorId(),
                savedAppointment.getAppointmentDateTime()
            )
        );
        return savedAppointment;
    }
}
