package com.ondrecreates.incidentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkAssignRequest(
        @NotEmpty List<Long> incidentIds,
        @NotBlank String assignedUserId
) {
}
