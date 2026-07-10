package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.CommentRequest;
import com.ondrecreates.incidentmanagement.dto.CommentResponse;
import com.ondrecreates.incidentmanagement.dto.CreateIncidentRequest;
import com.ondrecreates.incidentmanagement.dto.IncidentDetailResponse;
import com.ondrecreates.incidentmanagement.dto.IncidentResponse;
import com.ondrecreates.incidentmanagement.dto.TimelineEntryResponse;
import com.ondrecreates.incidentmanagement.dto.TransitionRequest;
import com.ondrecreates.incidentmanagement.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return IncidentResponse.from(incidentService.createIncident(request, jwt.getSubject()));
    }

    @GetMapping
    public Page<IncidentResponse> list(@RequestParam(required = false) Status status,
                                        @RequestParam(required = false) Severity severity,
                                        @RequestParam(required = false) String assignedUserId,
                                        Pageable pageable) {
        return incidentService.listIncidents(status, severity, assignedUserId, pageable)
                .map(IncidentResponse::from);
    }

    @GetMapping("/{id}")
    public IncidentDetailResponse detail(@PathVariable Long id) {
        Incident incident = incidentService.getIncidentOrThrow(id);
        return IncidentDetailResponse.from(incident, incidentService.getTimeline(id));
    }

    @PostMapping("/{id}/transition")
    public IncidentResponse transition(@PathVariable Long id, @Valid @RequestBody TransitionRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        Incident incident = incidentService.transition(id, request.targetStatus(), request.assignedUserId(),
                jwt.getSubject(), request.note());
        return IncidentResponse.from(incident);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return CommentResponse.from(incidentService.addComment(id, request.content(), jwt.getSubject()));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<Iterable<TimelineEntryResponse>> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getTimeline(id).stream().map(TimelineEntryResponse::from).toList());
    }
}
