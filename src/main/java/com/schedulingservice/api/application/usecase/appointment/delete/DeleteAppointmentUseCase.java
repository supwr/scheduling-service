package com.schedulingservice.api.application.usecase.appointment.delete;

import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class DeleteAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;

    public DeleteAppointmentUseCase(final AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
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

        appointmentGateway.save(deletedAppointment);
    }
}
