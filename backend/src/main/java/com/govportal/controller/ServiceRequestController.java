package com.govportal.controller;

import com.govportal.dto.CreateServiceRequestDto;
import com.govportal.dto.ServiceRequestDto;
import com.govportal.dto.UpdateStatusDto;
import com.govportal.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CITIZEN', 'CASE_WORKER')")
    public ResponseEntity<List<ServiceRequestDto>> listRequests(Authentication authentication) {
        return ResponseEntity.ok(service.listRequests(authentication));
    }

    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ServiceRequestDto> createRequest(@Valid @RequestBody CreateServiceRequestDto dto,
                                                             Authentication authentication) {
        ServiceRequestDto created = service.createRequest(dto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER')")
    public ResponseEntity<ServiceRequestDto> updateStatus(@PathVariable String id,
                                                            @Valid @RequestBody UpdateStatusDto dto,
                                                            Authentication authentication) {
        return ResponseEntity.ok(service.updateStatus(id, dto, authentication));
    }
}
