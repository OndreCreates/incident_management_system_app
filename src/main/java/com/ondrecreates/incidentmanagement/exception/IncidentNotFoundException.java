package com.ondrecreates.incidentmanagement.exception;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(Long id) {
        super("Incident %d not found".formatted(id));
    }
}
