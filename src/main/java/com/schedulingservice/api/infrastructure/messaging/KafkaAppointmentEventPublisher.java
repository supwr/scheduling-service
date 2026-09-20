package com.schedulingservice.api.infrastructure.messaging;

import com.schedulingservice.api.application.dto.event.AppointmentHistoryEvent;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class KafkaAppointmentEventPublisher implements AppointmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String notificationTopic;
    private final String appointmentScheduledTopic;
    private final String sourceService;

    public KafkaAppointmentEventPublisher(
        final KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${app.kafka.topics.notification}") final String notificationTopic,
        @Value("${app.kafka.topics.appointment-scheduled}") final String appointmentScheduledTopic,
        @Value("${app.kafka.source-service}") final String sourceService
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationTopic = notificationTopic;
        this.appointmentScheduledTopic = appointmentScheduledTopic;
        this.sourceService = sourceService;
    }

    @Override
    public void publishNotificationEvent(final UUID resourceUuid, final AppointmentScheduledNotificationEvent event) {
        kafkaTemplate.send(buildRecord(notificationTopic, resourceUuid, event.patientId().toString(), event));
    }

    @Override
    public void publishHistoryEvent(final UUID resourceUuid, final AppointmentHistoryEvent event) {
        kafkaTemplate.send(buildRecord(appointmentScheduledTopic, resourceUuid, event.patientId().toString(), event));
    }

    private ProducerRecord<String, Object> buildRecord(
        final String topic,
        final UUID resourceUuid,
        final String partitionKey,
        final Object payload
    ) {
        final ProducerRecord<String, Object> record = new ProducerRecord<>(topic, partitionKey, payload);
        record.headers()
            .add(new RecordHeader("X-Idempotency-Key", resourceUuid.toString().getBytes(StandardCharsets.UTF_8)))
            .add(new RecordHeader("X-Source-Service", sourceService.getBytes(StandardCharsets.UTF_8)));
        return record;
    }
}
