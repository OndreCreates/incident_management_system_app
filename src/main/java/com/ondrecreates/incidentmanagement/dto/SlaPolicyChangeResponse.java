package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.SlaPolicyChange;
import java.time.Instant;

public record SlaPolicyChangeResponse(
        Long id,
        Severity severity,
        int oldSlaMinutes,
        int oldNearBreachPercentage,
        int newSlaMinutes,
        int newNearBreachPercentage,
        String changedBy,
        Instant changedAt
) {

    public static SlaPolicyChangeResponse from(SlaPolicyChange change) {
        return new SlaPolicyChangeResponse(
                change.getId(),
                change.getSeverity(),
                change.getOldSlaMinutes(),
                change.getOldNearBreachPercentage(),
                change.getNewSlaMinutes(),
                change.getNewNearBreachPercentage(),
                change.getChangedBy(),
                change.getChangedAt()
        );
    }
}
