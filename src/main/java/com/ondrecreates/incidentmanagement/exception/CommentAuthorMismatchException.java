package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

public class CommentAuthorMismatchException extends ApiException {

    public CommentAuthorMismatchException(Long commentId) {
        super("Only the original author can edit or delete comment %d".formatted(commentId));
    }

    @Override
    public String errorCode() {
        return "COMMENT_AUTHOR_MISMATCH";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }
}
