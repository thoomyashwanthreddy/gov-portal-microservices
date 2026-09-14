package com.govportal.service;

import com.govportal.config.KafkaConfig;
import com.govportal.domain.RequestStatus;
import com.govportal.domain.ServiceRequest;
import com.govportal.dto.CreateServiceRequestDto;
import com.govportal.dto.ServiceRequestDto;
import com.govportal.dto.UpdateStatusDto;
import com.govportal.event.RequestStatusChangedEvent;
import com.govportal.exception.ResourceNotFoundException;
import com.govportal.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository repository;
    private final KafkaTemplate<String, RequestStatusChangedEvent> kafkaTemplate;

    /**
     * Case-workers see every request; citizens only ever see their own.
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestDto> listRequests(Authentication authentication) {
        if (hasRole(authentication, "ROLE_CASE_WORKER")) {
            return repository.findAllByOrderByCreatedAtDesc().stream()
                    .map(ServiceRequestDto::from)
                    .toList();
        }
        String sub = subjectOf(authentication);
        return repository.findByCitizenSubOrderByCreatedAtDesc(sub).stream()
                .map(ServiceRequestDto::from)
                .toList();
    }

    @Transactional
    public ServiceRequestDto createRequest(CreateServiceRequestDto dto, Authentication authentication) {
        ServiceRequest request = ServiceRequest.builder()
                .citizenSub(subjectOf(authentication))
                .citizenUsername(usernameOf(authentication))
                .category(dto.category())
                .description(dto.description())
                .location(dto.location())
                .status(RequestStatus.SUBMITTED)
                .build();

        ServiceRequest saved = repository.save(request);
        publishStatusChange(saved, null, saved.getStatus(), usernameOf(authentication));
        log.info("Service request created id={} category={} citizen={}",
                saved.getId(), saved.getCategory(), saved.getCitizenUsername());
        return ServiceRequestDto.from(saved);
    }

    @Transactional
    public ServiceRequestDto updateStatus(String id, UpdateStatusDto dto, Authentication authentication) {
        ServiceRequest request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found: " + id));

        RequestStatus previous = request.getStatus();
        request.setStatus(dto.status());
        request.setLastUpdatedByUsername(usernameOf(authentication));
        ServiceRequest saved = repository.save(request);

        publishStatusChange(saved, previous, saved.getStatus(), usernameOf(authentication));
        log.info("Service request status changed id={} {} -> {} by={}",
                saved.getId(), previous, saved.getStatus(), usernameOf(authentication));
        return ServiceRequestDto.from(saved);
    }

    private void publishStatusChange(ServiceRequest request, RequestStatus previous, RequestStatus next, String changedBy) {
        RequestStatusChangedEvent event = RequestStatusChangedEvent.of(
                request.getId(), request.getCitizenUsername(), request.getCategory(),
                previous, next, changedBy
        );
        kafkaTemplate.send(KafkaConfig.REQUEST_STATUS_CHANGED_TOPIC, request.getId(), event);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private String subjectOf(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return authentication.getName();
    }

    private String usernameOf(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String preferred = jwt.getClaimAsString("preferred_username");
            return preferred != null ? preferred : authentication.getName();
        }
        return authentication.getName();
    }
}
