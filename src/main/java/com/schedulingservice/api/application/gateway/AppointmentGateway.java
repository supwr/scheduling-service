package com.schedulingservice.api.application.gateway;

import com.schedulingservice.api.domain.model.Appointment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentGateway {

    Appointment save(Appointment appointment);

    Optional<Appointment> findByUuid(UUID uuid);

    List<Appointment> findActiveAppointmentsByPatientId(UUID patientId);
}
