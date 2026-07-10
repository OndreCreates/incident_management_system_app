package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.exception.InvalidTransitionException;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentTimelineRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentTransitionService {

    private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = Map.of(
            Status.CREATED, Set.of(Status.ASSIGNED),
            Status.ASSIGNED, Set.of(Status.INVESTIGATING),
            Status.INVESTIGATING, Set.of(Status.MITIGATED, Status.ASSIGNED),
            Status.MITIGATED, Set.of(Status.RESOLVED, Status.INVESTIGATING),
            Status.RESOLVED, Set.of(Status.CLOSED, Status.INVESTIGATING),
            Status.CLOSED, Set.of(Status.INVESTIGATING)
    );

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;

    public IncidentTransitionService(IncidentRepository incidentRepository,
                                      IncidentTimelineRepository timelineRepository) {
        this.incidentRepository = incidentRepository;
        this.timelineRepository = timelineRepository;
    }

    public static Set<Status> allowedNextStatuses(Status from) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
    }

    @Transactional
    public Incident transition(Incident incident, Status target, String actorUserId, String note) {
        Set<Status> allowed = allowedNextStatuses(incident.getStatus());
        if (!allowed.contains(target)) {
            throw new InvalidTransitionException(incident.getStatus(), target, allowed);
        }

        Status previous = incident.getStatus();
        incident.setStatus(target);
        // Tracked for dashboard analytics (avg. resolution time) -- cleared on reopen so a
        // re-resolved incident's resolution time reflects the latest resolution, not the first.
        if (target == Status.RESOLVED) {
            incident.setResolvedAt(Instant.now());
        } else if (target == Status.INVESTIGATING) {
            incident.setResolvedAt(null);
        }
        Incident saved = incidentRepository.save(incident);
        timelineRepository.save(IncidentTimelineEntry.forStatusChange(saved, previous, target, actorUserId, note));
        return saved;
    }
}
