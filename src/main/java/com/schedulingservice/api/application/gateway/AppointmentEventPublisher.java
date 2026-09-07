package com.schedulingservice.api.application.gateway;

import com.schedulingservice.api.application.dto.event.AppointmentScheduledEvent;
import com.schedulingservice.api.application.dto.event.AppointmentScheduledNotificationEvent;

public interface AppointmentEventPublisher {

    void publishNotificationEvent(AppointmentScheduledNotificationEvent event);

    void publishHistoryEvent(AppointmentScheduledEvent event);
}
