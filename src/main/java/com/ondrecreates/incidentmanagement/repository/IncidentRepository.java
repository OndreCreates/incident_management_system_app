package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.Status;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByStatusNotIn(Collection<Status> statuses);

    long countBySeverityAndStatusNotIn(Severity severity, Collection<Status> statuses);

    long countBySlaBreachedTrue();

    long countByStatusIn(Collection<Status> statuses);

    long countByStatusInAndSlaBreachedFalse(Collection<Status> statuses);

    List<Incident> findByStatusNotInAndSlaDeadlineBeforeAndSlaBreachedFalse(Collection<Status> excludedStatuses,
                                                                             Instant now);

    List<Incident> findByStatusNotInAndNearBreachAtBeforeAndSlaBreachedFalseAndNearBreachNotifiedFalse(
            Collection<Status> excludedStatuses, Instant now);

    List<Incident> findBySlaBreachedTrueAndBreachNotifiedFalse();

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, created_at, resolved_at)) FROM incident "
            + "WHERE resolved_at IS NOT NULL", nativeQuery = true)
    Double averageResolutionMinutes();

    @Query(value = "SELECT DATE(created_at) AS day, COUNT(*) AS cnt FROM incident "
            + "WHERE created_at >= :since GROUP BY DATE(created_at) ORDER BY day", nativeQuery = true)
    List<Object[]> countCreatedPerDaySince(@Param("since") Instant since);
}
