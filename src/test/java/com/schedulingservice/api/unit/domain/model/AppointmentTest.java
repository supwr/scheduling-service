package com.schedulingservice.api.unit.domain.model;

import com.schedulingservice.api.domain.exception.ValidationException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentTest {

    @Test
    void validateShouldPassForValidAppointment() {
        final Appointment appointment = new Appointment(
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(1),
            "John Doe",
            "john@example.com",
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );

        assertDoesNotThrow(appointment::validate);
    }

    @Test
    void validateShouldFailWhenPatientIsMissing() {
        final Appointment appointment = new Appointment(
            null,
            UUID.randomUUID(),
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(1),
            "John Doe",
            "john@example.com",
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );

        assertThrows(ValidationException.class, appointment::validate);
    }
}
