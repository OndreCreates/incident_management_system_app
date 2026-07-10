package com.ondrecreates.incidentmanagement.dto;

import java.util.List;

public record DashboardAnalyticsResponse(
        /** null when no incident has ever been resolved yet. */
        Double avgResolutionMinutes,
        /** 0-100, null when no incident is terminal yet. */
        Double slaComplianceRate,
        List<DailyCountResponse> createdPerDay
) {
}
