package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByStatusNotIn(Collection<Status> statuses);

    long countBySeverityAndStatusNotIn(Severity severity, Collection<Status> statuses);

    long countBySlaBreachedTrue();

    List<Incident> findByStatusNotInAndSlaDeadlineBeforeAndSlaBreachedFalse(Collection<Status> excludedStatuses,
                                                                             Instant now);

    List<Incident> findByStatusNotInAndNearBreachAtBeforeAndSlaBreachedFalseAndNearBreachNotifiedFalse(
            Collection<Status> excludedStatuses, Instant now);

    List<Incident> findBySlaBreachedTrueAndBreachNotifiedFalse();
}
