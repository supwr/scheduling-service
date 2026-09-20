package com.schedulingservice.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulingservice.api.application.gateway.AppointmentEventPublisher;
import com.schedulingservice.api.model.AppointmentRequest;
import com.schedulingservice.api.model.AppointmentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentEventPublisher appointmentEventPublisher;

    @Test
    void appointmentCrudFlowShouldWork() throws Exception {
        final AppointmentRequest request = new AppointmentRequest();
        final UUID patientId = UUID.randomUUID();
        final UUID doctorId = UUID.randomUUID();
        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
        request.setFullname("John Doe");
        request.setEmail("john@example.com");

        final String createdBody = mockMvc.perform(post("/api/v1/appointments")
                .header("X-User-Roles", "DOCTOR")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.uuid", notNullValue()))
            .andExpect(jsonPath("$.patientId", equalTo(patientId.toString())))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final UUID uuid = UUID.fromString(objectMapper.readTree(createdBody).get("uuid").asText());

        mockMvc.perform(get("/api/v1/appointments/{uuid}", uuid)
                .header("X-User-ID", UUID.randomUUID()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uuid", equalTo(uuid.toString())));

        request.setAppointmentDateTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(2));
        mockMvc.perform(put("/api/v1/appointments/{uuid}", uuid)
                .header("X-User-Roles", "NURSE")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uuid", equalTo(uuid.toString())));

        mockMvc.perform(delete("/api/v1/appointments/{uuid}", uuid)
                .header("X-User-Roles", "ADMIN")
                .header("X-User-ID", UUID.randomUUID()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/appointments/{uuid}", uuid)
                .header("X-User-ID", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void appointmentCreationShouldBeIdempotentForSamePatientAndSchedule() throws Exception {
        final AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(UUID.randomUUID());
        request.setDoctorId(UUID.randomUUID());
        request.setAppointmentDateTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
        request.setFullname("Jane Smith");
        request.setEmail("jane@example.com");

        mockMvc.perform(post("/api/v1/appointments")
                .header("X-User-Roles", "DOCTOR")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/appointments")
                .header("X-User-Roles", "DOCTOR")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void appointmentMutationShouldBeForbiddenWithoutRole() throws Exception {
        final AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(UUID.randomUUID());
        request.setDoctorId(UUID.randomUUID());
        request.setAppointmentDateTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
        request.setFullname("Bob Johnson");
        request.setEmail("bob@example.com");

        mockMvc.perform(post("/api/v1/appointments")
                .header("X-User-Roles", "PATIENT")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void appointmentUpdateShouldRespectConflictWindow() throws Exception {
        final UUID patientId = UUID.randomUUID();
        final UUID doctorId = UUID.randomUUID();
        final OffsetDateTime firstTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).withMinute(0).withSecond(0).withNano(0);
        final OffsetDateTime secondTime = firstTime.plusHours(6);

        final AppointmentRequest firstRequest = new AppointmentRequest();
        firstRequest.setPatientId(patientId);
        firstRequest.setDoctorId(doctorId);
        firstRequest.setAppointmentDateTime(firstTime);
        firstRequest.setFullname("Alice Wilson");
        firstRequest.setEmail("alice@example.com");

        final AppointmentRequest secondRequest = new AppointmentRequest();
        secondRequest.setPatientId(patientId);
        secondRequest.setDoctorId(UUID.randomUUID());
        secondRequest.setAppointmentDateTime(secondTime);
        secondRequest.setFullname("Charlie Brown");
        secondRequest.setEmail("charlie@example.com");

        final String createdBody = mockMvc.perform(post("/api/v1/appointments")
                .header("X-User-Roles", "DOCTOR")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final UUID firstUuid = UUID.fromString(objectMapper.readTree(createdBody).get("uuid").asText());

        mockMvc.perform(post("/api/v1/appointments")
                .header("X-User-Roles", "DOCTOR")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
            .andExpect(status().isCreated());

        firstRequest.setAppointmentDateTime(secondTime.minusHours(3));

        mockMvc.perform(put("/api/v1/appointments/{uuid}", firstUuid)
                .header("X-User-Roles", "NURSE")
                .header("X-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail", equalTo("A patient can only have one non-cancelled appointment within a 4 hour window")));
    }
}
