package com.schedulingservice.api.infrastructure.messaging;

import com.schedulingservice.api.application.dto.event.AppointmentScheduledEvent;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaAppointmentEventPublisher implements AppointmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String notificationTopic;
    private final String appointmentScheduledTopic;

    public KafkaAppointmentEventPublisher(
        final KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${app.kafka.topics.notification}") final String notificationTopic,
        @Value("${app.kafka.topics.appointment-scheduled}") final String appointmentScheduledTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationTopic = notificationTopic;
        this.appointmentScheduledTopic = appointmentScheduledTopic;
    }

    @Override
    public void publishNotificationEvent(final AppointmentScheduledNotificationEvent event) {
        kafkaTemplate.send(notificationTopic, event.patientId().toString(), event);
    }

    @Override
    public void publishHistoryEvent(final AppointmentScheduledEvent event) {
        kafkaTemplate.send(appointmentScheduledTopic, event.patientId().toString(), event);
    }
}
