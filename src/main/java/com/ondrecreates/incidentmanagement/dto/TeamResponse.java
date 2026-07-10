package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Team;
import java.time.Instant;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        List<String> memberEmails,
        Instant createdAt
) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getMembers().stream().map(m -> m.getUserEmail()).toList(),
                team.getCreatedAt()
        );
    }
}
