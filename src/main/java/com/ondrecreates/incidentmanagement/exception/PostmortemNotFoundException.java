package com.ondrecreates.incidentmanagement.exception;

public class PostmortemNotFoundException extends RuntimeException {

    public PostmortemNotFoundException(Long incidentId) {
        super("No postmortem for incident %d".formatted(incidentId));
    }
}
