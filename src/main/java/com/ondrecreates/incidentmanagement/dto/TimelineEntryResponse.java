package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.EventType;
import com.ondrecreates.incidentmanagement.domain.IncidentComment;
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
        boolean commentEdited,
        boolean commentDeleted,
        String actorUserId,
        String note,
        Instant createdAt
) {

    public static TimelineEntryResponse from(IncidentTimelineEntry entry) {
        IncidentComment comment = entry.getComment();
        return new TimelineEntryResponse(
                entry.getId(),
                entry.getEventType(),
                entry.getFromStatus(),
                entry.getToStatus(),
                comment != null ? comment.getId() : null,
                comment != null && !comment.isDeleted() ? comment.getContent() : null,
                comment != null && comment.isEdited(),
                comment != null && comment.isDeleted(),
                entry.getActorUserId(),
                entry.getNote(),
                entry.getCreatedAt()
        );
    }
}
