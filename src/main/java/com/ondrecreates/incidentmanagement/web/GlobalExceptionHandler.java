package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.exception.InvalidTransitionException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidTransitionException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "INVALID_TRANSITION");
        body.put("from", ex.getFrom());
        body.put("attempted", ex.getAttempted());
        body.put("allowed", ex.getAllowed());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
