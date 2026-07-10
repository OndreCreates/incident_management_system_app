package com.ondrecreates.incidentmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record AddTeamMemberRequest(
        @NotBlank String userEmail
) {
}
