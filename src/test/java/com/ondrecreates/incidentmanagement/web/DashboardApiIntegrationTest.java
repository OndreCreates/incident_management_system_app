package com.ondrecreates.incidentmanagement.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Priority;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.job.SlaBreachJob;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private SlaBreachJob slaBreachJob;

    @Test
    void summaryReflectsActiveCriticalAndBreachedCounts() throws Exception {
        incidentRepository.save(new Incident("Critical overdue", "desc", Severity.CRITICAL, Priority.P1,
                Instant.now().minusSeconds(3600), Instant.now().plusSeconds(3600), "creator@example.com"));
        incidentRepository.save(new Incident("High, still within SLA", "desc", Severity.HIGH, Priority.P2,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200), "creator@example.com"));

        slaBreachJob.detectBreaches();

        mockMvc.perform(get("/api/v1/dashboard/summary").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeCount").value(2))
                .andExpect(jsonPath("$.criticalCount").value(1))
                .andExpect(jsonPath("$.breachedCount").value(1));
    }

    @Test
    void analyticsReflectsResolutionTimeAndSlaCompliance() throws Exception {
        Incident compliant = new Incident("Resolved without breach", "desc", Severity.LOW, Priority.P4,
                Instant.now().plusSeconds(7200), Instant.now().plusSeconds(3600), "creator@example.com");
        compliant.setStatus(Status.RESOLVED);
        compliant.setResolvedAt(Instant.now().minusSeconds(600));
        incidentRepository.save(compliant);

        Incident breached = new Incident("Resolved after breach", "desc", Severity.LOW, Priority.P4,
                Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600), "creator@example.com");
        breached.setStatus(Status.RESOLVED);
        breached.setSlaBreached(true);
        breached.setResolvedAt(Instant.now().minusSeconds(60));
        incidentRepository.save(breached);

        mockMvc.perform(get("/api/v1/dashboard/analytics").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avgResolutionMinutes").isNotEmpty())
                .andExpect(jsonPath("$.slaComplianceRate").value(50.0))
                .andExpect(jsonPath("$.createdPerDay").isArray());
    }
}
