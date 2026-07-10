package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.EventType;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import com.ondrecreates.incidentmanagement.domain.Status;
import java.time.Instant;

public record TimelineEntryResponse(
        Long id,
        EventType eventType,
        Status fromStatus,
        Status toStatus,
        Long commentId,
        String commentContent,
        String actorUserId,
        String note,
        Instant createdAt
) {

    public static TimelineEntryResponse from(IncidentTimelineEntry entry) {
        return new TimelineEntryResponse(
                entry.getId(),
                entry.getEventType(),
                entry.getFromStatus(),
                entry.getToStatus(),
                entry.getComment() != null ? entry.getComment().getId() : null,
                entry.getComment() != null ? entry.getComment().getContent() : null,
                entry.getActorUserId(),
                entry.getNote(),
                entry.getCreatedAt()
        );
    }
}
