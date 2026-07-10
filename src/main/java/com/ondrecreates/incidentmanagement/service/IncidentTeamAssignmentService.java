package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import com.ondrecreates.incidentmanagement.domain.Team;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentTimelineRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Team routing, independent of individual assignment (see IncidentAssignmentService) --
 * an incident typically routes to a team first, then a specific person from that team
 * takes ownership. Own EventType.TEAM_ASSIGNMENT timeline entry.
 */
@Service
public class IncidentTeamAssignmentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final TeamService teamService;

    public IncidentTeamAssignmentService(IncidentRepository incidentRepository,
                                          IncidentTimelineRepository timelineRepository,
                                          TeamService teamService) {
        this.incidentRepository = incidentRepository;
        this.timelineRepository = timelineRepository;
        this.teamService = teamService;
    }

    @Transactional
    public Incident assignTeam(Incident incident, Long teamId, String actorUserId) {
        Team currentTeam = incident.getAssignedTeam();
        if (currentTeam != null && Objects.equals(currentTeam.getId(), teamId)) {
            return incident;
        }
        Team team = teamService.getTeamOrThrow(teamId);
        incident.setAssignedTeam(team);
        Incident saved = incidentRepository.save(incident);
        timelineRepository.save(IncidentTimelineEntry.forTeamAssignment(saved, actorUserId,
                "Assigned to team " + team.getName()));
        return saved;
    }
}
