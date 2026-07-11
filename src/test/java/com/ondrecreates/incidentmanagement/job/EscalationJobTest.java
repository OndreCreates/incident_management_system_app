package com.ondrecreates.incidentmanagement.job;

import static org.assertj.core.api.Assertions.assertThat;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Priority;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.domain.Team;
import com.ondrecreates.incidentmanagement.notification.NotificationClient;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.service.TeamService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fáze 2 acceptance: near-breach and breach escalations notify the right
 * recipient (individual assignee, else whole team, else nobody) exactly
 * once. NotificationClient is swapped for an in-memory fake (see
 * {@link Config}) rather than mocked -- Mockito's inline mock maker fails
 * to instrument classes under some JDKs (observed on JDK 25 here); the
 * interface split (NotificationClient / RestNotificationClient) makes this
 * unnecessary anyway. This tests recipient resolution and the
 * notified-flag bookkeeping that prevents duplicate escalations, not HTTP
 * delivery itself (RestNotificationClient's own concern).
 */
@SpringBootTest
@Transactional
class EscalationJobTest {

    @Autowired
    private EscalationJob escalationJob;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private FakeNotificationClient notificationClient;

    @BeforeEach
    void resetFakeNotificationClient() {
        // Spring caches the ApplicationContext (and this singleton bean) across test
        // methods -- without this, recipients from a previous test would leak in.
        notificationClient.recipients().clear();
        notificationClient.webSocketRecipients().clear();
    }

    @Test
    void nearBreachNotifiesIndividualAssigneeAndMarksNotified() {
        Incident incident = new Incident("DB latency creeping up", "desc", Severity.CRITICAL, Priority.P1,
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(60), "creator@example.com");
        incident.setAssignedUserId("responder@example.com");
        incident = incidentRepository.save(incident);

        escalationJob.detectNearBreaches();

        Incident reloaded = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(reloaded.isNearBreachNotified()).isTrue();
        assertThat(notificationClient.recipients()).containsExactly("responder@example.com");
    }

    @Test
    void nearBreachNotifiesAllTeamMembersWhenNoIndividualAssignee() {
        Team team = teamService.createTeam("SRE On-Call", List.of("a@example.com", "b@example.com"));

        Incident incident = new Incident("Elevated error rate", "desc", Severity.HIGH, Priority.P2,
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(60), "creator@example.com");
        incident.setAssignedTeam(team);
        incidentRepository.save(incident);

        escalationJob.detectNearBreaches();

        assertThat(notificationClient.recipients()).containsExactlyInAnyOrder("a@example.com", "b@example.com");
    }

    @Test
    void nearBreachSkipsIncidentsWithNoAssigneeOrTeam() {
        Incident incident = new Incident("Unassigned incident", "desc", Severity.LOW, Priority.P4,
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(60), "creator@example.com");
        incident = incidentRepository.save(incident);

        escalationJob.detectNearBreaches();

        Incident reloaded = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(reloaded.isNearBreachNotified()).isTrue();
        assertThat(notificationClient.recipients()).isEmpty();
    }

    @Test
    void nearBreachDoesNotFireForTerminalIncidents() {
        Incident incident = new Incident("Already closed", "desc", Severity.CRITICAL, Priority.P1,
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(60), "creator@example.com");
        incident.setStatus(Status.CLOSED);
        incident.setAssignedUserId("responder@example.com");
        incident = incidentRepository.save(incident);

        escalationJob.detectNearBreaches();

        Incident reloaded = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(reloaded.isNearBreachNotified()).isFalse();
        assertThat(notificationClient.recipients()).isEmpty();
    }

    @Test
    void nearBreachAlsoSendsWebSocketNotification() {
        Incident incident = new Incident("DB latency creeping up", "desc", Severity.CRITICAL, Priority.P1,
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(60), "creator@example.com");
        incident.setAssignedUserId("responder@example.com");
        incidentRepository.save(incident);

        escalationJob.detectNearBreaches();

        assertThat(notificationClient.webSocketRecipients()).containsExactly("responder@example.com");
    }

    @Test
    void breachEscalationNotifiesAndMarksNotifiedExactlyOnce() {
        Incident incident = new Incident("DB connection pool exhausted", "desc", Severity.CRITICAL, Priority.P1,
                Instant.now().minusSeconds(3600), Instant.now().minusSeconds(7200), "creator@example.com");
        incident.setAssignedUserId("responder@example.com");
        incident.setSlaBreached(true);
        incident = incidentRepository.save(incident);

        escalationJob.detectBreachEscalations();
        escalationJob.detectBreachEscalations();

        Incident reloaded = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(reloaded.isBreachNotified()).isTrue();
        assertThat(notificationClient.recipients()).containsExactly("responder@example.com");
    }

    static class FakeNotificationClient implements NotificationClient {
        private final List<String> recipients = new ArrayList<>();
        private final List<String> webSocketRecipients = new ArrayList<>();

        @Override
        public void sendEmail(String recipient, String subject, String body) {
            recipients.add(recipient);
        }

        @Override
        public void sendWebSocket(String recipient, String subject, String body) {
            webSocketRecipients.add(recipient);
        }

        List<String> recipients() {
            return recipients;
        }

        List<String> webSocketRecipients() {
            return webSocketRecipients;
        }
    }

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        FakeNotificationClient fakeNotificationClient() {
            return new FakeNotificationClient();
        }
    }
}
