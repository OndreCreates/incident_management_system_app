package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.exception.IncidentNotFoundException;
import com.ondrecreates.incidentmanagement.exception.InvalidTransitionException;
import com.ondrecreates.incidentmanagement.exception.PostmortemAlreadyExistsException;
import com.ondrecreates.incidentmanagement.exception.PostmortemNotAllowedException;
import com.ondrecreates.incidentmanagement.exception.PostmortemNotFoundException;
import com.ondrecreates.incidentmanagement.exception.TeamNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleIncidentNotFound(IncidentNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "INCIDENT_NOT_FOUND");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTeamNotFound(TeamNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "TEAM_NOT_FOUND");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(PostmortemNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePostmortemNotFound(PostmortemNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "POSTMORTEM_NOT_FOUND");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(PostmortemNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handlePostmortemNotAllowed(PostmortemNotAllowedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "POSTMORTEM_NOT_ALLOWED");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(PostmortemAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handlePostmortemAlreadyExists(PostmortemAlreadyExistsException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "POSTMORTEM_ALREADY_EXISTS");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "VALIDATION_FAILED");
        body.put("fields", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
