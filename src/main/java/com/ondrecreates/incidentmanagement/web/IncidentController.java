package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.AssignTeamRequest;
import com.ondrecreates.incidentmanagement.dto.BulkAssignRequest;
import com.ondrecreates.incidentmanagement.dto.BulkOperationResult;
import com.ondrecreates.incidentmanagement.dto.BulkTransitionRequest;
import com.ondrecreates.incidentmanagement.dto.CommentRequest;
import com.ondrecreates.incidentmanagement.dto.CommentResponse;
import com.ondrecreates.incidentmanagement.dto.CreateIncidentRequest;
import com.ondrecreates.incidentmanagement.dto.IncidentDetailResponse;
import com.ondrecreates.incidentmanagement.dto.IncidentResponse;
import com.ondrecreates.incidentmanagement.dto.TimelineEntryResponse;
import com.ondrecreates.incidentmanagement.dto.TransitionRequest;
import com.ondrecreates.incidentmanagement.service.BulkOperationService;
import com.ondrecreates.incidentmanagement.service.IncidentCsvExporter;
import com.ondrecreates.incidentmanagement.service.IncidentService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "CRUD, state transitions and comments")
@SecurityRequirement(name = "bearerAuth")
public class IncidentController {

    private final IncidentService incidentService;
    private final BulkOperationService bulkOperationService;
    private final IncidentCsvExporter csvExporter;

    public IncidentController(IncidentService incidentService, BulkOperationService bulkOperationService,
                               IncidentCsvExporter csvExporter) {
        this.incidentService = incidentService;
        this.bulkOperationService = bulkOperationService;
        this.csvExporter = csvExporter;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an incident", description = "Status starts at CREATED. SLA deadline is "
            + "computed from severity. createdBy is taken from the JWT, never the request body.")
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return IncidentResponse.from(incidentService.createIncident(request, jwt.getSubject()));
    }

    @GetMapping
    @Operation(summary = "List incidents", description = "Filterable by status/severity/assignedUserId/"
            + "assignedTeamId, paginated. q does a case-insensitive substring search over title/description.")
    public Page<IncidentResponse> list(@RequestParam(required = false) Status status,
                                        @RequestParam(required = false) Severity severity,
                                        @RequestParam(required = false) String assignedUserId,
                                        @RequestParam(required = false) Long assignedTeamId,
                                        @RequestParam(required = false) String q,
                                        Pageable pageable) {
        return incidentService.listIncidents(status, severity, assignedUserId, assignedTeamId, q, pageable)
                .map(IncidentResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident detail", description = "Includes the full timeline.")
    public IncidentDetailResponse detail(@PathVariable Long id) {
        Incident incident = incidentService.getIncidentOrThrow(id);
        return IncidentDetailResponse.from(incident, incidentService.getTimeline(id));
    }

    @PostMapping("/{id}/transition")
    @Operation(summary = "Transition to a new status", description = "Rejected with 409 INVALID_TRANSITION "
            + "(body lists currently allowed next statuses) if not in ALLOWED_TRANSITIONS for the current status.")
    @ApiResponse(responseCode = "409", description = "Invalid transition")
    public IncidentResponse transition(@PathVariable Long id, @Valid @RequestBody TransitionRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        Incident incident = incidentService.transition(id, request.targetStatus(), request.assignedUserId(),
                jwt.getSubject(), request.note());
        return IncidentResponse.from(incident);
    }

    @GetMapping("/export")
    @Operation(summary = "Export incidents as CSV", description = "Respects the same status/severity/"
            + "assignedUserId/assignedTeamId/q filters as the list endpoint, but returns the full matching "
            + "set (no pagination).")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Status status,
                                          @RequestParam(required = false) Severity severity,
                                          @RequestParam(required = false) String assignedUserId,
                                          @RequestParam(required = false) Long assignedTeamId,
                                          @RequestParam(required = false) String q) {
        List<Incident> incidents = incidentService.exportIncidents(status, severity, assignedUserId,
                assignedTeamId, q);
        byte[] csv = csvExporter.toCsv(incidents).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("incidents.csv").build().toString())
                .body(csv);
    }

    @PostMapping("/bulk-transition")
    @Operation(summary = "Transition many incidents at once", description = "Per-item, not all-or-nothing -- "
            + "each incidentId gets its own success/error in the response, so a batch mixing valid and invalid "
            + "transitions still applies the valid ones.")
    public List<BulkOperationResult> bulkTransition(@Valid @RequestBody BulkTransitionRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return bulkOperationService.bulkTransition(request.incidentIds(), request.targetStatus(), request.note(),
                jwt.getSubject());
    }

    @PostMapping("/bulk-assign")
    @Operation(summary = "Assign many incidents to one user at once", description = "Per-item, not all-or-nothing.")
    public List<BulkOperationResult> bulkAssign(@Valid @RequestBody BulkAssignRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return bulkOperationService.bulkAssign(request.incidentIds(), request.assignedUserId(), jwt.getSubject());
    }

    @PostMapping("/{id}/assign-team")
    @Operation(summary = "Route the incident to a team", description = "Independent of individual assignment -- "
            + "writes a TEAM_ASSIGNMENT timeline entry.")
    public IncidentResponse assignTeam(@PathVariable Long id, @Valid @RequestBody AssignTeamRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return IncidentResponse.from(incidentService.assignTeam(id, request.teamId(), jwt.getSubject()));
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment", description = "Also creates a COMMENT timeline entry in the same transaction.")
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return CommentResponse.from(incidentService.addComment(id, request.content(), jwt.getSubject()));
    }

    @PutMapping("/{id}/comments/{commentId}")
    @Operation(summary = "Edit a comment", description = "Only the original author can edit it. Marks the "
            + "comment as edited; the timeline entry's timestamp/actor still reflect the original post.")
    @ApiResponse(responseCode = "403", description = "Not the comment's author")
    public CommentResponse editComment(@PathVariable Long id, @PathVariable Long commentId,
                                        @Valid @RequestBody CommentRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return CommentResponse.from(incidentService.editComment(id, commentId, request.content(), jwt.getSubject()));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a comment", description = "Only the original author can delete it. Soft delete -- "
            + "the timeline entry still exists, its content is just hidden (append-only audit trail).")
    @ApiResponse(responseCode = "403", description = "Not the comment's author")
    public void deleteComment(@PathVariable Long id, @PathVariable Long commentId,
                               @AuthenticationPrincipal Jwt jwt) {
        incidentService.deleteComment(id, commentId, jwt.getSubject());
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get the full audit timeline", description = "Append-only: status changes, assignments, comments.")
    public ResponseEntity<Iterable<TimelineEntryResponse>> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getTimeline(id).stream().map(TimelineEntryResponse::from).toList());
    }
}
