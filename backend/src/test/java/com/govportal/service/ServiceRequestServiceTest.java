package com.govportal.service;

import com.govportal.domain.RequestStatus;
import com.govportal.domain.ServiceRequest;
import com.govportal.dto.CreateServiceRequestDto;
import com.govportal.dto.ServiceRequestDto;
import com.govportal.dto.UpdateStatusDto;
import com.govportal.event.RequestStatusChangedEvent;
import com.govportal.exception.ResourceNotFoundException;
import com.govportal.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository repository;

    @Mock
    private KafkaTemplate<String, RequestStatusChangedEvent> kafkaTemplate;

    @InjectMocks
    private ServiceRequestService service;

    private Authentication citizenAuth;
    private Authentication caseWorkerAuth;

    @BeforeEach
    void setUp() {
        citizenAuth = jwtAuth("citizen-sub", "jane.citizen", "ROLE_CITIZEN");
        caseWorkerAuth = jwtAuth("worker-sub", "worker.bob", "ROLE_CASE_WORKER");
    }

    private Authentication jwtAuth(String sub, String username, String role) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", sub)
                .claim("preferred_username", username)
                .build();
        return new UsernamePasswordAuthenticationToken(jwt, null, List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    void createRequest_savesEntityAndPublishesEvent() {
        CreateServiceRequestDto dto = new CreateServiceRequestDto("Pothole", "Large pothole on Main St", "Main St");
        when(repository.save(any(ServiceRequest.class))).thenAnswer(invocation -> {
            ServiceRequest req = invocation.getArgument(0);
            req.setId("generated-id");
            req.setCreatedAt(Instant.now());
            req.setUpdatedAt(Instant.now());
            return req;
        });

        ServiceRequestDto result = service.createRequest(dto, citizenAuth);

        assertThat(result.id()).isEqualTo("generated-id");
        assertThat(result.status()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(result.citizenUsername()).isEqualTo("jane.citizen");
        verify(kafkaTemplate).send(eq("request-status-changed"), anyString(), any(RequestStatusChangedEvent.class));
    }

    @Test
    void listRequests_citizenSeesOnlyOwnRequests() {
        when(repository.findByCitizenSubOrderByCreatedAtDesc("citizen-sub"))
                .thenReturn(List.of(buildRequest("req-1", "citizen-sub")));

        List<ServiceRequestDto> results = service.listRequests(citizenAuth);

        assertThat(results).hasSize(1);
        verify(repository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void listRequests_caseWorkerSeesAllRequests() {
        when(repository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(buildRequest("req-1", "a"), buildRequest("req-2", "b")));

        List<ServiceRequestDto> results = service.listRequests(caseWorkerAuth);

        assertThat(results).hasSize(2);
        verify(repository, never()).findByCitizenSubOrderByCreatedAtDesc(anyString());
    }

    @Test
    void updateStatus_notFound_throws() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus("missing", new UpdateStatusDto(RequestStatus.APPROVED), caseWorkerAuth))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_updatesAndPublishesEvent() {
        ServiceRequest existing = buildRequest("req-1", "citizen-sub");
        when(repository.findById("req-1")).thenReturn(Optional.of(existing));
        when(repository.save(any(ServiceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceRequestDto result = service.updateStatus("req-1", new UpdateStatusDto(RequestStatus.APPROVED), caseWorkerAuth);

        assertThat(result.status()).isEqualTo(RequestStatus.APPROVED);
        assertThat(result.lastUpdatedByUsername()).isEqualTo("worker.bob");
        verify(kafkaTemplate).send(eq("request-status-changed"), eq("req-1"), any(RequestStatusChangedEvent.class));
    }

    private ServiceRequest buildRequest(String id, String citizenSub) {
        return ServiceRequest.builder()
                .id(id)
                .citizenSub(citizenSub)
                .citizenUsername("jane.citizen")
                .category("Pothole")
                .description("desc")
                .status(RequestStatus.SUBMITTED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
