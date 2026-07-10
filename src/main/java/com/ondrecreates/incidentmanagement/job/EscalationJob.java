package com.ondrecreates.incidentmanagement.job;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.domain.Team;
import com.ondrecreates.incidentmanagement.domain.TeamMember;
import com.ondrecreates.incidentmanagement.notification.NotificationClient;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.service.TeamService;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Two independent polling loops (near-breach and already-breached) rather
 * than piggybacking on SlaBreachJob -- decouples this job's notified flags
 * from exactly when SlaBreachJob happens to flip sla_breached, and keeps
 * SlaBreachJob (Fáze 1E, already tested) untouched.
 *
 * Recipient resolution: notify the individual assignee if there is one,
 * otherwise the whole team if the incident is routed to one, otherwise
 * nobody (nothing to escalate to yet).
 */
@Component
public class EscalationJob {

    private final IncidentRepository incidentRepository;
    private final TeamService teamService;
    private final NotificationClient notificationClient;

    public EscalationJob(IncidentRepository incidentRepository, TeamService teamService,
                          NotificationClient notificationClient) {
        this.incidentRepository = incidentRepository;
        this.teamService = teamService;
        this.notificationClient = notificationClient;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void detectNearBreaches() {
        List<Incident> incidents = incidentRepository
                .findByStatusNotInAndNearBreachAtBeforeAndSlaBreachedFalseAndNearBreachNotifiedFalse(
                        Status.TERMINAL_STATUSES, Instant.now());
        incidents.forEach(this::escalateNearBreach);
        incidentRepository.saveAll(incidents);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void detectBreachEscalations() {
        List<Incident> incidents = incidentRepository.findBySlaBreachedTrueAndBreachNotifiedFalse();
        incidents.forEach(this::escalateBreach);
        incidentRepository.saveAll(incidents);
    }

    private void escalateNearBreach(Incident incident) {
        String subject = "Near SLA breach: " + incident.getTitle();
        String body = "Incident #%d (%s) is approaching its SLA deadline at %s.".formatted(
                incident.getId(), incident.getSeverity(), incident.getSlaDeadline());
        notifyRecipients(incident, subject, body);
        incident.setNearBreachNotified(true);
    }

    private void escalateBreach(Incident incident) {
        String subject = "SLA BREACHED: " + incident.getTitle();
        String body = "Incident #%d (%s) has breached its SLA deadline (%s).".formatted(
                incident.getId(), incident.getSeverity(), incident.getSlaDeadline());
        notifyRecipients(incident, subject, body);
        incident.setBreachNotified(true);
    }

    private void notifyRecipients(Incident incident, String subject, String body) {
        for (String recipient : resolveRecipients(incident)) {
            notificationClient.sendEmail(recipient, subject, body);
        }
    }

    private List<String> resolveRecipients(Incident incident) {
        if (incident.getAssignedUserId() != null) {
            return List.of(incident.getAssignedUserId());
        }
        if (incident.getAssignedTeam() != null) {
            Team team = teamService.getTeamOrThrow(incident.getAssignedTeam().getId());
            return team.getMembers().stream().map(TeamMember::getUserEmail).toList();
        }
        return List.of();
    }
}
