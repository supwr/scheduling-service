package com.schedulingservice.api.unit.application.usecase.appointment.get;

import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.application.usecase.appointment.get.GetAppointmentUseCase;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private GetAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAppointmentUseCase(appointmentGateway);
    }

    @Test
    void executeShouldReturnAppointment() {
        final UUID uuid = UUID.randomUUID();
        final Appointment appointment = new Appointment(
            1L,
            uuid,
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
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.of(appointment));

        assertEquals(uuid, useCase.execute(uuid).getUuid());
    }

    @Test
    void executeShouldThrowWhenMissing() {
        final UUID uuid = UUID.randomUUID();
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(uuid));
    }
}
