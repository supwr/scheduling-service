package com.schedulingservice.api.unit.application.usecase.appointment.create;

import com.schedulingservice.api.application.dto.event.AppointmentScheduledEvent;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.application.usecase.appointment.create.CreateAppointmentUseCase;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );

        when(appointmentGateway.save(any(Appointment.class))).thenReturn(saved);

        final Appointment result = useCase.execute(input);

        assertNotNull(result);
        assertEquals(saved.getUuid(), result.getUuid());

        final ArgumentCaptor<AppointmentScheduledNotificationEvent> notificationCaptor = ArgumentCaptor.forClass(AppointmentScheduledNotificationEvent.class);
        final ArgumentCaptor<AppointmentScheduledEvent> historyCaptor = ArgumentCaptor.forClass(AppointmentScheduledEvent.class);
        verify(eventPublisher).publishNotificationEvent(notificationCaptor.capture());
        verify(eventPublisher).publishHistoryEvent(historyCaptor.capture());
        assertEquals(input.getPatientId(), notificationCaptor.getValue().patientId());
        assertEquals(input.getDoctorId(), historyCaptor.getValue().doctorId());
    }
}
