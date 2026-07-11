package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.IncidentComment;
import java.time.Instant;

public record CommentResponse(
        Long id,
        String authorUserId,
        String content,
        boolean edited,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {

    public static CommentResponse from(IncidentComment comment) {
        return new CommentResponse(comment.getId(), comment.getAuthorUserId(),
                comment.isDeleted() ? null : comment.getContent(), comment.isEdited(), comment.isDeleted(),
                comment.getCreatedAt(), comment.getUpdatedAt());
    }
}
