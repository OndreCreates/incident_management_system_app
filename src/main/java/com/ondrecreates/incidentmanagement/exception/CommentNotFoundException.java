package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends ApiException {

    public CommentNotFoundException(Long id) {
        super("Comment %d not found".formatted(id));
    }

    @Override
    public String errorCode() {
        return "COMMENT_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
