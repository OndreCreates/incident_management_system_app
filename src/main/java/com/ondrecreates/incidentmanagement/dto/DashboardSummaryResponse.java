package com.ondrecreates.incidentmanagement.dto;

public record DashboardSummaryResponse(
        long activeCount,
        long criticalCount,
        long breachedCount
) {
}
