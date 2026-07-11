package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

public class IncidentNotFoundException extends ApiException {

    public IncidentNotFoundException(Long id) {
        super("Incident %d not found".formatted(id));
    }

    @Override
    public String errorCode() {
        return "INCIDENT_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
