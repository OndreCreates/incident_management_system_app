package com.ondrecreates.incidentmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record PostmortemRequest(
        @NotBlank String impact,
        @NotBlank String rootCause,
        @NotBlank String resolution,
        @NotBlank String lessonsLearned,
        String actionItems
) {
}
