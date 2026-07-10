package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.IncidentPostmortem;
import java.time.Instant;

public record PostmortemResponse(
        Long id,
        Long incidentId,
        String impact,
        String rootCause,
        String resolution,
        String lessonsLearned,
        String actionItems,
        String authorUserId,
        Instant createdAt,
        Instant updatedAt
) {

    public static PostmortemResponse from(IncidentPostmortem postmortem) {
        return new PostmortemResponse(
                postmortem.getId(),
                postmortem.getIncidentId(),
                postmortem.getImpact(),
                postmortem.getRootCause(),
                postmortem.getResolution(),
                postmortem.getLessonsLearned(),
                postmortem.getActionItems(),
                postmortem.getAuthorUserId(),
                postmortem.getCreatedAt(),
                postmortem.getUpdatedAt()
        );
    }
}
