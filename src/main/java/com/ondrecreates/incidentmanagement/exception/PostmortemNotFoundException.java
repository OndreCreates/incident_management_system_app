package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

public class PostmortemNotFoundException extends ApiException {

    public PostmortemNotFoundException(Long incidentId) {
        super("No postmortem for incident %d".formatted(incidentId));
    }

    @Override
    public String errorCode() {
        return "POSTMORTEM_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
