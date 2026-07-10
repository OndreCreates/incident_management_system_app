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

    public static Specification<Incident> hasAssignedUserId(Long assignedUserId) {
        return (root, query, cb) -> assignedUserId == null ? null
                : cb.equal(root.get("assignedUserId"), assignedUserId);
    }

    public static Specification<Incident> filter(Status status, Severity severity, Long assignedUserId) {
        return Specification.where(hasStatus(status))
                .and(hasSeverity(severity))
                .and(hasAssignedUserId(assignedUserId));
    }
}
