package com.schedulingservice.api.infrastructure.web.controller;

import com.schedulingservice.api.AppointmentsApi;
import com.schedulingservice.api.application.usecase.appointment.create.CreateAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.delete.DeleteAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.get.GetAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.update.UpdateAppointmentUseCase;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.infrastructure.web.mapper.AppointmentMapper;
import com.schedulingservice.api.model.AppointmentRequest;
import com.schedulingservice.api.model.AppointmentResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AppointmentController implements AppointmentsApi {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final GetAppointmentUseCase getAppointmentUseCase;
    private final UpdateAppointmentUseCase updateAppointmentUseCase;
    private final DeleteAppointmentUseCase deleteAppointmentUseCase;
    private final AppointmentMapper appointmentMapper;

    public AppointmentController(
        final CreateAppointmentUseCase createAppointmentUseCase,
        final GetAppointmentUseCase getAppointmentUseCase,
        final UpdateAppointmentUseCase updateAppointmentUseCase,
        final DeleteAppointmentUseCase deleteAppointmentUseCase,
        final AppointmentMapper appointmentMapper
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.getAppointmentUseCase = getAppointmentUseCase;
        this.updateAppointmentUseCase = updateAppointmentUseCase;
        this.deleteAppointmentUseCase = deleteAppointmentUseCase;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    @RateLimiter(name = "appointmentLimiter")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> createAppointment(
        @RequestHeader("X-User-ID") final UUID xUserID,
        @RequestHeader("X-User-Roles") final String xUserRoles,
        final AppointmentRequest appointmentRequest
    ) {
        final Appointment createdAppointment = createAppointmentUseCase.execute(appointmentMapper.toDomain(appointmentRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentMapper.toResponse(createdAppointment));
    }

    @Override
    @RateLimiter(name = "appointmentLimiter")
    public ResponseEntity<AppointmentResponse> getAppointmentById(final UUID uuid, @RequestHeader("X-User-ID") final UUID xUserID) {
        return ResponseEntity.ok(appointmentMapper.toResponse(getAppointmentUseCase.execute(uuid)));
    }

    @Override
    @RateLimiter(name = "appointmentLimiter")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> updateAppointment(
        final UUID uuid,
        @RequestHeader("X-User-ID") final UUID xUserID,
        @RequestHeader("X-User-Roles") final String xUserRoles,
        final AppointmentRequest appointmentRequest
    ) {
        final Appointment updatedAppointment = updateAppointmentUseCase.execute(uuid, appointmentMapper.toDomain(appointmentRequest));
        return ResponseEntity.ok(appointmentMapper.toResponse(updatedAppointment));
    }

    @Override
    @RateLimiter(name = "appointmentLimiter")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<Void> deleteAppointment(
        final UUID uuid,
        @RequestHeader("X-User-ID") final UUID xUserID,
        @RequestHeader("X-User-Roles") final String xUserRoles
    ) {
        deleteAppointmentUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }
}
