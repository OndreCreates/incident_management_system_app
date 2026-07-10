package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.IncidentPostmortem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentPostmortemRepository extends JpaRepository<IncidentPostmortem, Long> {

    Optional<IncidentPostmortem> findByIncidentId(Long incidentId);
}
