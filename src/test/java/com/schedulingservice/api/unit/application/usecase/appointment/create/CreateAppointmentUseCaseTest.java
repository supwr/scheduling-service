package com.schedulingservice.api.unit.application.usecase.appointment.create;

import com.schedulingservice.api.application.dto.event.AppointmentHistoryEvent;
import com.schedulingservice.api.application.dto.event.AppointmentHistoryEventType;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.application.usecase.appointment.create.CreateAppointmentUseCase;
import com.schedulingservice.api.domain.exception.ConflictException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    private CreateAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateAppointmentUseCase(appointmentGateway, eventPublisher);
    }

    @Test
    void executeShouldPersistAndPublishEvents() {
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
        final Appointment saved = new Appointment(
            1L,
            UUID.randomUUID(),
            input.getPatientId(),
            input.getDoctorId(),
            input.getAppointmentDateTime(),
            input.getFullname(),
            input.getEmail(),
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );

        when(appointmentGateway.save(any(Appointment.class))).thenReturn(saved);
        when(appointmentGateway.findActiveAppointmentsByPatientId(input.getPatientId())).thenReturn(List.of());

        final Appointment result = useCase.execute(input);

        assertNotNull(result);
        assertEquals(saved.getUuid(), result.getUuid());

        final ArgumentCaptor<AppointmentScheduledNotificationEvent> notificationCaptor = ArgumentCaptor.forClass(AppointmentScheduledNotificationEvent.class);
        final ArgumentCaptor<AppointmentHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AppointmentHistoryEvent.class);
        verify(eventPublisher).publishNotificationEvent(org.mockito.ArgumentMatchers.eq(saved.getUuid()), notificationCaptor.capture());
        verify(eventPublisher).publishHistoryEvent(org.mockito.ArgumentMatchers.eq(saved.getUuid()), historyCaptor.capture());
        assertEquals(input.getPatientId(), notificationCaptor.getValue().patientId());
        assertEquals(AppointmentHistoryEventType.SCHEDULED, historyCaptor.getValue().type());
        assertEquals(input.getDoctorId(), historyCaptor.getValue().doctorId());
    }

    @Test
    void executeShouldFailWhenActiveConflictExists() {
        final UUID patientId = UUID.randomUUID();
        final OffsetDateTime appointmentDateTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        final Appointment input = new Appointment(
            null,
            null,
            patientId,
            UUID.randomUUID(),
            appointmentDateTime,
            "John Doe",
            "john@example.com",
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        );

        when(appointmentGateway.findActiveAppointmentsByPatientId(patientId)).thenReturn(List.of(
            new Appointment(
                1L,
                UUID.randomUUID(),
                patientId,
                UUID.randomUUID(),
                appointmentDateTime.minusHours(3).minusMinutes(30),
                "Jane Smith",
                "jane@example.com",
                AppointmentStatus.SCHEDULED,
                Instant.now(),
                Instant.now(),
                null
            )
        ));

        assertThrows(ConflictException.class, () -> useCase.execute(input));
    }
}
