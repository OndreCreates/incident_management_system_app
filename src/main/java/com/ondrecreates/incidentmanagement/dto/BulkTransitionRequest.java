package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Status;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkTransitionRequest(
        @NotEmpty List<Long> incidentIds,
        @NotNull Status targetStatus,
        String note
) {
}
