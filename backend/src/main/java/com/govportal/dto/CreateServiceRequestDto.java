package com.govportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateServiceRequestDto(
        @NotBlank(message = "category is required")
        String category,

        @NotBlank(message = "description is required")
        @Size(max = 2000, message = "description must be 2000 characters or fewer")
        String description,

        String location
) {
}
