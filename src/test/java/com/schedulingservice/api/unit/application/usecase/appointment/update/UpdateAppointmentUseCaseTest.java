package com.schedulingservice.api.unit.application.usecase.appointment.update;

import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.application.usecase.appointment.update.UpdateAppointmentUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private UpdateAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAppointmentUseCase(appointmentGateway);
    }

    @Test
    void executeShouldUpdateAppointment() {
        final UUID uuid = UUID.randomUUID();
        final Appointment current = new Appointment(
            1L,
            uuid,
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(1),
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );
        final Appointment updated = new Appointment(
            1L,
            uuid,
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(2),
            AppointmentStatus.SCHEDULED,
            current.getCreatedAt(),
            Instant.now(),
            null
        );
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.of(current));
        when(appointmentGateway.save(any(Appointment.class))).thenReturn(updated);

        assertEquals(updated.getUuid(), useCase.execute(uuid, updated).getUuid());
    }

    @Test
    void executeShouldThrowWhenMissing() {
        final UUID uuid = UUID.randomUUID();
        final Appointment input = new Appointment(
            null,
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(1),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        );
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(uuid, input));
    }
}
