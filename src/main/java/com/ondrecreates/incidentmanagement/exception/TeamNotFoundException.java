package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

public class TeamNotFoundException extends ApiException {

    public TeamNotFoundException(Long id) {
        super("Team %d not found".formatted(id));
    }

    @Override
    public String errorCode() {
        return "TEAM_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
