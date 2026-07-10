package com.ondrecreates.incidentmanagement.exception;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException(Long id) {
        super("Team %d not found".formatted(id));
    }
}
