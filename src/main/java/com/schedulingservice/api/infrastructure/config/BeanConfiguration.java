package com.schedulingservice.api.infrastructure.config;

import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.application.gateway.AppointmentGateway;
import com.schedulingservice.api.application.usecase.appointment.create.CreateAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.delete.DeleteAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.get.GetAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.update.UpdateAppointmentUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateAppointmentUseCase createAppointmentUseCase(
        final AppointmentGateway appointmentGateway,
        final AppointmentEventPublisher eventPublisher
    ) {
        return new CreateAppointmentUseCase(appointmentGateway, eventPublisher);
    }

    @Bean
    public GetAppointmentUseCase getAppointmentUseCase(final AppointmentGateway appointmentGateway) {
        return new GetAppointmentUseCase(appointmentGateway);
    }

    @Bean
    public UpdateAppointmentUseCase updateAppointmentUseCase(final AppointmentGateway appointmentGateway) {
        return new UpdateAppointmentUseCase(appointmentGateway);
    }

    @Bean
    public DeleteAppointmentUseCase deleteAppointmentUseCase(final AppointmentGateway appointmentGateway) {
        return new DeleteAppointmentUseCase(appointmentGateway);
    }
}
