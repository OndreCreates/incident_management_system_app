package com.ondrecreates.incidentmanagement.exception;

import org.springframework.http.HttpStatus;

/** Common shape for domain exceptions that map to a {error, message} JSON body --
 * lets GlobalExceptionHandler use one @ExceptionHandler instead of one per subclass.
 * InvalidTransitionException stays separate: its body has extra fields (from/attempted/allowed). */
public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }

    public abstract String errorCode();

    public abstract HttpStatus status();
}
