package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import org.springframework.data.jpa.domain.Specification;

public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> hasStatus(Status status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Incident> hasSeverity(Severity severity) {
        return (root, query, cb) -> severity == null ? null : cb.equal(root.get("severity"), severity);
    }

    public static Specification<Incident> hasAssignedUserId(String assignedUserId) {
        return (root, query, cb) -> assignedUserId == null ? null
                : cb.equal(root.get("assignedUserId"), assignedUserId);
    }

    public static Specification<Incident> hasAssignedTeamId(Long assignedTeamId) {
        return (root, query, cb) -> assignedTeamId == null ? null
                : cb.equal(root.get("assignedTeam").get("id"), assignedTeamId);
    }

    // Case-insensitive substring match on title/description, not a real MySQL FULLTEXT
    // MATCH...AGAINST -- at the incident volumes this portfolio app will ever hold, relevance
    // ranking buys nothing, and a plain Specification predicate stays composable with the
    // other filters below without a native-query escape hatch.
    public static Specification<Incident> matchesQuery(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return null;
            }
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Incident> filter(Status status, Severity severity, String assignedUserId,
                                                   Long assignedTeamId, String q) {
        return Specification.where(hasStatus(status))
                .and(hasSeverity(severity))
                .and(hasAssignedUserId(assignedUserId))
                .and(hasAssignedTeamId(assignedTeamId))
                .and(matchesQuery(q));
    }
}
