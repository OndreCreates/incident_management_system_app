package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.IncidentComment;
import java.time.Instant;

public record CommentResponse(
        Long id,
        Long authorUserId,
        String content,
        Instant createdAt
) {

    public static CommentResponse from(IncidentComment comment) {
        return new CommentResponse(comment.getId(), comment.getAuthorUserId(), comment.getContent(),
                comment.getCreatedAt());
    }
}
