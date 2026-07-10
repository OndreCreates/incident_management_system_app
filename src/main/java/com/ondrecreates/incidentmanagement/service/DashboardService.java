package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.DailyCountResponse;
import com.ondrecreates.incidentmanagement.dto.DashboardAnalyticsResponse;
import com.ondrecreates.incidentmanagement.dto.DashboardSummaryResponse;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final int TREND_WINDOW_DAYS = 14;

    private final IncidentRepository incidentRepository;

    public DashboardService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public DashboardSummaryResponse getSummary() {
        long activeCount = incidentRepository.countByStatusNotIn(Status.TERMINAL_STATUSES);
        long criticalCount = incidentRepository.countBySeverityAndStatusNotIn(Severity.CRITICAL,
                Status.TERMINAL_STATUSES);
        long breachedCount = incidentRepository.countBySlaBreachedTrue();
        return new DashboardSummaryResponse(activeCount, criticalCount, breachedCount);
    }

    public DashboardAnalyticsResponse getAnalytics() {
        Double avgResolutionMinutes = incidentRepository.averageResolutionMinutes();

        long terminalCount = incidentRepository.countByStatusIn(Status.TERMINAL_STATUSES);
        long compliantCount = incidentRepository.countByStatusInAndSlaBreachedFalse(Status.TERMINAL_STATUSES);
        Double slaComplianceRate = terminalCount == 0 ? null : (compliantCount * 100.0 / terminalCount);

        Instant since = Instant.now().minus(Duration.ofDays(TREND_WINDOW_DAYS - 1));
        List<DailyCountResponse> createdPerDay = incidentRepository.countCreatedPerDaySince(since).stream()
                .map(row -> new DailyCountResponse(row[0].toString(), ((Number) row[1]).longValue()))
                .toList();

        return new DashboardAnalyticsResponse(avgResolutionMinutes, slaComplianceRate, createdPerDay);
    }
}
