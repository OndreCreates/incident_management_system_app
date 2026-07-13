package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

public class InsufficientRoleException extends ApiException {

    public InsufficientRoleException(String userEmail) {
        super("User %s does not have the ADMIN role required for this action".formatted(userEmail));
    }

    @Override
    public String errorCode() {
        return "ROLE_FORBIDDEN";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }
}
