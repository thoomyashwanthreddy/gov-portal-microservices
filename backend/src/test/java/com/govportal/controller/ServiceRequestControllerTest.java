package com.govportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govportal.config.SecurityConfig;
import com.govportal.domain.RequestStatus;
import com.govportal.dto.CreateServiceRequestDto;
import com.govportal.dto.ServiceRequestDto;
import com.govportal.dto.UpdateStatusDto;
import com.govportal.service.ServiceRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceRequestController.class)
@Import(SecurityConfig.class)
class ServiceRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceRequestService service;

    // Prevents the resource-server auto-config from trying to reach a real
    // Keycloak instance to fetch its JWK set while this slice test boots.
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "CITIZEN")
    void createRequest_asCitizen_succeeds() throws Exception {
        ServiceRequestDto dto = new ServiceRequestDto("id-1", "jane", "Pothole", "desc", "Main St",
                RequestStatus.SUBMITTED, Instant.now(), Instant.now(), null);
        when(service.createRequest(any(), any())).thenReturn(dto);

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateServiceRequestDto("Pothole", "desc", "Main St"))))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CASE_WORKER")
    void createRequest_asCaseWorker_forbidden() throws Exception {
        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateServiceRequestDto("Pothole", "desc", "Main St"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void updateStatus_asCitizen_forbidden() throws Exception {
        mockMvc.perform(patch("/api/requests/{id}", "req-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusDto(RequestStatus.APPROVED))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CASE_WORKER")
    void updateStatus_asCaseWorker_succeeds() throws Exception {
        ServiceRequestDto dto = new ServiceRequestDto("req-1", "jane", "Pothole", "desc", "Main St",
                RequestStatus.APPROVED, Instant.now(), Instant.now(), "worker.bob");
        when(service.updateStatus(any(), any(), any())).thenReturn(dto);

        mockMvc.perform(patch("/api/requests/{id}", "req-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusDto(RequestStatus.APPROVED))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"CITIZEN", "CASE_WORKER"})
    void listRequests_authenticated_ok() throws Exception {
        when(service.listRequests(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk());
    }

    @Test
    void listRequests_unauthenticated_unauthorized() throws Exception {
        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isUnauthorized());
    }
}
