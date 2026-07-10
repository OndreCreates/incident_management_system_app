package com.ondrecreates.incidentmanagement.domain;

import java.util.Set;

public enum Status {
    CREATED,
    ASSIGNED,
    INVESTIGATING,
    MITIGATED,
    RESOLVED,
    CLOSED;

    public static final Set<Status> TERMINAL_STATUSES = Set.of(RESOLVED, CLOSED);
}
