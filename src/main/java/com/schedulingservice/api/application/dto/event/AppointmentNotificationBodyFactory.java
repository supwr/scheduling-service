package com.schedulingservice.api.application.dto.event;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class AppointmentNotificationBodyFactory {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private AppointmentNotificationBodyFactory() {
    }

    public static String created(final OffsetDateTime appointmentDateTime) {
        return "Um novo agendamento foi feito para " + format(appointmentDateTime) + ".";
    }

    public static String updated(final OffsetDateTime appointmentDateTime) {
        return "Seu agendamento foi atualizado para " + format(appointmentDateTime) + ".";
    }

    public static String deleted() {
        return "Seu agendamento foi removido.";
    }

    private static String format(final OffsetDateTime appointmentDateTime) {
        return appointmentDateTime.format(DATE_TIME_FORMATTER);
    }
}
