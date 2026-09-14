package com.govportal.dto;

import com.govportal.domain.RequestStatus;
import com.govportal.domain.ServiceRequest;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ServiceRequestDto(
        String id,
        String citizenUsername,
        String category,
        String description,
        String location,
        RequestStatus status,
        Instant createdAt,
        Instant updatedAt,
        String lastUpdatedByUsername
) {
    public static ServiceRequestDto from(ServiceRequest entity) {
        return ServiceRequestDto.builder()
                .id(entity.getId())
                .citizenUsername(entity.getCitizenUsername())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastUpdatedByUsername(entity.getLastUpdatedByUsername())
                .build();
    }
}
