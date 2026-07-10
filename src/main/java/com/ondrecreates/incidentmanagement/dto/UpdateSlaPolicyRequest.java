package com.ondrecreates.incidentmanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateSlaPolicyRequest(
        @Min(1) int slaMinutes,
        @Min(1) @Max(100) int nearBreachPercentage
) {
}
