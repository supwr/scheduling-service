package com.schedulingservice.api.unit.infrastructure.messaging;

import com.schedulingservice.api.application.dto.event.AppointmentHistoryEvent;
import com.schedulingservice.api.application.dto.event.AppointmentHistoryEventType;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.infrastructure.messaging.KafkaAppointmentEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaAppointmentEventPublisherTest {

    @Test
    void publishNotificationEventShouldAddStandardHeadersAndPartitionKey() throws Exception {
        final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        final KafkaAppointmentEventPublisher publisher = new KafkaAppointmentEventPublisher(
            kafkaTemplate,
            "notification-topic",
            "history-topic",
            "scheduling-service"
        );

        final UUID resourceUuid = UUID.randomUUID();
        final UUID patientId = UUID.randomUUID();
        final AppointmentScheduledNotificationEvent event = new AppointmentScheduledNotificationEvent(
            patientId,
            UUID.randomUUID(),
            "John Doe",
            "john@example.com",
            OffsetDateTime.of(2026, 9, 7, 18, 51, 22, 785_000_000, ZoneOffset.ofHours(-3))
        );

        publisher.publishNotificationEvent(resourceUuid, event);

        @SuppressWarnings("unchecked")
        final var captor = org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertEquals(patientId.toString(), captor.getValue().key());
        assertEquals("notification-topic", captor.getValue().topic());
        assertEquals(resourceUuid.toString(), new String(captor.getValue().headers().lastHeader("X-Idempotency-Key").value()));
        assertEquals("scheduling-service", new String(captor.getValue().headers().lastHeader("X-Source-Service").value()));

        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        final String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("\"appointmentDateTime\":\"2026-09-07 18:51:22.785 -0300\""));
    }

    @Test
    void publishHistoryEventShouldAddStandardHeaders() throws Exception {
        final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        final KafkaAppointmentEventPublisher publisher = new KafkaAppointmentEventPublisher(
            kafkaTemplate,
            "notification-topic",
            "history-topic",
            "scheduling-service"
        );

        final UUID resourceUuid = UUID.randomUUID();
        final UUID patientId = UUID.randomUUID();
        final AppointmentHistoryEvent event = new AppointmentHistoryEvent(
            AppointmentHistoryEventType.SCHEDULED,
            patientId,
            UUID.randomUUID(),
            "John Doe",
            "john@example.com",
            OffsetDateTime.of(2026, 9, 7, 18, 51, 22, 785_000_000, ZoneOffset.ofHours(-3))
        );

        publisher.publishHistoryEvent(resourceUuid, event);

        @SuppressWarnings("unchecked")
        final var captor = org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertEquals(patientId.toString(), captor.getValue().key());
        assertEquals("history-topic", captor.getValue().topic());
        assertEquals(AppointmentHistoryEventType.SCHEDULED, ((AppointmentHistoryEvent) captor.getValue().value()).type());

        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        final String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("\"type\":\"SCHEDULED\""));
        assertTrue(json.contains("\"appointmentDateTime\":\"2026-09-07 18:51:22.785 -0300\""));
    }
}
