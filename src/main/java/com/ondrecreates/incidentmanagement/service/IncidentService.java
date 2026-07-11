package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentComment;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.SlaPolicy;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.CreateIncidentRequest;
import com.ondrecreates.incidentmanagement.exception.CommentAuthorMismatchException;
import com.ondrecreates.incidentmanagement.exception.CommentNotFoundException;
import com.ondrecreates.incidentmanagement.exception.IncidentNotFoundException;
import com.ondrecreates.incidentmanagement.repository.IncidentCommentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentSpecifications;
import com.ondrecreates.incidentmanagement.repository.IncidentTimelineRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentTransitionService transitionService;
    private final IncidentAssignmentService assignmentService;
    private final IncidentTeamAssignmentService teamAssignmentService;
    private final SlaPolicyService slaPolicyService;

    public IncidentService(IncidentRepository incidentRepository,
                            IncidentTimelineRepository timelineRepository,
                            IncidentCommentRepository commentRepository,
                            IncidentTransitionService transitionService,
                            IncidentAssignmentService assignmentService,
                            IncidentTeamAssignmentService teamAssignmentService,
                            SlaPolicyService slaPolicyService) {
        this.incidentRepository = incidentRepository;
        this.timelineRepository = timelineRepository;
        this.commentRepository = commentRepository;
        this.transitionService = transitionService;
        this.assignmentService = assignmentService;
        this.teamAssignmentService = teamAssignmentService;
        this.slaPolicyService = slaPolicyService;
    }

    @Transactional
    public Incident createIncident(CreateIncidentRequest request, String createdBy) {
        Instant now = Instant.now();
        SlaPolicy policy = slaPolicyService.getPolicy(request.severity());
        Instant slaDeadline = now.plus(policy.slaDuration());
        Instant nearBreachAt = now.plus(policy.nearBreachDuration());
        Incident incident = new Incident(request.title(), request.description(), request.severity(),
                request.priority(), slaDeadline, nearBreachAt, createdBy);
        return incidentRepository.save(incident);
    }

    public Incident getIncidentOrThrow(Long id) {
        return incidentRepository.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
    }

    public Page<Incident> listIncidents(Status status, Severity severity, String assignedUserId, Long assignedTeamId,
                                         String q, Pageable pageable) {
        return incidentRepository.findAll(
                IncidentSpecifications.filter(status, severity, assignedUserId, assignedTeamId, q), pageable);
    }

    public List<IncidentTimelineEntry> getTimeline(Long incidentId) {
        Incident incident = getIncidentOrThrow(incidentId);
        return timelineRepository.findByIncidentOrderByCreatedAtAsc(incident);
    }

    public List<Incident> exportIncidents(Status status, Severity severity, String assignedUserId,
                                           Long assignedTeamId, String q) {
        return incidentRepository.findAll(
                IncidentSpecifications.filter(status, severity, assignedUserId, assignedTeamId, q),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional
    public Incident transition(Long incidentId, Status target, String assignedUserId, String actorUserId, String note) {
        Incident incident = getIncidentOrThrow(incidentId);
        if (assignedUserId != null) {
            incident = assignmentService.assign(incident, assignedUserId, actorUserId);
        }
        return transitionService.transition(incident, target, actorUserId, note);
    }

    @Transactional
    public Incident assignTeam(Long incidentId, Long teamId, String actorUserId) {
        Incident incident = getIncidentOrThrow(incidentId);
        return teamAssignmentService.assignTeam(incident, teamId, actorUserId);
    }

    @Transactional
    public Incident assignUser(Long incidentId, String assignedUserId, String actorUserId) {
        Incident incident = getIncidentOrThrow(incidentId);
        return assignmentService.assign(incident, assignedUserId, actorUserId);
    }

    @Transactional
    public IncidentComment addComment(Long incidentId, String content, String authorUserId) {
        Incident incident = getIncidentOrThrow(incidentId);
        IncidentComment comment = commentRepository.save(new IncidentComment(incident, authorUserId, content));
        timelineRepository.save(IncidentTimelineEntry.forComment(incident, comment, authorUserId));
        return comment;
    }

    @Transactional
    public IncidentComment editComment(Long incidentId, Long commentId, String newContent, String actorUserId) {
        IncidentComment comment = getCommentOrThrow(incidentId, commentId);
        requireAuthor(comment, actorUserId);
        comment.edit(newContent);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long incidentId, Long commentId, String actorUserId) {
        IncidentComment comment = getCommentOrThrow(incidentId, commentId);
        requireAuthor(comment, actorUserId);
        comment.softDelete();
        commentRepository.save(comment);
    }

    private IncidentComment getCommentOrThrow(Long incidentId, Long commentId) {
        IncidentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getIncident().getId().equals(incidentId)) {
            throw new CommentNotFoundException(commentId);
        }
        return comment;
    }

    private void requireAuthor(IncidentComment comment, String actorUserId) {
        if (!comment.getAuthorUserId().equals(actorUserId)) {
            throw new CommentAuthorMismatchException(comment.getId());
        }
    }
}
