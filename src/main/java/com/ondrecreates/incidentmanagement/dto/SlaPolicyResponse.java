package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.SlaPolicy;
import java.time.Instant;

public record SlaPolicyResponse(
        Severity severity,
        int slaMinutes,
        int nearBreachPercentage,
        Instant updatedAt
) {

    public static SlaPolicyResponse from(SlaPolicy policy) {
        return new SlaPolicyResponse(policy.getSeverity(), policy.getSlaMinutes(), policy.getNearBreachPercentage(),
                policy.getUpdatedAt());
    }
}
