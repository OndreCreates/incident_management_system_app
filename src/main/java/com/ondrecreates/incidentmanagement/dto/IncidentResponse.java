package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Priority;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import java.time.Instant;

public record IncidentResponse(
        Long id,
        String title,
        String description,
        Severity severity,
        Priority priority,
        Status status,
        String assignedUserId,
        Long assignedTeamId,
        Instant slaDeadline,
        boolean slaBreached,
        String rootCause,
        String resolution,
        String createdBy,
        Instant createdAt,
        Instant updatedAt
) {

    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getPriority(),
                incident.getStatus(),
                incident.getAssignedUserId(),
                // .getId() on a lazy proxy doesn't force initialization -- safe outside a
                // transaction (open-in-view is false). See IncidentTimelineRepository for
                // the pattern used where the full related entity actually needs loading.
                incident.getAssignedTeam() != null ? incident.getAssignedTeam().getId() : null,
                incident.getSlaDeadline(),
                incident.isSlaBreached(),
                incident.getRootCause(),
                incident.getResolution(),
                incident.getCreatedBy(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}
