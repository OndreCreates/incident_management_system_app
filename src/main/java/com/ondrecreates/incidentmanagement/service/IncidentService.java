package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentComment;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.CreateIncidentRequest;
import com.ondrecreates.incidentmanagement.exception.IncidentNotFoundException;
import com.ondrecreates.incidentmanagement.repository.IncidentCommentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentSpecifications;
import com.ondrecreates.incidentmanagement.repository.IncidentTimelineRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentTransitionService transitionService;
    private final IncidentAssignmentService assignmentService;

    public IncidentService(IncidentRepository incidentRepository,
                            IncidentTimelineRepository timelineRepository,
                            IncidentCommentRepository commentRepository,
                            IncidentTransitionService transitionService,
                            IncidentAssignmentService assignmentService) {
        this.incidentRepository = incidentRepository;
        this.timelineRepository = timelineRepository;
        this.commentRepository = commentRepository;
        this.transitionService = transitionService;
        this.assignmentService = assignmentService;
    }

    @Transactional
    public Incident createIncident(CreateIncidentRequest request) {
        Instant slaDeadline = Instant.now().plus(request.severity().getSlaDuration());
        Incident incident = new Incident(request.title(), request.description(), request.severity(),
                request.priority(), slaDeadline, request.createdBy());
        return incidentRepository.save(incident);
    }

    public Incident getIncidentOrThrow(Long id) {
        return incidentRepository.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
    }

    public Page<Incident> listIncidents(Status status, Severity severity, Long assignedUserId, Pageable pageable) {
        return incidentRepository.findAll(IncidentSpecifications.filter(status, severity, assignedUserId), pageable);
    }

    public List<IncidentTimelineEntry> getTimeline(Long incidentId) {
        Incident incident = getIncidentOrThrow(incidentId);
        return timelineRepository.findByIncidentOrderByCreatedAtAsc(incident);
    }

    @Transactional
    public Incident transition(Long incidentId, Status target, Long assignedUserId, Long actorUserId, String note) {
        Incident incident = getIncidentOrThrow(incidentId);
        if (assignedUserId != null) {
            incident = assignmentService.assign(incident, assignedUserId, actorUserId);
        }
        return transitionService.transition(incident, target, actorUserId, note);
    }

    @Transactional
    public IncidentComment addComment(Long incidentId, String content, Long authorUserId) {
        Incident incident = getIncidentOrThrow(incidentId);
        IncidentComment comment = commentRepository.save(new IncidentComment(incident, authorUserId, content));
        timelineRepository.save(IncidentTimelineEntry.forComment(incident, comment, authorUserId));
        return comment;
    }
}
