package com.ondrecreates.incidentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import com.ondrecreates.incidentmanagement.domain.Priority;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.exception.InvalidTransitionException;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentTimelineRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Vyčerpávající matice přechodů — každá dvojice (from, to) přes všechny
 * Status hodnoty musí buď projít, nebo vyhodit InvalidTransitionException,
 * přesně podle mapy zdokumentované v CLAUDE.md.
 */
class IncidentTransitionServiceTest {

    private static final Map<Status, Set<Status>> EXPECTED_TRANSITIONS = new EnumMap<>(Status.class);

    static {
        EXPECTED_TRANSITIONS.put(Status.CREATED, Set.of(Status.ASSIGNED));
        EXPECTED_TRANSITIONS.put(Status.ASSIGNED, Set.of(Status.INVESTIGATING));
        EXPECTED_TRANSITIONS.put(Status.INVESTIGATING, Set.of(Status.MITIGATED, Status.ASSIGNED));
        EXPECTED_TRANSITIONS.put(Status.MITIGATED, Set.of(Status.RESOLVED, Status.INVESTIGATING));
        EXPECTED_TRANSITIONS.put(Status.RESOLVED, Set.of(Status.CLOSED, Status.INVESTIGATING));
        EXPECTED_TRANSITIONS.put(Status.CLOSED, Set.of(Status.INVESTIGATING));
    }

    private IncidentRepository incidentRepository;
    private IncidentTimelineRepository timelineRepository;
    private IncidentTransitionService service;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        timelineRepository = mock(IncidentTimelineRepository.class);
        service = new IncidentTransitionService(incidentRepository, timelineRepository);
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    static Stream<Arguments> allStatusPairs() {
        return EnumSet.allOf(Status.class).stream()
                .flatMap(from -> EnumSet.allOf(Status.class).stream()
                        .map(to -> Arguments.of(from, to)));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allStatusPairs")
    void transitionFollowsAllowedMatrix(Status from, Status to) {
        Incident incident = incidentWithStatus(from);
        boolean shouldSucceed = EXPECTED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);

        if (shouldSucceed) {
            Incident result = service.transition(incident, to, "actor@example.com", "note");

            assertThat(result.getStatus()).isEqualTo(to);
            verify(incidentRepository, times(1)).save(incident);
            verify(timelineRepository, times(1)).save(any(IncidentTimelineEntry.class));
        } else {
            assertThatThrownBy(() -> service.transition(incident, to, "actor@example.com", "note"))
                    .isInstanceOf(InvalidTransitionException.class)
                    .satisfies(ex -> {
                        InvalidTransitionException invalid = (InvalidTransitionException) ex;
                        assertThat(invalid.getFrom()).isEqualTo(from);
                        assertThat(invalid.getAttempted()).isEqualTo(to);
                        assertThat(invalid.getAllowed())
                                .isEqualTo(EXPECTED_TRANSITIONS.getOrDefault(from, Set.of()));
                    });
            assertThat(incident.getStatus()).isEqualTo(from);
            verifyNoInteractions(incidentRepository, timelineRepository);
        }
    }

    private Incident incidentWithStatus(Status status) {
        Incident incident = new Incident("title", "description", Severity.HIGH, Priority.P2,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(1800), "creator@example.com");
        incident.setStatus(status);
        return incident;
    }
}
