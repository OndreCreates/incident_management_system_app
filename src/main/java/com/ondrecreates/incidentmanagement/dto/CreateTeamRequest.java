package com.ondrecreates.incidentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateTeamRequest(
        @NotBlank String name,
        @NotEmpty List<@NotBlank String> memberEmails
) {
}
