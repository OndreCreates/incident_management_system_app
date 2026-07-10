package com.ondrecreates.incidentmanagement.exception;

public class PostmortemAlreadyExistsException extends RuntimeException {

    public PostmortemAlreadyExistsException(Long incidentId) {
        super("Incident %d already has a postmortem -- use PUT to update it".formatted(incidentId));
    }
}
