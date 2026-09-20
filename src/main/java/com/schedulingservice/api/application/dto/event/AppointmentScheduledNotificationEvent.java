package com.schedulingservice.api.application.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentScheduledNotificationEvent(
    UUID patientId,
    UUID doctorId,
    String fullname,
    String email,
    String body,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
    OffsetDateTime appointmentDateTime
) {
}
