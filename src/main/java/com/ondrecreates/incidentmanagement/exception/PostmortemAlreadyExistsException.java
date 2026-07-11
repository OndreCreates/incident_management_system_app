package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

public class PostmortemAlreadyExistsException extends ApiException {

    public PostmortemAlreadyExistsException(Long incidentId) {
        super("Incident %d already has a postmortem -- use PUT to update it".formatted(incidentId));
    }

    @Override
    public String errorCode() {
        return "POSTMORTEM_ALREADY_EXISTS";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
