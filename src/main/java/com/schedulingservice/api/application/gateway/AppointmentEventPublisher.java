package com.schedulingservice.api.application.gateway;

import com.schedulingservice.api.application.dto.event.AppointmentScheduledEvent;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;

import java.util.UUID;

public interface AppointmentEventPublisher {

    void publishNotificationEvent(UUID resourceUuid, AppointmentScheduledNotificationEvent event);

    void publishHistoryEvent(UUID resourceUuid, AppointmentScheduledEvent event);
}
