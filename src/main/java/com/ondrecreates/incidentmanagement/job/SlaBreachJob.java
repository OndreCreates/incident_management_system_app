package com.ondrecreates.incidentmanagement.job;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vyřešený/uzavřený incident se nikdy neoznačí jako breached, i kdyby
 * trval déle než SLA okno — breach detekce platí jen pro incidenty,
 * které jsou stále otevřené po deadline (Status.TERMINAL_STATUSES).
 */
@Component
public class SlaBreachJob {

    private final IncidentRepository incidentRepository;

    public SlaBreachJob(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void detectBreaches() {
        List<Incident> overdue = incidentRepository.findByStatusNotInAndSlaDeadlineBeforeAndSlaBreachedFalse(
                Status.TERMINAL_STATUSES, Instant.now());
        overdue.forEach(incident -> incident.setSlaBreached(true));
        incidentRepository.saveAll(overdue);
    }
}
