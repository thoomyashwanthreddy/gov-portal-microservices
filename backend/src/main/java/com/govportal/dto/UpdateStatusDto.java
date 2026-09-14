package com.govportal.dto;

import com.govportal.domain.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusDto(
        @NotNull(message = "status is required")
        RequestStatus status
) {
}
