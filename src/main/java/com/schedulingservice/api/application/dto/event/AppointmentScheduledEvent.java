package com.schedulingservice.api.application.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentScheduledEvent(
    UUID patientId,
    UUID doctorId,
    OffsetDateTime appointmentDateTime
) {
}
