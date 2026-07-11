package com.ondrecreates.incidentmanagement.exception;

import com.ondrecreates.incidentmanagement.domain.Status;
import org.springframework.http.HttpStatus;

public class PostmortemNotAllowedException extends ApiException {

    public PostmortemNotAllowedException(Long incidentId, Status currentStatus) {
        super("Incident %d is not in a terminal status (currently %s) -- postmortem requires Resolved or Closed"
                .formatted(incidentId, currentStatus));
    }

    @Override
    public String errorCode() {
        return "POSTMORTEM_NOT_ALLOWED";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
