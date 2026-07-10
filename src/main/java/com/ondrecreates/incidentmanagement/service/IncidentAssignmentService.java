package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentTimelineRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Přiřazení incidentu na uživatele je oddělené od stavového přechodu
 * (viz IncidentTransitionService) — jde o samostatný audit fakt
 * (EventType.ASSIGNMENT), i když se v praxi často děje spolu s
 * přechodem na ASSIGNED.
 */
@Service
public class IncidentAssignmentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;

    public IncidentAssignmentService(IncidentRepository incidentRepository,
                                      IncidentTimelineRepository timelineRepository) {
        this.incidentRepository = incidentRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional
    public Incident assign(Incident incident, String assigneeUserId, String actorUserId) {
        if (Objects.equals(incident.getAssignedUserId(), assigneeUserId)) {
            return incident;
        }
        incident.setAssignedUserId(assigneeUserId);
        Incident saved = incidentRepository.save(incident);
        timelineRepository.save(IncidentTimelineEntry.forAssignment(saved, actorUserId,
                "Assigned to user " + assigneeUserId));
        return saved;
    }
}
