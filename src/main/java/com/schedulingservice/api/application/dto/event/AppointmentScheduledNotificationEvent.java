package com.schedulingservice.api.application.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentScheduledNotificationEvent(
    UUID patientId,
    UUID doctorId,
    OffsetDateTime appointmentDateTime
) {
}
