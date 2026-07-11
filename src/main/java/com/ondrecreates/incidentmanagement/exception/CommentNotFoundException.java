package com.ondrecreates.incidentmanagement.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long id) {
        super("Comment %d not found".formatted(id));
    }
}
