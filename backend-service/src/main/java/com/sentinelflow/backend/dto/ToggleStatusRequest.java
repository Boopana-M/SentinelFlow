package com.sentinelflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ToggleStatusRequest(
        @NotBlank(message = "Status cannot be blank")
        @Pattern(regexp = "^(UP|DOWN)$", message = "Status must be UP or DOWN")
        String status
) {
}
