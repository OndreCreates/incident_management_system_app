package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.DashboardSummaryResponse;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

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
}
