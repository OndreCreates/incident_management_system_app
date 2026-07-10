package com.ondrecreates.incidentmanagement.exception;

import com.ondrecreates.incidentmanagement.domain.Status;

public class PostmortemNotAllowedException extends RuntimeException {

    public PostmortemNotAllowedException(Long incidentId, Status currentStatus) {
        super("Incident %d is not in a terminal status (currently %s) -- postmortem requires Resolved or Closed"
                .formatted(incidentId, currentStatus));
    }
}
