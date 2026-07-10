package com.ondrecreates.incidentmanagement;

import static org.assertj.core.api.Assertions.assertThat;

import com.ondrecreates.incidentmanagement.repository.IncidentCommentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentRepository;
import com.ondrecreates.incidentmanagement.repository.IncidentTimelineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Kontext se musí nahodit a Flyway migrace proběhnout bez chyby proti reálné
 * MySQL (docker-compose service) — ddl-auto: validate shodí start, pokud
 * entity a schéma nesedí.
 */
@SpringBootTest
class ApplicationContextSmokeTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentTimelineRepository incidentTimelineRepository;

    @Autowired
    private IncidentCommentRepository incidentCommentRepository;

    @Test
    void contextLoadsAndRepositoriesAreWired() {
        assertThat(incidentRepository).isNotNull();
        assertThat(incidentTimelineRepository).isNotNull();
        assertThat(incidentCommentRepository).isNotNull();
        assertThat(incidentRepository.findAll()).isEmpty();
    }
}
