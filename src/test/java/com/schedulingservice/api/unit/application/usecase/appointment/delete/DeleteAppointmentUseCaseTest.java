package com.schedulingservice.api.unit.application.usecase.appointment.delete;

import com.schedulingservice.api.application.dto.event.AppointmentHistoryEvent;
import com.schedulingservice.api.application.dto.event.AppointmentHistoryEventType;
import com.schedulingservice.api.application.dto.event.AppointmentNotificationBodyFactory;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.application.usecase.appointment.delete.DeleteAppointmentUseCase;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    private DeleteAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteAppointmentUseCase(appointmentGateway, eventPublisher);
    }

    @Test
    void executeShouldSoftDeleteAppointment() {
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
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.of(current));
        when(appointmentGateway.save(any(Appointment.class))).thenReturn(current);

        assertDoesNotThrow(() -> useCase.execute(uuid));
        final ArgumentCaptor<AppointmentScheduledNotificationEvent> notificationCaptor = ArgumentCaptor.forClass(AppointmentScheduledNotificationEvent.class);
        final ArgumentCaptor<AppointmentHistoryEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentHistoryEvent.class);
        verify(eventPublisher).publishNotificationEvent(org.mockito.ArgumentMatchers.eq(current.getUuid()), notificationCaptor.capture());
        verify(eventPublisher).publishHistoryEvent(org.mockito.ArgumentMatchers.eq(current.getUuid()), eventCaptor.capture());
        assertEquals(AppointmentNotificationBodyFactory.deleted(), notificationCaptor.getValue().body());
        assertEquals(AppointmentHistoryEventType.DELETED, eventCaptor.getValue().type());
    }

    @Test
    void executeShouldThrowWhenMissing() {
        final UUID uuid = UUID.randomUUID();
        when(appointmentGateway.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(uuid));
    }
}
