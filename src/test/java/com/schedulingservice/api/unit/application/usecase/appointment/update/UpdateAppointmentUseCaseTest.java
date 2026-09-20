package com.schedulingservice.api.unit.application.usecase.appointment.update;

import com.schedulingservice.api.application.dto.event.AppointmentHistoryEvent;
import com.schedulingservice.api.application.dto.event.AppointmentHistoryEventType;
import com.schedulingservice.api.application.dto.event.AppointmentNotificationBodyFactory;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.usecase.appointment.update.UpdateAppointmentUseCase;
import com.schedulingservice.api.domain.exception.ConflictException;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    private UpdateAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAppointmentUseCase(appointmentGateway, eventPublisher);
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
            "John Doe",
            "john@example.com",
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
            "Jane Smith",
            "jane@example.com",
            AppointmentStatus.SCHEDULED,
            current.getCreatedAt(),
            Instant.now(),
            null
        );
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.of(current));
        when(appointmentGateway.findActiveAppointmentsByPatientId(updated.getPatientId())).thenReturn(List.of(current));
        when(appointmentGateway.save(any(Appointment.class))).thenReturn(updated);

        assertEquals(updated.getUuid(), useCase.execute(uuid, updated).getUuid());

        final ArgumentCaptor<AppointmentScheduledNotificationEvent> notificationCaptor = ArgumentCaptor.forClass(AppointmentScheduledNotificationEvent.class);
        final ArgumentCaptor<AppointmentHistoryEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentHistoryEvent.class);
        verify(eventPublisher).publishNotificationEvent(org.mockito.ArgumentMatchers.eq(updated.getUuid()), notificationCaptor.capture());
        verify(eventPublisher).publishHistoryEvent(org.mockito.ArgumentMatchers.eq(updated.getUuid()), eventCaptor.capture());
        assertEquals(AppointmentNotificationBodyFactory.updated(updated.getAppointmentDateTime()), notificationCaptor.getValue().body());
        assertEquals(AppointmentHistoryEventType.UPDATED, eventCaptor.getValue().type());
    }

    @Test
    void executeShouldThrowConflictWhenUpdateCreatesOverlap() {
        final UUID uuid = UUID.randomUUID();
        final UUID patientId = UUID.randomUUID();
        final OffsetDateTime requestedTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        final Appointment current = new Appointment(
            1L,
            uuid,
            patientId,
            UUID.randomUUID(),
            requestedTime.minusHours(1),
            "John Doe",
            "john@example.com",
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );
        final Appointment input = new Appointment(
            null,
            null,
            patientId,
            UUID.randomUUID(),
            requestedTime,
            "Jane Smith",
            "jane@example.com",
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        );
        final Appointment conflictingAppointment = new Appointment(
            2L,
            UUID.randomUUID(),
            patientId,
            UUID.randomUUID(),
            requestedTime.minusHours(3).minusMinutes(30),
            "Bob Johnson",
            "bob@example.com",
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );

        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.of(current));
        when(appointmentGateway.findActiveAppointmentsByPatientId(patientId)).thenReturn(List.of(current, conflictingAppointment));

        assertThrows(ConflictException.class, () -> useCase.execute(uuid, input));
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
            "John Doe",
            "john@example.com",
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        );
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(uuid, input));
    }
}
