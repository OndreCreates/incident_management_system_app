package com.ondrecreates.incidentmanagement.exception;

import com.ondrecreates.incidentmanagement.domain.Status;
import java.util.Set;

public class InvalidTransitionException extends RuntimeException {

    private final Status from;
    private final Status attempted;
    private final Set<Status> allowed;

    public InvalidTransitionException(Status from, Status attempted, Set<Status> allowed) {
        super("Cannot transition incident from %s to %s".formatted(from, attempted));
        this.from = from;
        this.attempted = attempted;
        this.allowed = allowed;
    }

    public Status getFrom() {
        return from;
    }

    public Status getAttempted() {
        return attempted;
    }

    public Set<Status> getAllowed() {
        return allowed;
    }
}
