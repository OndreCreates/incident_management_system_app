package com.ondrecreates.incidentmanagement.job;

import static org.assertj.core.api.Assertions.assertThat;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Priority;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fáze 1E acceptance: scheduled job označí zpožděné otevřené incidenty do
 * jednoho pollovacího intervalu a nikdy neoznačí terminální (Resolved/Closed)
 * incident, i kdyby byl po deadline — proti reálné MySQL, s ručně
 * nastaveným deadline do minulosti (viz FAZE_1_PROMPT.md Fáze 1G krok 5).
 */
@SpringBootTest
@Transactional
class SlaBreachJobTest {

    @Autowired
    private SlaBreachJob slaBreachJob;

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void marksOverdueOpenIncidentAsBreached() {
        Incident incident = incidentRepository.save(new Incident("DB connection pool exhausted", "desc",
                Severity.CRITICAL, Priority.P1, Instant.now().minusSeconds(3600), "creator@example.com"));

        slaBreachJob.detectBreaches();

        Incident reloaded = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(reloaded.isSlaBreached()).isTrue();
    }

    @Test
    void neverMarksTerminalIncidentAsBreachedEvenWhenOverdue() {
        Incident incident = new Incident("Already handled outage", "desc", Severity.HIGH, Priority.P2,
                Instant.now().minusSeconds(3600), "creator@example.com");
        incident.setStatus(Status.RESOLVED);
        incident = incidentRepository.save(incident);

        slaBreachJob.detectBreaches();

        Incident reloaded = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(reloaded.isSlaBreached()).isFalse();
    }

    @Test
    void doesNotMarkIncidentBeforeItsDeadline() {
        Incident incident = incidentRepository.save(new Incident("Fresh incident", "desc", Severity.LOW,
                Priority.P4, Instant.now().plusSeconds(3600), "creator@example.com"));

        slaBreachJob.detectBreaches();

        Incident reloaded = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(reloaded.isSlaBreached()).isFalse();
    }
}
