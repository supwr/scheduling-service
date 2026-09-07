package com.schedulingservice.api.unit.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulingservice.api.application.usecase.appointment.create.CreateAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.delete.DeleteAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.get.GetAppointmentUseCase;
import com.schedulingservice.api.application.usecase.appointment.update.UpdateAppointmentUseCase;
import com.schedulingservice.api.domain.model.Appointment;
import com.schedulingservice.api.domain.model.AppointmentStatus;
import com.schedulingservice.api.infrastructure.web.controller.AppointmentController;
import com.schedulingservice.api.infrastructure.web.exception.GlobalExceptionHandler;
import com.schedulingservice.api.infrastructure.web.mapper.AppointmentMapper;
import com.schedulingservice.api.model.AppointmentRequest;
import com.schedulingservice.api.model.AppointmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerUnitTest {

    @Mock
    private CreateAppointmentUseCase createAppointmentUseCase;

    @Mock
    private GetAppointmentUseCase getAppointmentUseCase;

    @Mock
    private UpdateAppointmentUseCase updateAppointmentUseCase;

    @Mock
    private DeleteAppointmentUseCase deleteAppointmentUseCase;

    @Mock
    private AppointmentMapper appointmentMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AppointmentController(
            createAppointmentUseCase,
            getAppointmentUseCase,
            updateAppointmentUseCase,
            deleteAppointmentUseCase,
            appointmentMapper
        )).setControllerAdvice(new GlobalExceptionHandler()).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createAppointmentShouldReturnCreated() throws Exception {
        final AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(UUID.randomUUID());
        request.setDoctorId(UUID.randomUUID());
        request.setAppointmentDateTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));

        final Appointment domain = new Appointment(
            1L,
            UUID.randomUUID(),
            request.getPatientId(),
            request.getDoctorId(),
            request.getAppointmentDateTime(),
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );
        final AppointmentResponse response = new AppointmentResponse();
        response.setUuid(domain.getUuid());
        response.setPatientId(domain.getPatientId());
        response.setDoctorId(domain.getDoctorId());
        response.setAppointmentDateTime(domain.getAppointmentDateTime());
        response.setStatus(com.schedulingservice.api.model.AppointmentStatus.SCHEDULED);

        when(appointmentMapper.toDomain(any(AppointmentRequest.class))).thenReturn(new Appointment(
            null,
            null,
            request.getPatientId(),
            request.getDoctorId(),
            request.getAppointmentDateTime(),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        ));
        when(createAppointmentUseCase.execute(any(Appointment.class))).thenReturn(domain);
        when(appointmentMapper.toResponse(domain)).thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments")
                .header("X-User-ID", UUID.randomUUID())
                .header("X-User-Roles", "DOCTOR")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.uuid").exists());
    }

    @Test
    void getAppointmentShouldReturnOk() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Appointment domain = new Appointment(
            1L,
            uuid,
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(1),
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );
        final AppointmentResponse response = new AppointmentResponse();
        response.setUuid(uuid);
        when(getAppointmentUseCase.execute(uuid)).thenReturn(domain);
        when(appointmentMapper.toResponse(domain)).thenReturn(response);

        mockMvc.perform(get("/api/v1/appointments/{uuid}", uuid)
                .header("X-User-ID", UUID.randomUUID()))
            .andExpect(status().isOk());
    }

    @Test
    void updateAppointmentShouldReturnOk() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(UUID.randomUUID());
        request.setDoctorId(UUID.randomUUID());
        request.setAppointmentDateTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));

        final Appointment domain = new Appointment(
            1L,
            uuid,
            request.getPatientId(),
            request.getDoctorId(),
            request.getAppointmentDateTime(),
            AppointmentStatus.SCHEDULED,
            Instant.now(),
            Instant.now(),
            null
        );
        final AppointmentResponse response = new AppointmentResponse();
        response.setUuid(uuid);
        when(appointmentMapper.toDomain(any(AppointmentRequest.class))).thenReturn(new Appointment(
            null,
            null,
            request.getPatientId(),
            request.getDoctorId(),
            request.getAppointmentDateTime(),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            null
        ));
        when(updateAppointmentUseCase.execute(any(UUID.class), any(Appointment.class))).thenReturn(domain);
        when(appointmentMapper.toResponse(domain)).thenReturn(response);

        mockMvc.perform(put("/api/v1/appointments/{uuid}", uuid)
                .header("X-User-ID", UUID.randomUUID())
                .header("X-User-Roles", "NURSE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void deleteAppointmentShouldReturnNoContent() throws Exception {
        final UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/appointments/{uuid}", uuid)
                .header("X-User-ID", UUID.randomUUID())
                .header("X-User-Roles", "ADMIN"))
            .andExpect(status().isNoContent());
    }
}
