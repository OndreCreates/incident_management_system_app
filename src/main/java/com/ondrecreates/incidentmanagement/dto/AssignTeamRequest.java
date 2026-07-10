package com.ondrecreates.incidentmanagement.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTeamRequest(
        @NotNull Long teamId
) {
}
