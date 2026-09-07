package com.schedulingservice.api.application.usecase.appointment.get;

import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.model.Appointment;

import java.util.Objects;
import java.util.UUID;

public class GetAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;

    public GetAppointmentUseCase(final AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public Appointment execute(final UUID uuid) {
        Objects.requireNonNull(uuid);
        return appointmentGateway.findByUuid(uuid)
            .orElseThrow(() -> new EntityNotFoundException("Appointment", uuid.toString()));
    }
}
