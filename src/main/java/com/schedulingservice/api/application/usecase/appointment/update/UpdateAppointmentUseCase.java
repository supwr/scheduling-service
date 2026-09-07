package com.schedulingservice.api.application.usecase.appointment.update;

import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;

import java.time.Instant;
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
}
