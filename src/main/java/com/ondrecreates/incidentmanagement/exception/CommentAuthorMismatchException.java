package com.ondrecreates.incidentmanagement.exception;

public class CommentAuthorMismatchException extends RuntimeException {

    public CommentAuthorMismatchException(Long commentId) {
        super("Only the original author can edit or delete comment %d".formatted(commentId));
    }
}
